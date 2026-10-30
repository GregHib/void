package world.gregs.voidps.web.api.route

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.server.sse.SSE
import kotlinx.serialization.json.Json
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.web.account.AccountService
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.ErrorBody
import world.gregs.voidps.web.api.model.ErrorResponse
import world.gregs.voidps.web.avatar.AvatarService
import world.gregs.voidps.web.exchange.ExchangeService
import world.gregs.voidps.web.hiscores.HiscoresService
import java.io.File
import java.security.MessageDigest

/**
 * The JSON dialect the API speaks. Nulls are written rather than omitted because several fields
 * carry meaning when explicitly null — an unranked boss, an empty equipment slot — and dropping
 * them would leave the client unable to tell "absent" from "none".
 */
val apiJson: Json = Json {
    prettyPrint = false
    encodeDefaults = true
    explicitNulls = true
    ignoreUnknownKeys = true
}

/**
 * @param apiKey shared secret trusted backends send as `Authorization: Bearer <key>` to use [accountRoutes], null to disable
 */
fun Application.apiPlugins(json: Json = apiJson, apiKey: String? = null) {
    install(ContentNegotiation) {
        json(json)
    }
    if (apiKey != null) {
        val expected = apiKey.toByteArray()
        val logger = log
        install(Authentication) {
            bearer(API_KEY_AUTH) {
                authenticate { credential ->
                    if (MessageDigest.isEqual(expected, credential.token.toByteArray())) {
                        UserIdPrincipal(API_KEY_AUTH)
                    } else {
                        logger.warn("Rejected api request with invalid key from ${request.origin.remoteHost}")
                        null
                    }
                }
            }
        }
    }
    install(SSE)
    install(StatusPages) {
        // Authentication challenges respond without a body, give them the same envelope as every other error
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(status, ErrorResponse(ErrorBody("unauthorized", "Authentication required")))
        }
        exception<BadRequestException> { call, cause ->
            val message = cause.message ?: "Malformed request"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(ErrorBody("bad_request", message)))
        }
        exception<ApiException> { call, cause ->
            if (cause is ApiException.RateLimited) {
                call.response.header(HttpHeaders.RetryAfter, cause.retryAfterSeconds.toString())
            }
            call.respond(cause.status(), ErrorResponse(ErrorBody(cause.code, cause.message ?: cause.code, cause.details.ifEmpty { null })))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled failure serving ${call.request.local.uri}", cause)
            val body = ErrorResponse(ErrorBody("internal_error", "Something went wrong"))
            call.respond(HttpStatusCode.InternalServerError, body)
        }
    }
}

private fun ApiException.status(): HttpStatusCode = when (this) {
    is ApiException.Unauthorized -> HttpStatusCode.Unauthorized
    is ApiException.Forbidden -> HttpStatusCode.Forbidden
    is ApiException.InvalidCredentials -> HttpStatusCode.Unauthorized
    is ApiException.AccountLocked -> HttpStatusCode.Locked
    is ApiException.NotFound -> HttpStatusCode.NotFound
    is ApiException.PrivateProfile -> HttpStatusCode.Forbidden
    is ApiException.PlayerOffline -> HttpStatusCode.Conflict
    is ApiException.Conflict -> HttpStatusCode.Conflict
    is ApiException.Validation -> HttpStatusCode.UnprocessableEntity
    is ApiException.RateLimited -> HttpStatusCode.TooManyRequests
    is ApiException.Unavailable -> HttpStatusCode.ServiceUnavailable
}

const val API_PATH = "/api/v1"

/** Authentication provider for the shared key given to [apiPlugins]. */
const val API_KEY_AUTH = "api-key"

/**
 * Lets any origin read every response under this route, errors included. The site reads
 * hiscores, exchange and player data from whichever world the visitor has selected, so these are
 * fetched cross-origin from each world's own web server (see the site's `worlds.js`).
 */
fun Route.allowAnyOrigin() {
    install(AllowAnyOrigin)
}

private val AllowAnyOrigin = createRouteScopedPlugin("AllowAnyOrigin") {
    onCall { call ->
        call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
    }
}

/**
 * Mounts every route under [API_PATH]. Public routes sit at the top level; `/account` and `/dev`
 * wrap themselves in the authentication providers registered by [apiPlugins].
 * @param accounts enables the shared key protected [accountRoutes], null to disable
 */
fun Routing.api(storage: Storage, questDefinitions: QuestDefinitions, cache: Cache, accounts: AccountService? = null) {
    val hiscores = HiscoresService(storage, questDefinitions)
    val exchange = ExchangeService(storage)
//    val dev = DevService(storage, hiscores)
    val avatars = AvatarService(
        storage,
        cache,
        directory = File(Settings["web.avatars.path", "./data/avatars/"]),
        size = Settings["web.avatars.size", 192],
        accountName = hiscores::accountName,
    )
    route(API_PATH) {
        hiscoresRoutes(hiscores)
        exchangeRoutes(exchange)
//        devRoutes(dev)
        avatarRoutes(avatars)
        worldsRoutes()
        if (accounts != null) {
            accountRoutes(accounts)
        }
    }
}
