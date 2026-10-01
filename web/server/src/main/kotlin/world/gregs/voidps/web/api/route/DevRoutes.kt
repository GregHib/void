package world.gregs.voidps.web.api.route

import io.ktor.http.Parameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.dev.DevService

/**
 * Staff-only developer panel routes - player lookup and inspection, backed by [DevService].
 * Doesn't wrap itself in an auth check yet: see the doc comment on [Routing.api][api].
 */
fun Route.devRoutes(service: DevService) {
    route("/dev/players") {
        get("/search") {
            val params = call.request.queryParameters
            val limit = (params["limit"]?.toIntOrNull() ?: 25).coerceIn(1, 50)
            call.respond(service.searchPlayers(params["q"], limit))
        }
        get("/{name}") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            call.respond(service.overview(name) ?: throw ApiException.NotFound("player", name))
        }
        get("/{name}/skills") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            call.respond(service.skills(name) ?: throw ApiException.NotFound("player", name))
        }
        get("/{name}/inventories") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            call.respond(service.inventories(name) ?: throw ApiException.NotFound("player", name))
        }
        get("/{name}/variables") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            val query = call.request.queryParameters["q"]
            call.respond(service.variables(name, query) ?: throw ApiException.NotFound("player", name))
        }
        get("/{name}/events") {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            val params = call.request.queryParameters
            val result = service.events(name, page = params.page(), pageSize = params.pageSize())
                ?: throw ApiException.NotFound("player", name)
            call.respond(result)
        }
    }
}

private fun Parameters.page(): Int = (this["page"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

private fun Parameters.pageSize(): Int = (this["pageSize"]?.toIntOrNull() ?: 25).coerceIn(1, 100)
