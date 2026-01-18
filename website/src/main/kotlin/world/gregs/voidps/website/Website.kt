package world.gregs.voidps.website

import com.github.michaelbull.logging.InlineLogger
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import world.gregs.voidps.website.admin.adminRoutes
import world.gregs.voidps.website.public.publicRoutes
import world.gregs.voidps.website.auth.authRoutes
import world.gregs.voidps.website.auth.playerRoutes
import kotlinx.serialization.Serializable
import io.ktor.server.sessions.*

object Website {
    private val logger = InlineLogger()

    fun start() {
        logger.info { "Starting website on port 8080..." }
        embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
            install(Sessions) {
                cookie<UserSession>("user_session") {
                    cookie.path = "/"
                    cookie.maxAgeInSeconds = 3600
                }
            }
            configureRouting()
        }.start(wait = false)
    }

    private fun Application.configureRouting() {
        routing {
            publicRoutes()
            adminRoutes()
            authRoutes()
            playerRoutes()
        }
    }
}

@Serializable
data class UserSession(val username: String, val rights: String)
