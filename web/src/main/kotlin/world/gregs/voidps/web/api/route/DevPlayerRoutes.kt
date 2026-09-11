package world.gregs.voidps.web.api.route

import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.acceptItems
import io.ktor.server.request.receive
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import world.gregs.voidps.web.api.model.ChatChannel
import world.gregs.voidps.web.api.model.DevPlayerQuery
import world.gregs.voidps.web.api.model.EquipmentSlot
import world.gregs.voidps.web.api.model.KickRequest
import world.gregs.voidps.web.api.model.ModerationRequest
import world.gregs.voidps.web.api.model.PlayerState
import world.gregs.voidps.web.api.model.TeleportRequest
import world.gregs.voidps.web.api.model.VariableScope
import world.gregs.voidps.web.api.service.DevPlayerService

/** The player workbench. Staff only, and every write here is audited by the service. */
fun Route.devPlayerRoutes(players: DevPlayerService) {
    authenticate(STAFF_AUTH) {
        route("/dev/players") {
            get {
                val query = DevPlayerQuery(
                    text = call.text("q"),
                    state = call.filter("state", PlayerState::of),
                    world = call.int("world"),
                    page = call.page(),
                )
                call.respond(players.search(query))
            }

            route("/{playerId}") {
                get {
                    call.respond(players.player(call.path("playerId"), call.session().account))
                }

                get("/skills") {
                    call.respond(players.skills(call.path("playerId")))
                }

                get("/equipment") {
                    call.respond(EquipmentSlots(players.equipment(call.path("playerId"))))
                }

                get("/inventory") {
                    call.respond(players.inventory(call.path("playerId")))
                }

                get("/bank") {
                    call.respond(players.bank(call.path("playerId"), call.int("tab"), call.page()))
                }

                get("/variables") {
                    val scope = call.filter("scope", VariableScope::of)
                    call.respond(players.variables(call.path("playerId"), call.text("q"), scope))
                }

                get("/activity") {
                    call.respond(players.activity(call.path("playerId"), call.instant("since"), call.page()))
                }

                get("/audit") {
                    call.respond(players.audit(call.path("playerId"), call.page()))
                }

                get("/chat") {
                    val channel = call.filter("channel", ChatChannel::of)
                    val since = call.instant("since")
                    val viewer = call.session().account
                    call.respond(players.chat(call.path("playerId"), channel, since, call.page(), viewer))
                }

                get("/debug-dump") {
                    val dump = players.debugDump(call.path("playerId"), call.session().account)
                    // The Copy button wants the human-readable form; anything else gets the JSON
                    // the spec's `application/json` variant describes.
                    if (call.prefersPlainText()) {
                        call.respondText(dump, ContentType.Text.Plain)
                        return@get
                    }
                    call.respond(DebugDump(dump))
                }

                post("/kick") {
                    val request = call.receiveNullable<KickRequest>() ?: KickRequest()
                    val accepted = players.kick(call.path("playerId"), request.reason, call.session().account)
                    call.respond(HttpStatusCode.Accepted, accepted)
                }

                post("/teleport") {
                    val request = call.receive<TeleportRequest>()
                    val accepted = players.teleport(call.path("playerId"), request, call.session().account)
                    call.respond(HttpStatusCode.Accepted, accepted)
                }

                post("/follow") {
                    val accepted = players.follow(call.path("playerId"), call.session().account)
                    call.respond(HttpStatusCode.Accepted, accepted)
                }

                delete("/follow") {
                    players.unfollow(call.path("playerId"), call.session().account)
                    call.respond(HttpStatusCode.NoContent)
                }

                get("/moderation") {
                    call.respond(players.moderation(call.path("playerId"), call.session().account))
                }

                post("/moderation") {
                    val request = call.receive<ModerationRequest>()
                    val entry = players.moderate(call.path("playerId"), request, call.session().account)
                    call.respond(HttpStatusCode.Created, entry)
                }
            }
        }
    }
}

private fun ApplicationCall.prefersPlainText(): Boolean {
    val accepted = request.acceptItems()
    val plain = accepted.indexOfFirst { it.value == ContentType.Text.Plain.toString() }
    if (plain == -1) {
        return false
    }
    val json = accepted.indexOfFirst { it.value == ContentType.Application.Json.toString() }
    return json == -1 || plain < json
}

/** The `{ "slots": [...] }` envelope the equipment response uses. */
@Serializable
private data class EquipmentSlots(val slots: List<EquipmentSlot>)

/** The JSON variant of a debug dump, for callers that did not ask for plain text. */
@Serializable
private data class DebugDump(val dump: String)
