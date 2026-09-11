package world.gregs.voidps.web.api.route

import io.ktor.http.CookieEncoding
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import world.gregs.voidps.web.api.model.AccountSettings
import world.gregs.voidps.web.api.model.AccountUpdate
import world.gregs.voidps.web.api.model.Credentials
import world.gregs.voidps.web.api.model.PasswordChange
import world.gregs.voidps.web.api.model.Session
import world.gregs.voidps.web.api.service.AccountService
import world.gregs.voidps.web.api.service.AuthService

/**
 * Sign-in and the account dropdown's two links. `/auth/session` is what the nav-bar calls on every
 * page load to choose between the "Log in" button and the account icon.
 */
fun Route.accountRoutes(auth: AuthService, accounts: AccountService, secureCookies: Boolean) {
    route("/auth") {
        post("/login") {
            val session = auth.login(call.receive<Credentials>())
            call.setSessionCookie(session, secureCookies)
            call.respond(session)
        }

        post("/logout") {
            val token = call.sessionToken()
            if (token != null) {
                auth.logout(token)
            }
            call.clearSessionCookie(secureCookies)
            call.respond(HttpStatusCode.NoContent)
        }

        authenticate(SESSION_AUTH) {
            get("/session") {
                call.respond(call.session())
            }

            post("/refresh") {
                val session = auth.refresh(call.session().token)
                call.setSessionCookie(session, secureCookies)
                call.respond(session)
            }
        }
    }

    authenticate(SESSION_AUTH) {
        route("/account") {
            get {
                call.respond(accounts.account(call.session().account.id))
            }

            patch {
                val update = call.receive<AccountUpdate>()
                call.respond(accounts.update(call.session().account.id, update))
            }

            post("/password") {
                accounts.changePassword(call.session().account.id, call.receive<PasswordChange>())
                call.respond(HttpStatusCode.NoContent)
            }

            get("/settings") {
                call.respond(accounts.settings(call.session().account.id))
            }

            put("/settings") {
                val settings = call.receive<AccountSettings>()
                call.respond(accounts.updateSettings(call.session().account.id, settings))
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.setSessionCookie(session: Session, secure: Boolean) {
    response.cookies.append(
        name = SESSION_COOKIE,
        value = session.token,
        encoding = CookieEncoding.RAW,
        maxAge = maxOf(0, session.expiresAt.epochSecond - System.currentTimeMillis() / 1000),
        path = "/",
        secure = secure,
        httpOnly = true,
        extensions = mapOf("SameSite" to "Lax"),
    )
}

private fun io.ktor.server.application.ApplicationCall.clearSessionCookie(secure: Boolean) {
    response.cookies.append(
        name = SESSION_COOKIE,
        value = "",
        encoding = CookieEncoding.RAW,
        maxAge = 0,
        path = "/",
        secure = secure,
        httpOnly = true,
        extensions = mapOf("SameSite" to "Lax"),
    )
}
