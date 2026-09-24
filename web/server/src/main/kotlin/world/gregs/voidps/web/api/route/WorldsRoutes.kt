package world.gregs.voidps.web.api.route

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.web.api.model.ServerInfo
import java.lang.management.ManagementFactory

/**
 * Endpoints every world's site calls on every *other* world's web server to build its world list
 * (see the site's `worlds.js`), so both allow any origin. The static half of each listing lives in
 * the site's `worlds.json`.
 *
 * `/info` reports this world's live state (status, members, players, xp/drop rates, uptime).
 * `/ping` does no work at all — the browser times the round trip itself, so the latency shown is
 * the visitor's own, and it's cheap enough to poll far more often than `/info`.
 */
fun Route.worldsRoutes() {
    get("/info") {
        call.allowAnyOrigin()
        call.respond(info())
    }
    get("/ping") {
        call.allowAnyOrigin()
        call.response.header(HttpHeaders.CacheControl, "no-store")
        call.respond(HttpStatusCode.NoContent)
    }
}

private fun RoutingCall.allowAnyOrigin() {
    response.header(HttpHeaders.AccessControlAllowOrigin, "*")
}

private fun info(): ServerInfo {
    val players = Players.size
    val capacity = Settings["world.players.max", MAX_PLAYERS]
    val status = when {
        World.containsQueue("system_update") -> "Restarting"
        players >= capacity -> "Full"
        else -> "Online"
    }
    return ServerInfo(
        status = status,
        members = World.members,
        players = players,
        capacity = capacity,
        xpRate = Settings["world.experienceRate", 1.0],
        dropRate = Settings["world.itemDropRate", 1.0],
        uptimeSeconds = ManagementFactory.getRuntimeMXBean().uptime / 1000,
    )
}
