package world.gregs.voidps.web.api

import kotlinx.serialization.Serializable

/**
 * The failures a service is allowed to signal. Implementations throw these; the Ktor layer's
 * `StatusPages` handler is the only thing that knows which HTTP status each maps to, so a service
 * never imports anything from `io.ktor`.
 *
 * [code] is the stable machine-readable string in the error envelope; [message] is prose for a
 * developer, not for display.
 */
sealed class ApiException(
    val code: String,
    message: String,
    val details: List<FieldError> = emptyList(),
) : RuntimeException(message) {

    /** No session, or one that has expired. */
    class Unauthorized(message: String = "Authentication required") : ApiException("unauthorized", message)

    /** A valid session that lacks the rank the route needs. */
    class Forbidden(message: String = "Insufficient privileges") : ApiException("forbidden", message)

    /** Wrong credentials. Deliberately indistinguishable from an unknown account. */
    class InvalidCredentials : ApiException("invalid_credentials", "Unknown account or wrong password")

    /** The account exists but cannot sign in. */
    class AccountLocked(message: String) : ApiException("account_locked", message)

    class NotFound(resource: String, id: String) : ApiException("${resource}_not_found", "No $resource '$id'")

    /** The account opted out of public profiles in its privacy settings. */
    class PrivateProfile(name: String) : ApiException("profile_private", "'$name' has a private profile")

    /** The action needs the account to be online. */
    class PlayerOffline(id: String) : ApiException("player_offline", "'$id' is not online")

    class Conflict(code: String, message: String) : ApiException(code, message)

    /** Well-formed but invalid — [details] names the offending fields. */
    class Validation(message: String, details: List<FieldError>) : ApiException("validation_failed", message, details) {
        constructor(field: String, problem: String) : this("Invalid $field", listOf(FieldError(field, problem)))
    }

    class RateLimited(val retryAfterSeconds: Int) : ApiException("rate_limited", "Too many requests")
}

@Serializable
data class FieldError(val field: String, val message: String)
