package world.gregs.voidps.web.route

import io.ktor.server.http.content.staticZip
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.nio.file.Path

/**
 * Serves the web client's assets `void-client.js` under `/play/`; the page that
 * hosts them is the site's `play.html` (see `Play.kt` in `web/site`), so a bare `/play` redirects there.
 */
internal fun Routing.webclient(webclientZip: Path) {
    route("/play") {
        get {
            call.respondRedirect("/play.html")
        }
        staticZip("", "", webclientZip)
    }
}
