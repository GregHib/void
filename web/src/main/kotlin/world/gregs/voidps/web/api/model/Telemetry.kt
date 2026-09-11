@file:UseSerializers(InstantSerializer::class)

package world.gregs.voidps.web.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

/** The five cards across the top of the developer dashboard, plus the tick panel's aggregates. */
@Serializable
data class DevStats(
    val playersOnline: Int,
    val playersPeakToday: Int = 0,
    val playersChangeLastHour: Int = 0,
    val loginsPerMinute: Int = 0,
    val uptimeSeconds: Long,
    val startedAt: Instant,
    val cpuPercent: Double,
    val cores: Int,
    val heapUsedBytes: Long,
    val heapMaxBytes: Long,
    val rssBytes: Long = 0,
    val connections: Int = 0,
    val connectionLimit: Int = 0,
    val worldsHosted: List<Int> = emptyList(),
    val tick: TickStats,
)

/** [overruns] counts samples in the window above [budgetMs] — the game loop's tick budget. */
@Serializable
data class TickStats(
    val currentMs: Double,
    val averageMs: Double,
    val p95Ms: Double = 0.0,
    val maxMs: Double = 0.0,
    val budgetMs: Int = 600,
    val overruns: Int = 0,
)

/**
 * The initial fill for the dashboard's charts. Every series in [series] is the same length and
 * ordered oldest first, spaced by [intervalSeconds]; [sampledAt] is the newest sample.
 */
@Serializable
data class MetricsHistory(
    val sampledAt: Instant,
    val intervalSeconds: Int,
    val series: MetricsSeries,
)

@Serializable
data class MetricsSeries(
    val cpuPercent: List<Double> = emptyList(),
    val heapUsedBytes: List<Long> = emptyList(),
    val rssBytes: List<Long> = emptyList(),
    val tickMs: List<Double> = emptyList(),
    val players: List<Int> = emptyList(),
    val loginsPerMinute: List<Int> = emptyList(),
)

/** One `sample` event on the telemetry stream. */
@Serializable
data class MetricsSample(
    val at: Instant,
    val world: Int? = null,
    val cpuPercent: Double,
    val heapUsedBytes: Long,
    val rssBytes: Long,
    val tickMs: Double,
    val players: Int,
    val loginsPerMinute: Int,
)

@Serializable
data class RuntimeInfo(
    val version: String,
    val revision: Int,
    val commit: String,
    val jvm: String,
    val startedAt: Instant,
    val worldsHosted: List<Int> = emptyList(),
    val connections: Int = 0,
    val connectionLimit: Int = 0,
    val gcPausesLastHour: GcPauses? = null,
)

@Serializable
data class GcPauses(
    val count: Int,
    val averageMs: Double,
)

/** Filters for the dashboard's "Recent errors" panel. A null [level] is every level. */
data class ErrorQuery(
    val world: Int? = null,
    val level: ErrorLevel? = null,
    val since: Instant? = null,
    val page: PageRequest = PageRequest(),
)

/** [occurrences] is how often this message repeated in the window; [occurredAt] is the latest. */
@Serializable
data class ErrorEntry(
    val id: String,
    val level: ErrorLevel,
    val message: String,
    val source: String? = null,
    val world: Int? = null,
    val thread: String? = null,
    val occurrences: Int = 1,
    val occurredAt: Instant,
    val stackTrace: String? = null,
)

@Serializable
enum class ErrorLevel(val wire: String) {
    @SerialName("warn")
    Warn("warn"),

    @SerialName("error")
    Error("error"),
    ;

    companion object {
        fun of(value: String?): ErrorLevel? = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}

/** A line in the dashboard console. [level] maps to the console's four line colours. */
@Serializable
data class ConsoleLine(
    val level: ConsoleLevel,
    val text: String,
    val at: Instant,
    val world: Int? = null,
)

@Serializable
enum class ConsoleLevel(val wire: String) {
    @SerialName("info")
    Info("info"),

    @SerialName("ok")
    Ok("ok"),

    @SerialName("warn")
    Warn("warn"),

    @SerialName("error")
    Error("error"),
}

@Serializable
data class ConsoleCommand(
    val command: String,
    val world: Int? = null,
)

/**
 * Only the lines the command produced directly — anything it causes elsewhere arrives on the
 * console stream instead. [accepted] is false for an unknown command, which is still logged.
 */
@Serializable
data class CommandResult(
    val accepted: Boolean,
    val lines: List<ConsoleLine>,
)

/** The `{ "lines": [...] }` envelope the console backlog uses. */
@Serializable
data class ConsoleLog(val lines: List<ConsoleLine>)
