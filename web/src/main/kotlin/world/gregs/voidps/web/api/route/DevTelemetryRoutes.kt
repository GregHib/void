package world.gregs.voidps.web.api.route

import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.Json
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.ConsoleCommand
import world.gregs.voidps.web.api.model.ConsoleLog
import world.gregs.voidps.web.api.model.ErrorLevel
import world.gregs.voidps.web.api.model.ErrorQuery
import world.gregs.voidps.web.api.service.TelemetryService
import kotlin.time.Duration.Companion.seconds

private const val MAX_SAMPLES = 3600
private const val MAX_CONSOLE_LINES = 1000

/**
 * The developer dashboard. Wrapped in [STAFF_AUTH], which rejects before the handler runs — the
 * two SSE routes depend on that, since a stream's status line is sent before its first event.
 */
fun Route.devTelemetryRoutes(telemetry: TelemetryService, json: Json) {
    authenticate(STAFF_AUTH) {
        route("/dev") {
            get("/stats") {
                call.respond(telemetry.stats(call.int("world")))
            }

            get("/metrics") {
                val samples = call.int("samples") ?: 90
                if (samples < 1 || samples > MAX_SAMPLES) {
                    throw ApiException.Validation("samples", "expected 1..$MAX_SAMPLES")
                }
                val interval = call.int("intervalSeconds") ?: 1
                if (interval < 1 || interval > 3600) {
                    throw ApiException.Validation("intervalSeconds", "expected 1..3600")
                }
                call.respond(telemetry.metrics(call.int("world"), samples, interval.seconds))
            }

            get("/runtime") {
                call.respond(telemetry.runtime())
            }

            get("/errors") {
                val query = ErrorQuery(
                    world = call.int("world"),
                    level = call.filter("level", ErrorLevel::of),
                    since = call.instant("since"),
                    page = call.page(),
                )
                call.respond(telemetry.errors(query))
            }

            route("/console") {
                get("/log") {
                    val limit = call.limit(default = 90, max = MAX_CONSOLE_LINES)
                    call.respond(ConsoleLog(telemetry.console(call.int("world"), limit)))
                }

                post("/command") {
                    val command = call.receive<ConsoleCommand>()
                    if (command.command.isBlank()) {
                        throw ApiException.Validation("command", "must not be blank")
                    }
                    call.respond(telemetry.run(command, call.session().account))
                }
            }
        }

        // Cancelling the flow is what unsubscribes, and Ktor cancels the handler's scope when the
        // client disconnects — so a closed tab tears the subscription down on its own.
        sse("/dev/metrics/stream") {
            val world = call.int("world")
            telemetry.metricsStream(world).collect { sample ->
                send(ServerSentEvent(data = json.encodeToString(sample), event = "sample"))
            }
        }

        sse("/dev/console/stream") {
            val world = call.int("world")
            telemetry.consoleStream(world).collect { line ->
                send(ServerSentEvent(data = json.encodeToString(line), event = "line"))
            }
        }
    }
}
