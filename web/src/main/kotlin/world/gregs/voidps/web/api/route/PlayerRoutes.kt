package world.gregs.voidps.web.api.route

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import world.gregs.voidps.web.api.model.Items
import world.gregs.voidps.web.api.model.PlayerEventType
import world.gregs.voidps.web.api.model.QuestFilter
import world.gregs.voidps.web.api.model.SkillSort
import world.gregs.voidps.web.api.service.AuthService
import world.gregs.voidps.web.api.service.PlayerService

/**
 * Public player profiles, shared by the adventurer's log and the hiscores player view.
 *
 * These are open routes that still resolve a session when one is present: an account with a
 * private profile stays visible to its owner and to staff, and [PlayerService] needs the viewer to
 * decide that.
 */
fun Route.playerRoutes(players: PlayerService, auth: AuthService) {
    route("/players") {
        get("/search") {
            val results = players.search(call.text("q"), call.limit(default = 25, max = 50))
            call.respond(Items(results))
        }

        route("/{name}") {
            get {
                val viewer = call.optionalSession(auth)?.account
                call.respond(players.profile(call.path("name"), viewer))
            }

            get("/skills") {
                val viewer = call.optionalSession(auth)?.account
                val sort = call.choice("sort", SkillSort.Level, SkillSort::of)
                call.respond(players.skills(call.path("name"), sort, viewer))
            }

            get("/bosses") {
                val viewer = call.optionalSession(auth)?.account
                call.respond(players.bosses(call.path("name"), viewer))
            }

            get("/quests") {
                val viewer = call.optionalSession(auth)?.account
                val status = call.choice("status", QuestFilter.Complete, QuestFilter::of)
                call.respond(players.quests(call.path("name"), status, viewer))
            }

            get("/events") {
                val viewer = call.optionalSession(auth)?.account
                val type = call.filter("type", PlayerEventType::of)
                call.respond(players.events(call.path("name"), type, call.instant("since"), call.page(), viewer))
            }
        }
    }
}
