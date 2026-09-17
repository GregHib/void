package world.gregs.voidps.web.api.route

import io.ktor.http.Parameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.hiscores.HiscoresService

/**
 * Hiscores and players routes - leaderboards, head-to-head comparison and public profiles,
 * backed by [HiscoresService]. See `web/openapi.yaml` for the full contract.
 */
fun Route.hiscoresRoutes(service: HiscoresService) {
    route("/hiscores") {
        get("/metadata") {
            call.respond(service.metadata())
        }
        get("/overall") {
            val params = call.request.queryParameters
            call.respond(
                service.overall(
                    query = params["q"],
                    mode = params["mode"],
                    page = params.page(),
                    pageSize = params.pageSize(),
                ),
            )
        }
        get("/skills/{skill}") {
            val skillId = call.parameters["skill"] ?: throw ApiException.Validation("skill", "Required")
            val params = call.request.queryParameters
            val result = service.skill(skillId, query = params["q"], mode = params["mode"], page = params.page(), pageSize = params.pageSize())
                ?: throw ApiException.NotFound("skill", skillId)
            call.respond(result)
        }
        route("/bosses/{boss}") {
            get("/kills") {
                val bossId = call.parameters["boss"] ?: throw ApiException.Validation("boss", "Required")
                val params = call.request.queryParameters
                val result = service.bossKills(bossId, mode = params["mode"], page = params.page(), pageSize = params.pageSize())
                    ?: throw ApiException.NotFound("boss", bossId)
                call.respond(result)
            }
            get("/times") {
                val bossId = call.parameters["boss"] ?: throw ApiException.Validation("boss", "Required")
                val params = call.request.queryParameters
                val result = service.bossTimes(bossId, teamSize = params["teamSize"], page = params.page(), pageSize = params.pageSize())
                    ?: throw ApiException.NotFound("boss", bossId)
                call.respond(result)
            }
        }
        get("/compare") {
            val params = call.request.queryParameters
            val playerA = params["playerA"] ?: throw ApiException.Validation("playerA", "Required")
            val playerB = params["playerB"] ?: throw ApiException.Validation("playerB", "Required")
            val result = service.compare(playerA, playerB) ?: throw ApiException.NotFound("player", "$playerA or $playerB")
            call.respond(result)
        }
    }
    route("/players") {
        get("/search") {
            val params = call.request.queryParameters
            val limit = (params["limit"]?.toIntOrNull() ?: 25).coerceIn(1, 50)
            call.respond(mapOf("items" to service.searchPlayers(params["q"], limit)))
        }
        get("/{name}") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            call.respond(service.player(name) ?: throw ApiException.NotFound("player", name))
        }
        get("/{name}/skills") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            call.respond(service.playerSkills(name) ?: throw ApiException.NotFound("player", name))
        }
        get("/{name}/bosses") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            call.respond(service.playerBosses(name) ?: throw ApiException.NotFound("player", name))
        }
        get("/{name}/quests") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            val status = call.request.queryParameters["status"]
            call.respond(service.playerQuests(name, status) ?: throw ApiException.NotFound("player", name))
        }
        get("/{name}/events") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            val params = call.request.queryParameters
            val result = service.playerEvents(name, type = params["type"], page = params.page(), pageSize = params.pageSize())
                ?: throw ApiException.NotFound("player", name)
            call.respond(result)
        }
    }
}

private fun Parameters.page(): Int = (this["page"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

private fun Parameters.pageSize(): Int = (this["pageSize"]?.toIntOrNull() ?: 25).coerceIn(1, 100)
