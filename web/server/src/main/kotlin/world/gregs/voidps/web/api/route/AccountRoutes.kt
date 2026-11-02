package world.gregs.voidps.web.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.origin
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import world.gregs.voidps.web.account.AccountService
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.CreateAccountRequest
import world.gregs.voidps.web.api.model.NameResponse
import world.gregs.voidps.web.api.model.PasswordRequest
import world.gregs.voidps.web.api.model.RenameRequest

/**
 * Account creation and management for a trusted backend such as a website, backed by [AccountService].
 * Accounts are addressed by account name only; `/names` checks whether a name is free as either kind.
 * Every route needs the shared [API_KEY_AUTH] key and, unlike the public routes, never allows other origins.
 */
fun Route.accountRoutes(service: AccountService) {
    authenticate(API_KEY_AUTH) {
        get("/names/{name}") {
            val name = call.name()
            call.respond(NameResponse(name, service.available(name)))
        }
        route("/accounts") {
            post {
                val request = call.body<CreateAccountRequest>()
                call.respond(HttpStatusCode.Created, service.create(request.name, request.password, request.displayName, call.clientIp()))
            }
            route("/{name}") {
                get {
                    val name = call.name()
                    call.respond(service.account(name) ?: throw ApiException.NotFound("account", name))
                }
                put("/password") {
                    val request = call.body<PasswordRequest>()
                    service.password(call.name(), request.password)
                    call.respond(HttpStatusCode.NoContent)
                }
                put("/name") {
                    val request = call.body<RenameRequest>()
                    call.respond(service.rename(call.name(), request.displayName))
                }
            }
        }
    }
}

private fun ApplicationCall.name(): String = parameters["name"] ?: throw ApiException.Validation("name", "Required")

/**
 * End user address forwarded by the (authenticated) caller, falling back to the connection address
 */
private fun ApplicationCall.clientIp(): String {
    val forwarded = request.header("X-Forwarded-For")?.substringBefore(',')?.trim()
    if (!forwarded.isNullOrEmpty()) {
        return forwarded
    }
    return request.origin.remoteAddress
}

/**
 * Receives the json body, treating a missing or mistyped body like a malformed one
 */
private suspend inline fun <reified T : Any> ApplicationCall.body(): T = try {
    receive<T>()
} catch (e: ContentTransformationException) {
    throw BadRequestException("Request body is missing or malformed", e)
}
