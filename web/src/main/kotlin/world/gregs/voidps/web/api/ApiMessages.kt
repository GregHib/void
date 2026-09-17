package world.gregs.voidps.web.api

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val name: String,
    val password: String,
    val displayName: String? = null,
)

@Serializable
data class PasswordRequest(
    val password: String,
)

@Serializable
data class RenameRequest(
    val displayName: String,
)

@Serializable
data class AccountResponse(
    val accountName: String,
    val displayName: String,
    val previousName: String,
    val online: Boolean,
) {
    constructor(info: AccountInfo) : this(info.accountName, info.displayName, info.previousName, info.online)
}

@Serializable
data class StatusResponse(
    val name: String,
    val world: Int,
    val worldName: String,
    val revision: Int,
    val players: Int,
    val uptime: Long,
) {
    constructor(status: ServerStatus) : this(status.name, status.world, status.worldName, status.revision, status.players, status.uptime)
}

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
)

internal class ApiError(
    val status: HttpStatusCode,
    val response: ErrorResponse,
)

internal fun AccountResult.error(): ApiError = when (this) {
    AccountResult.SUCCESS -> throw IllegalArgumentException("Success is not an error")
    AccountResult.INVALID_NAME -> ApiError(HttpStatusCode.BadRequest, ErrorResponse("invalid_name", "Account name must be 1-12 letters, numbers or spaces, or a valid email address."))
    AccountResult.INVALID_DISPLAY_NAME -> ApiError(HttpStatusCode.BadRequest, ErrorResponse("invalid_display_name", "Display name must be 1-12 letters, numbers or spaces and is required for email accounts."))
    AccountResult.INVALID_PASSWORD -> ApiError(HttpStatusCode.BadRequest, ErrorResponse("invalid_password", "Password must be 5-20 letters or numbers and not easily guessed."))
    AccountResult.NAME_TAKEN -> ApiError(HttpStatusCode.Conflict, ErrorResponse("name_taken", "That name is already in use."))
    AccountResult.NOT_FOUND -> ApiError(HttpStatusCode.NotFound, ErrorResponse("not_found", "No account with that name."))
    AccountResult.BUSY -> ApiError(HttpStatusCode.Conflict, ErrorResponse("busy", "Account is being saved, try again shortly."))
    AccountResult.REFUSED -> ApiError(HttpStatusCode.Forbidden, ErrorResponse("registration_disabled", "Account creation is disabled."))
    AccountResult.RATE_LIMITED -> ApiError(HttpStatusCode.TooManyRequests, ErrorResponse("too_many_requests", "Too many accounts created from this address, try again later."))
    AccountResult.UNAVAILABLE -> ApiError(HttpStatusCode.ServiceUnavailable, ErrorResponse("unavailable", "The game server could not complete the request."))
}

internal val invalidJson = ApiError(HttpStatusCode.BadRequest, ErrorResponse("invalid_json", "Request body is missing or malformed."))
