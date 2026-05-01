package content.bot

import com.github.michaelbull.logging.InlineLogger
import java.lang.management.ManagementFactory

/**
 * Tier-0 perf instrumentation for [BotManager]. Disabled by default: the only overhead while idle
 * is one volatile read + branch in [BotManager.run]. The `/bot_stress` admin command flips it on
 * for a fixed number of ticks, then [finish] formats a multi-line report and fires [onComplete]
 * once on the game thread.
 *
 * All mutating methods (begin/end/record/inc) are called from the game thread (`Contexts.Game`),
 * so the internal arrays/longs do not need synchronization beyond the volatile flag that gates
 * entry into the measuring path.
 */
object BotMetrics {

    private val logger = InlineLogger("BotMetrics")
    private const val MAX_BOT_TICK_SAMPLES = 500_000

    /**
     * Wall-clock time per bot tick cannot distinguish "doing work" from "waiting on GC". The
     * Oracle/OpenJDK extension [com.sun.management.ThreadMXBean.getCurrentThreadAllocatedBytes]
     * reports bytes allocated on the current thread, which is a cleaner signal for hot-path
     * churn. Wrapped in a try/catch so non-HotSpot JVMs still run (with alloc stats skipped).
     */
    private val threadBean: com.sun.management.ThreadMXBean? = try {
        (ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean)
            ?.takeIf { it.isThreadAllocatedMemorySupported }
            ?.also { it.isThreadAllocatedMemoryEnabled = true }
    } catch (_: Throwable) {
        null
    }

    @Volatile
    var measuring: Boolean = false
        private set

    private var label: String = ""
    private var ticksRemaining: Int = 0
    private var ticksTotal: Int = 0
    private var runStartNanos: Long = 0L
    private var runStartAllocBytes: Long = 0L

    private var runNanos: LongArray = LongArray(0)
    private var runNanosCount: Int = 0

    private var runAllocBytes: LongArray = LongArray(0)
    private var runAllocBytesCount: Int = 0
    private var totalAllocBytes: Long = 0L

    private var botTickNanos: LongArray = LongArray(0)
    private var botTickNanosCount: Int = 0
    private var botTickSamplesDropped: Int = 0

    private var heapUsedStart: Long = 0L
    private var heapUsedPeak: Long = 0L
    private var heapUsedEnd: Long = 0L

    private var scans: Long = 0
    private var totalBotTicksCounted: Long = 0

    private var onComplete: ((List<String>) -> Unit)? = null

    fun start(ticks: Int, label: String = "", onComplete: (List<String>) -> Unit) {
        require(ticks > 0) { "ticks must be > 0" }
        if (measuring) {
            onComplete(listOf("BotMetrics: stress test already running, ignoring start()."))
            return
        }
        this.label = label
        ticksRemaining = ticks
        ticksTotal = ticks
        runNanos = LongArray(ticks)
        runNanosCount = 0
        runAllocBytes = LongArray(ticks)
        runAllocBytesCount = 0
        totalAllocBytes = 0L
        botTickNanos = LongArray(MAX_BOT_TICK_SAMPLES)
        botTickNanosCount = 0
        botTickSamplesDropped = 0
        heapUsedStart = usedHeapBytes()
        heapUsedPeak = heapUsedStart
        heapUsedEnd = heapUsedStart
        scans = 0
        totalBotTicksCounted = 0
        this.onComplete = onComplete
        // Set last so any begin/end on a parallel game-thread call sees a fully-initialized state.
        measuring = true
        logger.info { "BotMetrics: started label='$label' ticks=$ticks" }
    }

    fun beginRun() {
        if (!measuring) return
        runStartNanos = System.nanoTime()
        runStartAllocBytes = threadBean?.currentThreadAllocatedBytes ?: -1L
    }

    fun endRun(botCount: Int) {
        if (!measuring) return
        val elapsed = System.nanoTime() - runStartNanos
        if (runNanosCount < runNanos.size) {
            runNanos[runNanosCount++] = elapsed
        }
        if (runStartAllocBytes >= 0) {
            val allocEnd = threadBean?.currentThreadAllocatedBytes ?: -1L
            if (allocEnd >= 0) {
                val delta = allocEnd - runStartAllocBytes
                if (delta >= 0) {
                    totalAllocBytes += delta
                    if (runAllocBytesCount < runAllocBytes.size) {
                        runAllocBytes[runAllocBytesCount++] = delta
                    }
                }
            }
        }
        val used = usedHeapBytes()
        heapUsedEnd = used
        if (used > heapUsedPeak) heapUsedPeak = used
        ticksRemaining--
        if (ticksRemaining <= 0) {
            finish(botCount)
        }
    }

    fun recordBotTick(nanos: Long) {
        if (!measuring) return
        totalBotTicksCounted++
        if (botTickNanosCount < botTickNanos.size) {
            botTickNanos[botTickNanosCount++] = nanos
        } else {
            botTickSamplesDropped++
        }
    }

    fun incScans() {
        if (measuring) scans++
    }

    private fun usedHeapBytes(): Long {
        val rt = Runtime.getRuntime()
        return rt.totalMemory() - rt.freeMemory()
    }

    private fun finish(currentBots: Int) {
        val callback = onComplete
        val report = buildReport(currentBots)
        // Flip the flag off before invoking the callback so a re-entrant /bot_stress works.
        measuring = false
        onComplete = null
        report.forEach { line -> logger.info { line } }
        callback?.invoke(report)
    }

    private fun buildReport(currentBots: Int): List<String> {
        val lines = mutableListOf<String>()
        lines += "=== BotMetrics report${if (label.isNotEmpty()) " (label=\"$label\")" else ""} ==="
        lines += "bots          : $currentBots"
        lines += "ticks         : $ticksTotal (recorded=$runNanosCount)"
        lines += "managerRun ms : ${formatStats(runNanos, runNanosCount, divisorNs = 1_000_000.0, decimals = 2)}"
        val perTickAvg = if (runNanosCount > 0) totalBotTicksCounted.toDouble() / runNanosCount else 0.0
        lines += "botTick    us : ${formatStats(botTickNanos, botTickNanosCount, divisorNs = 1_000.0, decimals = 0)}" +
            "  samples=$totalBotTicksCounted  perTick=${"%.1f".format(perTickAvg)}" +
            if (botTickSamplesDropped > 0) "  dropped=$botTickSamplesDropped" else ""
        lines += "spiralScans   : $scans  perBotTick=${ratio(scans, totalBotTicksCounted)}"
        lines += formatAllocLine()
        lines += formatHeapLine()
        lines += "================================================="
        return lines
    }

    private fun formatAllocLine(): String {
        if (threadBean == null) return "gameThreadAlloc: unsupported on this JVM"
        if (runAllocBytesCount <= 0) return "gameThreadAlloc: no samples"
        val sorted = runAllocBytes.copyOf(runAllocBytesCount)
        sorted.sort()
        val avg = totalAllocBytes.toDouble() / runAllocBytesCount
        val p50 = sorted[(runAllocBytesCount * 50 / 100).coerceAtMost(runAllocBytesCount - 1)].toDouble()
        val p95 = sorted[(runAllocBytesCount * 95 / 100).coerceAtMost(runAllocBytesCount - 1)].toDouble()
        val p99 = sorted[(runAllocBytesCount * 99 / 100).coerceAtMost(runAllocBytesCount - 1)].toDouble()
        val max = sorted[runAllocBytesCount - 1].toDouble()
        val perBotTick = if (totalBotTicksCounted > 0) totalAllocBytes.toDouble() / totalBotTicksCounted else 0.0
        return "gameThreadAlloc KB/run: avg=${kb(avg)}  p50=${kb(p50)}  p95=${kb(p95)}  p99=${kb(p99)}  max=${kb(max)}" +
            "  total=${mb(totalAllocBytes.toDouble())}MB  perBotTick=${formatBytes(perBotTick)}"
    }

    private fun formatHeapLine(): String {
        val rt = Runtime.getRuntime()
        return "heap used MB   : start=${mb(heapUsedStart.toDouble())}  end=${mb(heapUsedEnd.toDouble())}  peak=${mb(heapUsedPeak.toDouble())}  max=${mb(rt.maxMemory().toDouble())}"
    }

    private fun kb(bytes: Double): String = "%.1f".format(bytes / 1_024.0)
    private fun mb(bytes: Double): String = "%.1f".format(bytes / (1_024.0 * 1_024.0))

    private fun formatBytes(bytes: Double): String = when {
        bytes >= 1_024 * 1_024 -> "%.2fMB".format(bytes / (1_024.0 * 1_024.0))
        bytes >= 1_024 -> "%.1fKB".format(bytes / 1_024.0)
        else -> "%.0fB".format(bytes)
    }

    private fun ratio(num: Long, denom: Long): String = if (denom <= 0) "n/a" else "%.2f".format(num.toDouble() / denom)

    private fun formatStats(values: LongArray, count: Int, divisorNs: Double, decimals: Int): String {
        if (count <= 0) return "no samples"
        val sorted = values.copyOf(count)
        sorted.sort()
        val sum = sorted.sumOf { it }
        val avg = sum.toDouble() / count / divisorNs
        val p50 = sorted[(count * 50 / 100).coerceAtMost(count - 1)] / divisorNs
        val p95 = sorted[(count * 95 / 100).coerceAtMost(count - 1)] / divisorNs
        val p99 = sorted[(count * 99 / 100).coerceAtMost(count - 1)] / divisorNs
        val max = sorted[count - 1] / divisorNs
        val fmt = "%.${decimals}f"
        return "avg=${fmt.format(avg)}  p50=${fmt.format(p50)}  p95=${fmt.format(p95)}  p99=${fmt.format(p99)}  max=${fmt.format(max)}"
    }
}
