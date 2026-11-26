# The PNG IHDR contains width, height, bit depth and colour type in network byte order.
def check_gallery_png(path, size:, color_type:, max_bytes:)
  header = File.binread(path, 26)
  unless header.start_with?("\x89PNG\r\n\x1a\n".b) && header.byteslice(12, 4) == "IHDR" &&
         header.byteslice(16, 10).unpack("N2C2") == [*size, 8, color_type] && File.size(path) <= max_bytes
    raise "Unexpected PNG format, dimensions or file size: #{path}"
  end
end

def validate_gallery_listing(directory)
  { "title.txt" => 30, "short_description.txt" => 80, "full_description.txt" => 4000 }.each do |name, limit|
    text = File.read(File.join(directory, name), encoding: "UTF-8")
    raise "#{name} must contain 1–#{limit} characters" unless text.valid_encoding? && (1..limit).cover?(text.length)
    raise "#{name} contains surrounding whitespace" unless text == text.strip
  end
  images = File.join(directory, "images")
  check_gallery_png(File.join(images, "icon.png"), size: [512, 512], color_type: 6, max_bytes: 1024**2)
  check_gallery_png(File.join(images, "featureGraphic.png"), size: [1024, 500], color_type: 2, max_bytes: 15 * 1024**2)
  shots = Dir[File.join(images, "phoneScreenshots", "*")].sort
  raise "Expected eight numbered phone screenshots" unless shots.length == 8
  shots.each_with_index do |path, index|
    raise "Unexpected screenshot order: #{path}" unless File.basename(path).start_with?(format("%02d-", index + 1))
    check_gallery_png(path, size: [1080, 1920], color_type: 2, max_bytes: 8 * 1024**2)
  end
end
