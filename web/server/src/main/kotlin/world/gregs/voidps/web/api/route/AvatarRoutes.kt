package world.gregs.voidps.web.api.route

import io.ktor.http.HttpHeaders
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.avatar.AvatarService

/**
 * Photo booth avatars, backed by [AvatarService]:
 *  - `GET /players/{name}/avatar/{type}` - the `full` body or `chat` head png, rendered on first request
 *    and again whenever the player takes a new snapshot.
 *  - `POST /dev/players/{name}/avatar` - forces a re-render of both images.
 */
fun Route.avatarRoutes(service: AvatarService) {
    route("/players/{name}/avatar/{type}") {
        allowAnyOrigin()
        get {
            val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
            val type = call.parameters["type"]?.removeSuffix(".png")
            if (type !in AvatarService.TYPES) {
                throw ApiException.Validation("type", "Must be one of ${AvatarService.TYPES}")
            }
            val file = service.image(name, type!!) ?: throw ApiException.NotFound("avatar", name)
            call.response.header(HttpHeaders.CacheControl, "public, max-age=300")
            call.respondFile(file)
        }
    }
    post("/dev/players/{name}/avatar") {
        val name = call.parameters["name"] ?: throw ApiException.Validation("name", "Required")
        call.respond(service.generate(name) ?: throw ApiException.NotFound("avatar", name))
    }
}
