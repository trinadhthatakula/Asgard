package com.valhalla.asgard.app

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import android.view.FrameMetrics
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.charts.*
import com.valhalla.asgard.components.*
import com.valhalla.asgard.expressivePress
import com.valhalla.asgard.navigation.*
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.File
import kotlin.math.sin

/** Benchmark-only activity: automated input, warmup, and raw platform frame timings. */
class ComponentBenchmarkActivity : ComponentActivity() {
    private val thread = HandlerThread("AsgardFrameMetrics")
    private var listener: Window.OnFrameMetricsAvailableListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT < 31) {
            Log.e("AsgardBenchmark", "Frame deadline/GPU benchmarks require Android 12 or newer")
            finish()
            return
        }
        startBenchmark()
    }

    @androidx.annotation.RequiresApi(31)
    private fun startBenchmark() {
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val scenario = intent.getStringExtra("scenario") ?: "progress"
        val run = intent.getStringExtra("run")?.replace(Regex("[^a-zA-Z0-9_-]"), "_") ?: "sample"
        val seconds = intent.getIntExtra("seconds", 12).coerceIn(3, 30)
        val preferredMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
        window.attributes = window.attributes.apply { preferredDisplayModeId = preferredMode?.modeId ?: 0 }
        val metricIds = intArrayOf(FrameMetrics.TOTAL_DURATION, FrameMetrics.DRAW_DURATION,
            FrameMetrics.LAYOUT_MEASURE_DURATION, FrameMetrics.ANIMATION_DURATION,
            FrameMetrics.INPUT_HANDLING_DURATION, FrameMetrics.SYNC_DURATION,
            FrameMetrics.COMMAND_ISSUE_DURATION, FrameMetrics.GPU_DURATION, FrameMetrics.DEADLINE)
        val frames = LongArray(6000 * metricIds.size)
        var count = 0
        var dropped = 0
        var begin = Long.MAX_VALUE
        var end = Long.MAX_VALUE
        var cpuStart = 0L
        thread.start()
        val worker = Handler(thread.looper)
        listener = Window.OnFrameMetricsAvailableListener { _, metrics, missed ->
            val now = System.nanoTime()
            if (now in begin..end && count < 6000 && metrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 0L) {
                val offset = count * metricIds.size
                metricIds.forEachIndexed { index, metric -> frames[offset + index] = metrics.getMetric(metric) }
                count++
                dropped += missed
            }
        }
        window.addOnFrameMetricsAvailableListener(listener!!, worker)
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding().padding(8.dp)) {
                    StressScenario(scenario)
                }
                LaunchedEffect(Unit) {
                    // Delay recording until the deterministic workload and pipeline have warmed.
                    delay(4000)
                    worker.post {
                        cpuStart = Process.getElapsedCpuTime()
                        begin = System.nanoTime()
                        end = begin + seconds * 1_000_000_000L
                    }
                    delay(seconds * 1000L)
                    window.removeOnFrameMetricsAvailableListener(listener!!)
                    worker.post {
                        val cpuMs = Process.getElapsedCpuTime() - cpuStart
                        val directory = File(getExternalFilesDir(null), "benchmarks").apply { mkdirs() }
                        File(directory, "$run-$scenario.csv").bufferedWriter().use { out ->
                            out.appendLine(HEADERS.joinToString(","))
                            repeat(count) { frame ->
                                out.appendLine((metricIds.indices).joinToString(",") { index -> frames[frame * metricIds.size + index].toString() })
                            }
                        }
                        File(directory, "$run-$scenario.json").writeText(JSONObject()
                            .put("scenario", scenario).put("run", run).put("warmupSeconds", 4)
                            .put("measureSeconds", seconds).put("frames", count).put("droppedMetricCallbacks", dropped)
                            .put("processCpuMs", cpuMs).put("refreshRateHz", display?.refreshRate)
                            .put("model", android.os.Build.MODEL).put("sdk", android.os.Build.VERSION.SDK_INT)
                            .put("debuggable", applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0)
                            .toString(2))
                        Log.i("AsgardBenchmark", "DONE $run $scenario frames=$count cpuMs=$cpuMs")
                        runOnUiThread { finish() }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        listener?.let { window.removeOnFrameMetricsAvailableListener(it) }
        thread.quitSafely()
        super.onDestroy()
    }

    companion object {
        private val HEADERS = listOf("totalNs", "drawNs", "layoutNs", "animationNs", "inputNs", "syncNs", "commandNs", "gpuNs", "deadlineNs")
    }
}

@Composable
private fun StressScenario(scenario: String) {
    when (scenario) {
        "progress" -> ProgressStress()
        "shimmer" -> ShimmerStress()
        "charts" -> ChartStress()
        "navigation" -> NavigationStress()
        "scrollbar" -> ScrollbarStress()
        "numerals" -> NumeralStress()
        "press" -> PressStress()
        "connected" -> ConnectedStress()
        "pulse" -> PulseStress()
        else -> error("Unknown benchmark scenario: $scenario")
    }
}

@Composable private fun ProgressStress() {
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { while (true) { delay(900); progress = 1f - progress } }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(8) { row -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) { column ->
                AsgardProgressRing(progress, size = 64.dp, animate = true,
                    animationSpec = tween(800), contentDescription = "Ring ${row * 5 + column}",
                    content = { Text("Ring", style = MaterialTheme.typography.labelSmall) })
            }
        } }
    }
}

@Composable private fun ShimmerStress() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(18) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { AsgardShimmer(Modifier.weight(1f).height(32.dp)) }
        } }
    }
}

@Composable private fun ChartStress() {
    val phase by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(1000), RepeatMode.Restart))
    val series = remember { List(4) { line -> AsgardLineSeries(
        List(512) { index -> AsgardChartPoint(index.toFloat(), 50f + 30f * sin(index / 14f + line)) },
        listOf(Color(0xFF287844), Color(0xFF405BD4), Color(0xFFB02B5B), Color(0xFF9B660C))[line]) } }
    Column {
        repeat(3) { AsgardLineChart(series, modifier = Modifier.fillMaxWidth().height(220.dp).drawWithContent {
            // An animated host redraws a chart without changing its data or measured bounds.
            phase
            drawContent()
        }) }
    }
}

private val BenchmarkIcon = ImageVector.Builder("Benchmark", 24.dp, 24.dp, 24f, 24f).build()

@Composable private fun NavigationStress() {
    var selected by remember { mutableIntStateOf(0) }
    val items = remember { listOf("Home", "Applications", "Freezer", "Settings").map { AsgardNavItem(BenchmarkIcon, it) } }
    LaunchedEffect(Unit) { while (true) { delay(180); selected = (selected + 1) % items.size } }
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        AsgardNavigationAppearance.entries.forEach { appearance ->
            AsgardNavigationBar(items, selected, {}, configuration = AsgardNavigationConfiguration(
                appearance = appearance, placement = AsgardNavigationPlacement.Floating))
        }
        AsgardNavigationRail(items, selected, {}, configuration = AsgardNavigationConfiguration(labels = AsgardNavigationLabels.Selected))
    }
}

@Composable private fun ScrollbarStress() {
    val list = rememberLazyListState()
    LaunchedEffect(list) { while (true) {
        repeat(100) { androidx.compose.runtime.withFrameNanos { }; list.scrollBy(18f) }
        repeat(100) { androidx.compose.runtime.withFrameNanos { }; list.scrollBy(-18f) }
    } }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(end = 48.dp), state = list) {
            items(500, key = { it }, contentType = { "row" }) { index ->
                AsgardListRow("Application $index", subtitle = "Variable-height list row", modifier = Modifier.fillMaxWidth().height(if (index % 3 == 0) 96.dp else 64.dp))
            }
        }
        AsgardLazyScrollbar(rememberAsgardScrollbarState(list), Modifier.align(Alignment.CenterEnd))
    }
}

@Composable private fun NumeralStress() {
    var value by remember { mutableIntStateOf(9) }
    LaunchedEffect(Unit) { while (true) { delay(600); value = if (value == 9) 100 else 9 } }
    Column { repeat(10) { Row(Modifier.fillMaxWidth()) {
        AsgardAnimatedNumeral(value.toString(), Modifier.weight(1f))
        AsgardStatTile("Count", value.toString(), Modifier.weight(1f), animateValue = true)
    } } }
}

@Composable private fun PressStress() {
    val sources = remember { List(40) { MutableInteractionSource() } }
    LaunchedEffect(Unit) { while (true) {
        val presses = sources.map { PressInteraction.Press(Offset.Zero) }
        sources.forEachIndexed { index, source -> source.emit(presses[index]) }
        delay(120)
        sources.forEachIndexed { index, source -> source.emit(PressInteraction.Release(presses[index])) }
        delay(180)
    } }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { repeat(8) { row -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) { column -> Box(Modifier.size(60.dp).expressivePress(sources[row * 5 + column]).background(MaterialTheme.colorScheme.primaryContainer)) }
    } } }
}

@Composable private fun ConnectedStress() {
    var selected by remember { mutableIntStateOf(0) }
    val items = remember { listOf(ConnectedButtonGroupItem.Label("User"), ConnectedButtonGroupItem.Label("System")) }
    LaunchedEffect(Unit) { while (true) { delay(180); selected = 1 - selected } }
    Column { repeat(8) { ConnectedButtonGroup(items, selectedIndex = selected, onItemSelected = {}) } }
}

@Composable private fun PulseStress() {
    Column { repeat(8) { Row { repeat(5) { AsgardPulseRing(Modifier.size(64.dp), ringSize = 60.dp) } } } }
}
