package world.gregs.voidps.web.api.route

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import kotlinx.serialization.builtins.ListSerializer
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.Tile
import world.gregs.voidps.web.api.model.PlayerLocation
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
 * `/players` lists where online players are for the site's world map, leaving out anyone in the
 * wilderness or who has opted out with the `::world_map` command. It's served from a snapshot
 * that only refreshes every [PLAYERS_REFRESH_MS] (see [PlayerSnapshot]).
 */
fun Route.worldsRoutes() {
    get("/info") {
        call.allowAnyOrigin()
        call.respond(info())
    }
    get("/players") {
        call.allowAnyOrigin()
        val snapshot = PlayerSnapshot.current()
        val maxAge = (snapshot.expiresAt - System.currentTimeMillis()).coerceAtLeast(0) / 1000
        call.response.header(HttpHeaders.CacheControl, "public, max-age=$maxAge")
        call.respondText(snapshot.json, ContentType.Application.Json)
    }
    get("/ping") {
        call.allowAnyOrigin()
        call.response.header(HttpHeaders.CacheControl, "no-store")
        call.respond(HttpStatusCode.NoContent)
    }
}

/**
 * `/players` shares its route with the hiscores' `/players/...`, which already allows any origin
 * (see [Route.allowAnyOrigin]); a second copy of the header would make browsers reject it.
 */
private fun RoutingCall.allowAnyOrigin() {
    if (response.headers[HttpHeaders.AccessControlAllowOrigin] == null) {
        response.header(HttpHeaders.AccessControlAllowOrigin, "*")
    }
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

private const val PLAYERS_REFRESH_MS = 30_000L

/**
 * The `/players` response, rebuilt at most once per [PLAYERS_REFRESH_MS] however many requests
 * arrive. That keeps the scan and serialisation off the per-request path, and means everyone sees
 * the same positions for the same window — polling faster than the map does, or spamming refresh,
 * can't track anyone more closely than [PLAYERS_REFRESH_MS] allows.
 */
internal object PlayerSnapshot {
    class Snapshot(val json: String, val expiresAt: Long)

    @Volatile
    private var snapshot = Snapshot("[]", 0)

    fun current(): Snapshot {
        val now = System.currentTimeMillis()
        snapshot.takeIf { now < it.expiresAt }?.let { return it }
        synchronized(this) {
            // Another request may have rebuilt it while this one waited for the lock.
            snapshot.takeIf { now < it.expiresAt }?.let { return it }
            val json = apiJson.encodeToString(playerLocations())
            return Snapshot(json, now + PLAYERS_REFRESH_MS).also { snapshot = it }
        }
    }

    fun clear() {
        snapshot = Snapshot("[]", 0)
    }
}

/**
 * Reads [Players] by index rather than iterating its list, since this runs off the game thread
 * and the list can be modified mid-iteration by logins and logouts.
 */
private fun playerLocations(): List<PlayerLocation> {
    val locations = mutableListOf<PlayerLocation>()
    for (index in 1 until MAX_PLAYERS) {
        val player = Players.indexed(index) ?: continue
        val tile = player.tile
        if (tile == Tile.EMPTY || player["in_wilderness", false] || player["world_map_hidden", false]) {
            continue
        }
        locations.add(PlayerLocation(player.name, tile.x, tile.y, tile.level))
    }
    return locations
}
