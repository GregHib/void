package world.gregs.voidps.web.api.route

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.AccountMode
import world.gregs.voidps.web.api.model.LeaderboardQuery
import world.gregs.voidps.web.api.service.HiscoreService

/** Boss tables page ten at a time rather than the twenty-five the ranked tables use. */
private const val BOSS_PAGE_SIZE = 10

fun Route.hiscoreRoutes(hiscores: HiscoreService) {
    route("/hiscores") {
        get("/metadata") {
            call.respond(hiscores.metadata())
        }

        get("/overall") {
            val query = LeaderboardQuery(
                name = call.text("q"),
                mode = call.filter("mode", AccountMode::of),
                page = call.page(),
            )
            call.respond(hiscores.overall(query))
        }

        get("/skills/{skill}") {
            val query = LeaderboardQuery(
                name = call.text("q"),
                mode = call.filter("mode", AccountMode::of),
                page = call.page(),
            )
            call.respond(hiscores.skill(call.path("skill"), query))
        }

        get("/bosses/{boss}/kills") {
            val mode = call.filter("mode", AccountMode::of)
            call.respond(hiscores.bossKills(call.path("boss"), mode, call.page(BOSS_PAGE_SIZE)))
        }

        get("/bosses/{boss}/times") {
            val teamSize = call.filter("teamSize") { it?.toIntOrNull() }
            if (teamSize != null && teamSize < 1) {
                throw ApiException.Validation("teamSize", "expected 1 or greater")
            }
            call.respond(hiscores.bossTimes(call.path("boss"), teamSize, call.page(BOSS_PAGE_SIZE)))
        }

        get("/compare") {
            val a = call.text("playerA") ?: throw ApiException.Validation("playerA", "required")
            val b = call.text("playerB") ?: throw ApiException.Validation("playerB", "required")
            call.respond(hiscores.compare(a, b))
        }
    }
}
