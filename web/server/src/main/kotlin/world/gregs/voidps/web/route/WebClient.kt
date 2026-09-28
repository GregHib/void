package world.gregs.voidps.web.route

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.http.content.LocalPathContent
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Serves the web client `void-client.js` (and its `.js.map` alongside it, if present) under `/play/`; the page that
 * hosts it is the site's `play.html` (see `Play.kt` in `web/site`), so a bare `/play` redirects there.
 */
internal fun Routing.webclient(webclient: Path) {
    route("/play") {
        get {
            call.respondRedirect("/play.html")
        }
        get("/void-client.js") {
            call.respond(LocalPathContent(webclient, ContentType.Application.JavaScript))
        }
        get("/void-client.js.map") {
            val map = webclient.resolveSibling("${webclient.fileName}.map")
            if (map.exists()) {
                call.respond(LocalPathContent(map, ContentType.Application.Json))
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
