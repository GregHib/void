package world.gregs.voidps.web.route

import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticZip
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe
import java.nio.file.Path

internal fun Routing.webclient(port: Int, webclientZip: Path) {
    route("/play") {
        get {
            call.respondHtml {
                head {
                    title("Void")
                    style {
                        +"""
                            html, body { margin: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
                            #client { position: relative; width: 100vw; height: 100vh; overflow: hidden; }
                            canvas { display: block; outline: none; }
                        """
                    }
                    script {
                        unsafe {
                            +"window.CONFIG = { url: \"ws://localhost:${port}/proxy\" };"
                        }
                    }
                    link {
                        rel = "icon"
                        type = "image/x-icon"
                        href = "/play/favicon.ico"
                    }
                }
                body {
                    div {
                        id = "client"
                    }
                    script(src = "/play/void-client.js") {}
                }
            }
        }
        staticZip("", "", webclientZip)
    }
}
