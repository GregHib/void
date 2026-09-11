package world.gregs.voidps.web.api.route

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.principal
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.Session
import world.gregs.voidps.web.api.service.AuthService

const val SESSION_COOKIE = "void_session"

/** Routes any signed-in account may call. */
const val SESSION_AUTH = "session"

/** Routes that additionally need a staff rank — everything under `/dev`. */
const val STAFF_AUTH = "staff"

/**
 * Resolves the `void_session` cookie or a bearer token into a [Session] and publishes it as the
 * call's principal.
 *
 * This is a real authentication provider rather than a check inside each handler because the SSE
 * routes need the rejection to happen *before* the handler runs — once an event stream is open its
 * status line has already gone out, and a 401 can no longer be sent.
 */
class SessionAuthenticationProvider internal constructor(config: Config) : AuthenticationProvider(config) {

    private val auth = config.auth
    private val staffOnly = config.staffOnly

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val token = context.call.sessionToken()
        val session = if (token == null) null else auth.session(token)
        if (session == null) {
            context.reject(AuthenticationFailedCause.NoCredentials, ApiException.Unauthorized())
            return
        }
        if (staffOnly && !session.account.staff) {
            context.reject(AuthenticationFailedCause.InvalidCredentials, ApiException.Forbidden())
            return
        }
        context.principal(session)
    }

    private fun AuthenticationContext.reject(cause: AuthenticationFailedCause, error: ApiException) {
        challenge(SESSION_COOKIE, cause) { challenge, call ->
            call.respondError(error)
            challenge.complete()
        }
    }

    class Config internal constructor(name: String?) : AuthenticationProvider.Config(name) {
        /** Resolves tokens. Must be set. */
        lateinit var auth: AuthService

        /** When true, a session without a staff rank is rejected with 403 rather than allowed. */
        var staffOnly: Boolean = false
    }
}

/** Registers a [SessionAuthenticationProvider] under [name]. */
fun AuthenticationConfig.sessionAuth(name: String, configure: SessionAuthenticationProvider.Config.() -> Unit) {
    val config = SessionAuthenticationProvider.Config(name).apply(configure)
    register(SessionAuthenticationProvider(config))
}

/** The bearer token, falling back to the session cookie. */
fun ApplicationCall.sessionToken(): String? {
    val header = request.headers[HttpHeaders.Authorization]
    if (header != null && header.startsWith("Bearer ", ignoreCase = true)) {
        return header.substring(7).trim().ifEmpty { null }
    }
    return request.cookies[SESSION_COOKIE]
}

/**
 * The authenticated session. Only call this inside an `authenticate` block — outside one the
 * principal is never set.
 */
fun ApplicationCall.session(): Session = principal<Session>() ?: throw ApiException.Unauthorized()

/** The session when there is one, for routes that are public but redact less for their owner. */
suspend fun ApplicationCall.optionalSession(auth: AuthService): Session? {
    val token = sessionToken() ?: return null
    return auth.session(token)
}
