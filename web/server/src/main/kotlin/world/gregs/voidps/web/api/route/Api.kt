package world.gregs.voidps.web.api.route

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.server.sse.SSE
import kotlinx.serialization.json.Json
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.ErrorBody
import world.gregs.voidps.web.api.model.ErrorResponse
import world.gregs.voidps.web.dev.DevService
import world.gregs.voidps.web.exchange.ExchangeService
import world.gregs.voidps.web.hiscores.HiscoresService

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

fun Application.apiPlugins(json: Json = apiJson) {
    install(ContentNegotiation) {
        json(json)
    }
    install(SSE)
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            val message = cause.message ?: "Malformed request"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(ErrorBody("bad_request", message)))
        }
        exception<ApiException> { call, cause ->
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
}

const val API_PATH = "/api/v1"

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
 */
fun Routing.api(storage: Storage, questDefinitions: QuestDefinitions) {
    val hiscores = HiscoresService(storage, questDefinitions)
    val exchange = ExchangeService(storage)
    val dev = DevService(storage, hiscores)
    route(API_PATH) {
        hiscoresRoutes(hiscores)
        exchangeRoutes(exchange)
        devRoutes(dev)
        worldsRoutes()
    }
}
