package world.gregs.voidps.web.api.service

import kotlinx.coroutines.flow.Flow
import world.gregs.voidps.web.api.model.AccountSummary
import world.gregs.voidps.web.api.model.CommandResult
import world.gregs.voidps.web.api.model.ConsoleCommand
import world.gregs.voidps.web.api.model.ConsoleLine
import world.gregs.voidps.web.api.model.DevStats
import world.gregs.voidps.web.api.model.ErrorEntry
import world.gregs.voidps.web.api.model.ErrorQuery
import world.gregs.voidps.web.api.model.MetricsHistory
import world.gregs.voidps.web.api.model.MetricsSample
import world.gregs.voidps.web.api.model.Page
import world.gregs.voidps.web.api.model.RuntimeInfo
import kotlin.time.Duration

/**
 * The developer dashboard. Staff only — the routes check the rank, not this interface.
 *
 * A null `world` aggregates across every hosted world; the dashboard passes the world its header
 * is pointed at.
 *
 * The two stream methods return cold [Flow]s: a new collector gets its own subscription, and
 * cancelling the flow is what unsubscribes. That is what the header's Live switch does — it stops
 * collecting, then backfills from [metrics] when switched back on.
 */
interface TelemetryService {

    suspend fun stats(world: Int? = null): DevStats

    /** [samples] most recent samples, spaced by [interval], oldest first. */
    suspend fun metrics(world: Int? = null, samples: Int = 90, interval: Duration = SAMPLE_INTERVAL): MetricsHistory

    fun metricsStream(world: Int? = null): Flow<MetricsSample>

    suspend fun runtime(): RuntimeInfo

    /** Grouped by message so a repeating error is one row with a count. */
    suspend fun errors(query: ErrorQuery): Page<ErrorEntry>

    /** The backlog already in the console when the dashboard loads, oldest first. */
    suspend fun console(world: Int? = null, limit: Int = 90): List<ConsoleLine>

    fun consoleStream(world: Int? = null): Flow<ConsoleLine>

    /**
     * Runs [command] as [staff], who is recorded against anything it changes. The result holds
     * only the lines the command produced directly; knock-on output arrives on [consoleStream].
     */
    suspend fun run(command: ConsoleCommand, staff: AccountSummary): CommandResult

    companion object {
        val SAMPLE_INTERVAL: Duration = kotlin.time.Duration.parse("1s")
    }
}
