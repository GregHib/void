package world.gregs.voidps.web.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.server.sse.SSE
import kotlinx.serialization.json.Json
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.ApiServices
import world.gregs.voidps.web.api.model.ErrorBody
import world.gregs.voidps.web.api.model.ErrorResponse

/** Everything is served under this prefix, matching the `servers` block in `web/openapi.yaml`. */
const val API_PATH = "/api/v1"

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
 * Installs the plugins the API needs. Call once, before [Routing.api].
 *
 * [secureCookies] should be false only for plain-HTTP local development; it controls the `Secure`
 * attribute on the session cookie.
 */
fun Application.apiPlugins(services: ApiServices, json: Json = apiJson) {
    install(ContentNegotiation) {
        json(json)
    }
    install(SSE)
    install(Authentication) {
        sessionAuth(SESSION_AUTH) {
            auth = services.auth
        }
        sessionAuth(STAFF_AUTH) {
            auth = services.auth
            staffOnly = true
        }
    }
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respondError(cause)
        }
        // A malformed body or a field of the wrong type never reaches a service.
        exception<BadRequestException> { call, cause ->
            val message = cause.message ?: "Malformed request"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(ErrorBody("bad_request", message)))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled failure serving ${call.request.local.uri}", cause)
            val body = ErrorResponse(ErrorBody("internal_error", "Something went wrong"))
            call.respond(HttpStatusCode.InternalServerError, body)
        }
    }
}

/**
 * Mounts every route under [API_PATH]. Public routes sit at the top level; `/account` and `/dev`
 * wrap themselves in the authentication providers registered by [apiPlugins].
 */
fun Routing.api(services: ApiServices, json: Json = apiJson, secureCookies: Boolean = true) {
    route(API_PATH) {
        accountRoutes(services.auth, services.accounts, secureCookies)
        hiscoreRoutes(services.hiscores)
        playerRoutes(services.players, services.auth)
        exchangeRoutes(services.exchange)
        worldRoutes(services.worlds)
        devTelemetryRoutes(services.telemetry, json)
        devPlayerRoutes(services.devPlayers)
    }
}
