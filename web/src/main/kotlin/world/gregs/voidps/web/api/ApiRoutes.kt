package world.gregs.voidps.web.api

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.security.MessageDigest

internal const val API_AUTH = "api"

/**
 * Installs json and bearer token authentication for the account api
 */
internal fun Application.apiPlugins(token: String) {
    val expected = token.toByteArray()
    val logger = log
    install(ContentNegotiation) {
        json()
    }
    install(Authentication) {
        bearer(API_AUTH) {
            authenticate { credential ->
                if (MessageDigest.isEqual(expected, credential.token.toByteArray())) {
                    UserIdPrincipal(API_AUTH)
                } else {
                    logger.warn("Rejected api request with invalid token")
                    null
                }
            }
        }
    }
}

internal fun Routing.api(accounts: AccountService) {
    authenticate(API_AUTH) {
        route("/api") {
            get("/status") {
                call.respond(StatusResponse(accounts.status()))
            }
            route("/accounts") {
                post {
                    val request = call.body<CreateAccountRequest>() ?: return@post call.respondError(invalidJson)
                    val result = accounts.create(request.name, request.password, request.displayName, call.clientIp())
                    if (result != AccountResult.SUCCESS) {
                        return@post call.respondError(result.error())
                    }
                    val info = accounts.account(request.name) ?: return@post call.respondError(AccountResult.UNAVAILABLE.error())
                    call.respond(HttpStatusCode.Created, AccountResponse(info))
                }
                route("/{name}") {
                    get {
                        val info = accounts.account(call.name()) ?: return@get call.respondError(AccountResult.NOT_FOUND.error())
                        call.respond(AccountResponse(info))
                    }
                    put("/password") {
                        val request = call.body<PasswordRequest>() ?: return@put call.respondError(invalidJson)
                        val result = accounts.password(call.name(), request.password)
                        if (result != AccountResult.SUCCESS) {
                            return@put call.respondError(result.error())
                        }
                        call.respond(HttpStatusCode.NoContent)
                    }
                    put("/name") {
                        val request = call.body<RenameRequest>() ?: return@put call.respondError(invalidJson)
                        val name = call.name()
                        val result = accounts.rename(name, request.displayName)
                        if (result != AccountResult.SUCCESS) {
                            return@put call.respondError(result.error())
                        }
                        val info = accounts.account(name) ?: return@put call.respondError(AccountResult.UNAVAILABLE.error())
                        call.respond(AccountResponse(info))
                    }
                }
            }
        }
    }
}

private fun ApplicationCall.name(): String = parameters["name"] ?: ""

/**
 * End user address forwarded by the (authenticated) caller, falling back to the connection address
 */
internal fun ApplicationCall.clientIp(): String {
    val forwarded = request.header("X-Forwarded-For")?.substringBefore(',')?.trim()
    if (!forwarded.isNullOrEmpty()) {
        return forwarded
    }
    return request.origin.remoteAddress
}

private suspend inline fun <reified T : Any> ApplicationCall.body(): T? = try {
    receive<T>()
} catch (e: BadRequestException) {
    null
} catch (e: ContentTransformationException) {
    null
}

private suspend fun ApplicationCall.respondError(error: ApiError) {
    respond(error.status, error.response)
}
