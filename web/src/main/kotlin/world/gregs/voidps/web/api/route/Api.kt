package world.gregs.voidps.web.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.server.sse.SSE
import kotlinx.serialization.json.Json
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.web.api.model.ErrorBody
import world.gregs.voidps.web.api.model.ErrorResponse


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
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled failure serving ${call.request.local.uri}", cause)
            val body = ErrorResponse(ErrorBody("internal_error", "Something went wrong"))
            call.respond(HttpStatusCode.InternalServerError, body)
        }
    }
}

const val API_PATH = "/api/v1"

/**
 * Mounts every route under [API_PATH]. Public routes sit at the top level; `/account` and `/dev`
 * wrap themselves in the authentication providers registered by [apiPlugins].
 */
fun Routing.api(storage: Storage) {
    route(API_PATH) {
        // TODO
    }
}
