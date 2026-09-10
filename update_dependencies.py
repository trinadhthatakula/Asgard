#!/usr/bin/env python3
"""
Dependency updater script for Android Gradle Version Catalogs (libs.versions.toml).
Queries Google Maven, Maven Central, and the Gradle Plugin Portal to find the latest stable/unstable versions.

Use --wrapper --dry-run to also check official stable Gradle distributions.
Use --wrapper to update the distribution URL and official SHA-256 together;
--allow-major permits wrapper major upgrades only. --wrapper-file selects a
non-default properties path. Custom distribution URLs are never rewritten.
This updates distribution selection, not gradlew/gradlew.bat/the wrapper JAR.
To regenerate those files, run the target Gradle's wrapper task separately and
review the generated changes. Gradle/toolchain compatibility is not inferred.
"""

import argparse
import json
import os
import re
import tempfile
from pathlib import Path
import urllib.request
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed

def version_sort_key(v_str):
    """
    Returns a tuple suitable for comparing/sorting version strings semantically.
    Handles semantic versioning along with pre-release suffixes (alpha, beta, rc).
    """
    match = re.match(r'^(\d+(?:\.\d+)*)(.*)$', v_str)
    if not match:
        return ((), -1, ())
    numbers = tuple(int(x) for x in match.group(1).split('.'))
    suffix = match.group(2).strip('-._')
    
    channel = get_channel(v_str)
    weight = {'stable': 3, 'rc': 2, 'beta': 1, 'alpha': 0, 'dev': 0, 'other': 2.5}[channel]

    suffix_parts = []
    if suffix:
        # Split suffix into string parts and numeric parts for proper comparison (e.g., 'alpha02' > 'alpha01')
        for part in re.split(r'(\d+)', suffix):
            if part.isdigit():
                suffix_parts.append((0, int(part)))
            elif part:
                suffix_parts.append((1, part.lower()))
                
    return (numbers, weight, tuple(suffix_parts))

def get_channel(v_str):
    """
    Classifies a version string into a release channel.
    """
    match = re.match(r'^(\d+(?:\.\d+)*)(.*)$', v_str)
    if not match:
        return 'stable'
    suffix = match.group(2).strip('-._').lower()
    if not suffix or suffix in {'stable', 'final', 'ga', 'release'}:
        return 'stable'
    for channel, qualifiers in (
        ('rc', 'rc|cr'), ('beta', 'beta|b'), ('alpha', 'alpha|a'),
        ('dev', 'dev|milestone|m'),
    ):
        if re.fullmatch(rf'(?:{qualifiers})(?:[-._]?\d+)?', suffix):
            return channel
    return 'other'


def positive_int(value):
    try:
        number = int(value)
    except ValueError:
        raise argparse.ArgumentTypeError('must be a positive integer')
    if number <= 0:
        raise argparse.ArgumentTypeError('must be a positive integer')
    return number


def get_best_version(versions, current_version, allow_pre_releases_for_stable=False):
    """
    Selects the best/latest version from a list of candidates.
    Respects version channels: e.g. if current is alpha, prefers newer alphas, and
    only falls back/upgrades to beta/rc/stable if no newer alpha exists.
    """
    current_key = version_sort_key(current_version)
    current_channel = get_channel(current_version)
    
    # Filter candidates that are strictly greater than current
    newer_candidates = []
    for v in versions:
        key = version_sort_key(v)
        if key > current_key:
            newer_candidates.append((key, v))
            
    if not newer_candidates:
        return current_version
        
    # Group newer candidates by channel
    channel_groups = {
        'stable': [],
        'rc': [],
        'beta': [],
        'alpha': [],
        'dev': [],
        'other': []
    }
    for key, v in newer_candidates:
        ch = get_channel(v)
        channel_groups[ch].append((key, v))
        
    # Standard channel hierarchy from current to stable
    hierarchy = ['dev', 'alpha', 'beta', 'rc', 'stable']
    
    if current_channel not in hierarchy:
        # Fallback to absolute sorting if current is not in standard hierarchy
        newer_candidates.sort()
        return newer_candidates[-1][1]
        
    # Prefer the current channel if any candidates exist in it
    if channel_groups[current_channel]:
        channel_groups[current_channel].sort()
        return channel_groups[current_channel][-1][1]
        
    # If no candidates in the current channel, fall back/upwards to more stable channels
    current_idx = hierarchy.index(current_channel)
    for ch in hierarchy[current_idx + 1:]:
        if channel_groups[ch]:
            channel_groups[ch].sort()
            return channel_groups[ch][-1][1]
            
    # If we are stable and allow pre-releases, we might have pre-release candidates
    if current_channel == 'stable' and allow_pre_releases_for_stable:
        newer_candidates.sort()
        return newer_candidates[-1][1]
        
    return current_version

def fetch_versions_from_url(url):
    """
    Fetches and parses maven-metadata.xml from a given repository URL.
    """
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=5) as response:
            content = response.read()
            root = ET.fromstring(content)
            return [v.text for v in root.findall(".//versioning/versions/version") if v.text]
    except Exception:
        return []

def get_library_urls(group, name):
    """
    Returns repository metadata URLs for standard Maven libraries.
    """
    group_path = group.replace('.', '/')
    return [
        f"https://maven.google.com/{group_path}/{name}/maven-metadata.xml",
        f"https://repo1.maven.org/maven2/{group_path}/{name}/maven-metadata.xml"
    ]

def get_plugin_urls(plugin_id):
    """
    Returns repository metadata URLs for Gradle plugins.
    """
    id_path = plugin_id.replace('.', '/')
    return [
        f"https://plugins.gradle.org/m2/{id_path}/{plugin_id}.gradle.plugin/maven-metadata.xml",
        f"https://maven.google.com/{id_path}/{plugin_id}.gradle.plugin/maven-metadata.xml",
        f"https://repo1.maven.org/maven2/{id_path}/{plugin_id}.gradle.plugin/maven-metadata.xml"
    ]

def consumer_versions(is_plugin, details):
    urls = get_plugin_urls(details) if is_plugin else get_library_urls(*details)
    return {version for url in urls for version in fetch_versions_from_url(url)}


def wrapper_update(content, allow_major=False):
    """Plan an official stable distribution update; fetch its checksum before editing."""
    urls = list(re.finditer(r'(?m)^distributionUrl[ \t]*=[ \t]*(\S+?)[ \t]*\r?$', content))
    if len(urls) != 1:
        raise ValueError('expected exactly one distributionUrl')
    entry = urls[0]
    url = entry.group(1).replace('\\:', ':')
    distribution = re.fullmatch(
        r'https://services\.gradle\.org/distributions/gradle-(\d+(?:\.\d+)+)-(bin|all)\.zip', url)
    if not distribution:
        raise ValueError('only official stable Gradle distribution URLs are supported; custom URLs left unchanged')
    current, kind = distribution.groups()
    with urllib.request.urlopen('https://services.gradle.org/versions/all', timeout=15) as response:
        releases = json.load(response)
    if not isinstance(releases, list) or any(not isinstance(release, dict) for release in releases):
        raise ValueError('invalid Gradle release metadata')
    versions = [release['version'] for release in releases
                if re.fullmatch(r'\d+(?:\.\d+)+', release.get('version', ''))
                and not release.get('broken') and not release.get('snapshot')]
    if not versions:
        raise ValueError('no stable Gradle releases found in metadata')
    key = lambda version: tuple(map(int, version.split('.')))
    newer = [version for version in versions if key(version) > key(current)]
    majors = [version for version in newer if key(version)[0] > key(current)[0]]
    eligible = newer if allow_major else [version for version in newer if key(version)[0] == key(current)[0]]
    note = f'; major {max(majors, key=key)} skipped (use --allow-major)' if majors and not allow_major else ''
    if not eligible:
        return content, f'Gradle Wrapper: {current}, no eligible update{note}'
    target = max(eligible, key=key)
    target_url = f'https://services.gradle.org/distributions/gradle-{target}-{kind}.zip'
    with urllib.request.urlopen(target_url + '.sha256', timeout=15) as response:
        checksum = response.read().decode('ascii').strip()
    if not re.fullmatch(r'[0-9a-fA-F]{64}', checksum):
        raise ValueError('invalid official Gradle SHA-256 checksum; wrapper left unchanged')
    checksum_pattern = r'(?m)^(distributionSha256Sum[ \t]*=[ \t]*)[^\r\n]*'
    if len(re.findall(checksum_pattern, content)) > 1:
        raise ValueError('duplicate distributionSha256Sum properties')
    escaped_url = target_url.replace(':', '\\:') if '\\:' in entry.group(1) else target_url
    updated = content[:entry.start(1)] + escaped_url + content[entry.end(1):]
    if re.search(checksum_pattern, updated):
        updated = re.sub(checksum_pattern, lambda match: match.group(1) + checksum.lower(), updated)
    else:
        newline = '\r\n' if '\r\n' in content else '\n'
        updated += ('' if updated.endswith('\n') else newline) + 'distributionSha256Sum=' + checksum.lower() + newline
    major = ' [MAJOR]' if key(target)[0] != key(current)[0] else ''
    return updated, f'Gradle Wrapper: {current} -> {target}{major}{note}'


def update_wrapper(path, dry_run=False, allow_major=False):
    path = Path(path)
    original = path.read_bytes()
    updated, report = wrapper_update(original.decode('utf-8'), allow_major)
    print(report)
    if updated.encode('utf-8') == original:
        return
    print('Distribution URL and SHA-256 are updated together. Wrapper scripts/JAR are unchanged; '
          'regenerate them with the target Gradle wrapper task when needed.')
    if dry_run:
        print('Wrapper dry run: no files modified.')
        return
    temporary = None
    try:
        with tempfile.NamedTemporaryFile(dir=path.parent, delete=False) as output:
            temporary = Path(output.name)
            output.write(updated.encode('utf-8'))
        temporary.chmod(path.stat().st_mode)
        if path.read_bytes() != original:
            raise ValueError('wrapper changed during lookup; refusing to overwrite it')
        os.replace(temporary, path)
    finally:
        if temporary is not None:
            temporary.unlink(missing_ok=True)


def check_artifact_version(item, allow_pre_releases_for_stable):
    """Select a version available to every consumer with resolvable metadata."""
    item_type, key, current, is_plugin, details = item
    consumers = details if item_type == 'catalog' else [(is_plugin, details)]
    resolved = [versions for plugin, artifact in consumers
                if (versions := consumer_versions(plugin, artifact))]
    if not resolved:
        return item_type, key, current, current, "Not Found"

    # Different individual latest versions are fine: choose from their intersection
    # while preserving the existing prerelease-channel preference.
    common_versions = set.intersection(*resolved)
    best = get_best_version(common_versions, current, allow_pre_releases_for_stable)
    return item_type, key, current, best, "Success"


def parse_toml_sections(content):
    """
    Parses a TOML string into its top-level sections and variables.
    """
    versions = {}
    libraries = []
    plugins = []
    
    current_section = None
    for line in content.splitlines():
        line_clean = line.strip()
        if not line_clean or line_clean.startswith('#'):
            continue
        if line_clean.startswith('[') and line_clean.endswith(']'):
            current_section = line_clean[1:-1].strip()
            continue
            
        if current_section == "versions":
            m = re.match(r'^([a-zA-Z0-9_\-]+)\s*=\s*"([^"]+)"', line_clean)
            if m:
                versions[m.group(1)] = m.group(2)
        elif current_section == "libraries":
            m = re.match(r'^([a-zA-Z0-9_\-]+)\s*=\s*\{([^}]+)\}', line_clean)
            if m:
                lib_name = m.group(1)
                props_str = m.group(2)
                props = {}
                for part in props_str.split(','):
                    if '=' in part:
                        k, v = part.split('=', 1)
                        props[k.strip()] = v.strip().strip('"\'')
                libraries.append((lib_name, props))
        elif current_section == "plugins":
            m = re.match(r'^([a-zA-Z0-9_\-]+)\s*=\s*\{([^}]+)\}', line_clean)
            if m:
                plugin_name = m.group(1)
                props_str = m.group(2)
                props = {}
                for part in props_str.split(','):
                    if '=' in part:
                        k, v = part.split('=', 1)
                        props[k.strip()] = v.strip().strip('"\'')
                plugins.append((plugin_name, props))
                
    return versions, libraries, plugins

def main():
    parser = argparse.ArgumentParser(description="Update Android libs.versions.toml dependencies to latest versions.")
    parser.add_argument("--file", default="gradle/libs.versions.toml", help="Path to libs.versions.toml file.")
    parser.add_argument("--dry-run", action="store_true", help="Print updates without writing changes.")
    parser.add_argument("--max-workers", type=positive_int, default=10, help="Max thread pool workers for concurrent requests.")
    parser.add_argument("--pre-releases", action="store_true", help="Allow upgrading stable versions to pre-releases (alpha/beta/rc).")
    parser.add_argument("--wrapper", action="store_true", help="Include official Gradle Wrapper distribution and checksum updates.")
    parser.add_argument("--wrapper-file", default="gradle/wrapper/gradle-wrapper.properties", help="Path to Gradle wrapper properties.")
    parser.add_argument("--allow-major", action="store_true", help="Allow major Gradle Wrapper upgrades (dependency policy is unchanged).")
    
    args = parser.parse_args()
    
    if not os.path.exists(args.file):
        print(f"Error: file not found at '{args.file}'")
        return
        
    with open(args.file, 'r') as f:
        content = f.read()

    if args.wrapper:
        try:
            update_wrapper(args.wrapper_file, args.dry_run, args.allow_major)
        except (OSError, ValueError) as error:
            parser.exit(1, f'Wrapper update failed: {error}\n')
        
    versions, libraries, plugins = parse_toml_sections(content)
    
    to_check = []
    version_refs_checked = set()
    
    # Collect every consumer before checking each shared reference once.
    reference_consumers = {}
    for lib_name, props in libraries:
        v_ref = props.get('version.ref')
        if v_ref and v_ref in versions:
            group, name = props.get('group'), props.get('name')
            if not (group and name):
                parts = props.get('module', '').split(':')
                if len(parts) != 2:
                    continue
                group, name = parts
            reference_consumers.setdefault(v_ref, []).append((False, (group, name)))

    for plugin_name, props in plugins:
        v_ref, p_id = props.get('version.ref'), props.get('id')
        if v_ref and v_ref in versions and p_id:
            reference_consumers.setdefault(v_ref, []).append((True, p_id))

    for v_ref, consumers in reference_consumers.items():
        if v_ref not in version_refs_checked:
            to_check.append(("catalog", v_ref, versions[v_ref], None, consumers))
            version_refs_checked.add(v_ref)

    # Libraries using inline version
    for lib_name, props in libraries:
        v_val = props.get('version')
        if v_val and not props.get('version.ref'):
            group = props.get('group')
            name = props.get('name')
            if group and name:
                to_check.append(("inline_lib", lib_name, v_val, False, (group, name)))
            elif 'module' in props:
                parts = props['module'].split(':')
                if len(parts) == 2:
                    to_check.append(("inline_lib", lib_name, v_val, False, (parts[0], parts[1])))
                    
    # Plugins using inline version
    for plugin_name, props in plugins:
        v_val = props.get('version')
        if v_val and not props.get('version.ref'):
            p_id = props.get('id')
            if p_id:
                to_check.append(("inline_plugin", plugin_name, v_val, True, p_id))

    print(f"Checking {len(to_check)} dependency versions...")
    
    results = []
    with ThreadPoolExecutor(max_workers=args.max_workers) as executor:
        futures = {executor.submit(check_artifact_version, item, args.pre_releases): item for item in to_check}
        for future in as_completed(futures):
            results.append(future.result())
            
    catalog_updates = {}
    inline_lib_updates = {}
    inline_plugin_updates = {}
    
    # Everything that could not be resolved at all. Without this list a network outage, a renamed
    # artifact or a repository that has stopped serving maven-metadata.xml all look identical to
    # "already on the latest version" — the script would print "All dependencies are up to date."
    # having checked nothing.
    unresolved = sorted(key for _, key, _, _, status in results if status != "Success")

    for item_type, key, current, best, status in results:
        if status == "Success" and current != best:
            if item_type == "catalog":
                catalog_updates[key] = (current, best)
            elif item_type == "inline_lib":
                inline_lib_updates[key] = (current, best)
            elif item_type == "inline_plugin":
                inline_plugin_updates[key] = (current, best)
                
    if unresolved:
        print(f"\nCould not resolve {len(unresolved)} of {len(results)} — versions unknown, not current:")
        for key in unresolved:
            print(f"  [Unresolved] {key}")

    if not catalog_updates and not inline_lib_updates and not inline_plugin_updates:
        if not results:
            # `results` is empty exactly when `to_check` was, so no lookup was attempted and no
            # network failure is implied. The message stays on that observation rather than naming a
            # cause: an empty or renamed catalog section and a catalog whose entries never resolve
            # to a versioned `group:name` both land here, and the script cannot tell them apart.
            # Distinct from the message below, which needs a lookup to have run and failed.
            print("\nNo versioned dependencies were found to check — nothing was verified.")
        elif len(unresolved) == len(results):
            print("\nNothing was checked — every lookup failed. Not a clean bill of health.")
        else:
            print(f"\nAll {len(results) - len(unresolved)} resolved dependencies are up to date.")
        return

    print("\nProposed Updates:")
    for key, (current, best) in sorted(catalog_updates.items()):
        print(f"  [Catalog Version] {key}: {current} -> {best}")
    for key, (current, best) in sorted(inline_lib_updates.items()):
        print(f"  [Inline Library] {key}: {current} -> {best}")
    for key, (current, best) in sorted(inline_plugin_updates.items()):
        print(f"  [Inline Plugin] {key}: {current} -> {best}")
        
    if args.dry_run:
        print("\nDry run completed. No files modified.")
        return
        
    lines = content.splitlines()
    updated_content_lines = []
    current_section = None
    
    for line in lines:
        line_strip = line.strip()
        if line_strip.startswith('[') and line_strip.endswith(']'):
            current_section = line_strip[1:-1].strip()
            updated_content_lines.append(line)
            continue
            
        if current_section == "versions":
            for v_name, (curr, best) in catalog_updates.items():
                pattern = rf'^(\s*{v_name}\s*=\s*")([^"]+)(")'
                if re.match(pattern, line):
                    line = re.sub(pattern, rf'\g<1>{best}\g<3>', line)
                    break
        elif current_section == "libraries":
            for lib_name, (curr, best) in inline_lib_updates.items():
                pattern = rf'^(\s*{lib_name}\s*=\s*\{{.*version\s*=\s*")([^"]+)(")'
                if re.match(pattern, line):
                    line = re.sub(pattern, rf'\g<1>{best}\g<3>', line)
                    break
        elif current_section == "plugins":
            for plugin_name, (curr, best) in inline_plugin_updates.items():
                pattern = rf'^(\s*{plugin_name}\s*=\s*\{{.*version\s*=\s*")([^"]+)(")'
                if re.match(pattern, line):
                    line = re.sub(pattern, rf'\g<1>{best}\g<3>', line)
                    break
                    
        updated_content_lines.append(line)
        
    updated_content = '\n'.join(updated_content_lines) + '\n'
    
    with open(args.file, 'w') as f:
        f.write(updated_content)
        
    print(f"\nSuccessfully updated {args.file}")

if __name__ == "__main__":
    main()
