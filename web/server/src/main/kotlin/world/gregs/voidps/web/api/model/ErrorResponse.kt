package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable
import world.gregs.voidps.web.api.FieldError

/** The error envelope every non-2xx response carries. */
@Serializable
data class ErrorResponse(val error: ErrorBody)

@Serializable
data class ErrorBody(
    val code: String,
    val message: String,
    val details: List<FieldError>? = null,
)
