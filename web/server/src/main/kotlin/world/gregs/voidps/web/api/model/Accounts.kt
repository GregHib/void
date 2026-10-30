package world.gregs.voidps.web.api.model

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
data class NameResponse(
    val name: String,
    val available: Boolean,
)

@Serializable
data class AccountResponse(
    val accountName: String,
    val displayName: String,
    val previousName: String,
    val online: Boolean,
)
