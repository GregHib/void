package world.gregs.voidps.web.api.route

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.response.respond
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.ErrorBody
import world.gregs.voidps.web.api.model.ErrorResponse
import world.gregs.voidps.web.api.model.PageRequest
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Query and path parsing. Every helper rejects a malformed value with [ApiException.Validation]
 * rather than silently falling back to a default, so a typo in a link surfaces as a 422 naming the
 * parameter instead of quietly different results.
 */

/** A required path segment. Missing means the route table and the handler disagree. */
fun ApplicationCall.path(name: String): String =
    parameters[name] ?: throw ApiException.Validation(name, "missing path parameter")

/** A trimmed query value, or null when absent or blank. */
fun ApplicationCall.text(name: String): String? = request.queryParameters[name]?.trim()?.ifEmpty { null }

fun ApplicationCall.int(name: String): Int? {
    val raw = text(name) ?: return null
    return raw.toIntOrNull() ?: throw ApiException.Validation(name, "expected a whole number")
}

fun ApplicationCall.boolean(name: String): Boolean? {
    val raw = text(name) ?: return null
    return when (raw.lowercase()) {
        "true" -> true
        "false" -> false
        else -> throw ApiException.Validation(name, "expected true or false")
    }
}

fun ApplicationCall.instant(name: String): Instant? {
    val raw = text(name) ?: return null
    try {
        return Instant.parse(raw)
    } catch (e: DateTimeParseException) {
        throw ApiException.Validation(name, "expected an RFC 3339 timestamp")
    }
}

/** A bounded `limit`, for the endpoints that cap a list rather than paginate it. */
fun ApplicationCall.limit(default: Int, max: Int): Int {
    val value = int("limit") ?: return default
    if (value < 1 || value > max) {
        throw ApiException.Validation("limit", "expected 1..$max")
    }
    return value
}

/** `page` and `pageSize`, bounded so a caller cannot ask for the whole table in one response. */
fun ApplicationCall.page(defaultSize: Int = PageRequest.DEFAULT_SIZE): PageRequest {
    val page = int("page") ?: 0
    if (page < 0) {
        throw ApiException.Validation("page", "expected 0 or greater")
    }
    val size = int("pageSize") ?: defaultSize
    if (size < 1 || size > PageRequest.MAX_SIZE) {
        throw ApiException.Validation("pageSize", "expected 1..${PageRequest.MAX_SIZE}")
    }
    return PageRequest(page, size)
}

/**
 * Resolves an enum-valued filter. `all` and an absent value both mean "no filter", which every
 * such parameter in the spec spells the same way.
 */
fun <T> ApplicationCall.filter(name: String, resolve: (String?) -> T?): T? {
    val raw = text(name) ?: return null
    if (raw.equals("all", ignoreCase = true)) {
        return null
    }
    return resolve(raw) ?: throw ApiException.Validation(name, "unknown value '$raw'")
}

/** Resolves an enum-valued parameter that has no "all" option. */
fun <T> ApplicationCall.choice(name: String, fallback: T, resolve: (String?) -> T?): T {
    val raw = text(name) ?: return fallback
    return resolve(raw) ?: throw ApiException.Validation(name, "unknown value '$raw'")
}

/** A repeated or comma-separated list, e.g. `?ids=a,b,c`. */
fun ApplicationCall.list(name: String, max: Int): List<String> {
    val values = request.queryParameters.getAll(name).orEmpty()
        .flatMap { it.split(',') }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (values.isEmpty()) {
        throw ApiException.Validation(name, "expected at least one value")
    }
    if (values.size > max) {
        throw ApiException.Validation(name, "expected at most $max values")
    }
    return values
}

/** The status each failure answers with. The only place HTTP and [ApiException] meet. */
fun ApiException.status(): HttpStatusCode = when (this) {
    is ApiException.Unauthorized -> HttpStatusCode.Unauthorized
    is ApiException.InvalidCredentials -> HttpStatusCode.Unauthorized
    is ApiException.Forbidden -> HttpStatusCode.Forbidden
    is ApiException.PrivateProfile -> HttpStatusCode.Forbidden
    is ApiException.NotFound -> HttpStatusCode.NotFound
    is ApiException.AccountLocked -> HttpStatusCode.Locked
    is ApiException.PlayerOffline -> HttpStatusCode.Conflict
    is ApiException.Conflict -> HttpStatusCode.Conflict
    is ApiException.Validation -> HttpStatusCode.UnprocessableEntity
    is ApiException.RateLimited -> HttpStatusCode.TooManyRequests
}

suspend fun ApplicationCall.respondError(error: ApiException) {
    if (error is ApiException.RateLimited) {
        response.header(HttpHeaders.RetryAfter, error.retryAfterSeconds.toString())
    }
    val details = error.details.ifEmpty { null }
    respond(error.status(), ErrorResponse(ErrorBody(error.code, error.message ?: error.code, details)))
}
