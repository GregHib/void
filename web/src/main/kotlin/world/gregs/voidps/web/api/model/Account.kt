@file:UseSerializers(InstantSerializer::class)

package world.gregs.voidps.web.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

/** How an account is played. Drives the hiscores account-type filter and the log's badges. */
@Serializable
enum class AccountMode(val wire: String) {
    @SerialName("standard")
    Standard("standard"),

    @SerialName("ironman")
    Ironman("ironman"),

    @SerialName("hardcore")
    Hardcore("hardcore"),

    @SerialName("ultimate")
    Ultimate("ultimate"),
    ;

    companion object {
        /** Resolves a query-string value. `null` and `all` both mean "no filter". */
        fun of(value: String?): AccountMode? = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}

/** Staff ranks, ordered least to most privileged. */
@Serializable
enum class AccountRole(val wire: String) {
    @SerialName("player")
    Player("player"),

    @SerialName("moderator")
    Moderator("moderator"),

    @SerialName("administrator")
    Administrator("administrator"),
}

/**
 * An authenticated session. [token] is also set as the `void_session` cookie; non-browser clients
 * send it as a bearer token instead.
 */
@Serializable
data class Session(
    val token: String,
    val expiresAt: Instant,
    val account: AccountSummary,
)

/**
 * What the nav-bar account dropdown needs, and nothing more. [staff] gates the "Developer panel"
 * entry and every `/dev` route.
 */
@Serializable
data class AccountSummary(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val roles: Set<AccountRole> = setOf(AccountRole.Player),
    val staff: Boolean = roles.any { it != AccountRole.Player },
    val member: Boolean = false,
    val memberUntil: Instant? = null,
) {
    fun hasRole(role: AccountRole): Boolean = roles.contains(role)
}

/**
 * The account management page. Repeats [AccountSummary]'s fields rather than nesting it, because
 * the spec composes the two with `allOf` — one flat object on the wire.
 */
@Serializable
data class Account(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val roles: Set<AccountRole> = setOf(AccountRole.Player),
    val staff: Boolean = roles.any { it != AccountRole.Player },
    val member: Boolean = false,
    val memberUntil: Instant? = null,
    val createdAt: Instant,
    val lastLoginAt: Instant? = null,
    val emailVerified: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val mode: AccountMode = AccountMode.Standard,
    val renameAvailableAt: Instant? = null,
)

@Serializable
data class AccountSettings(
    val privacy: PrivacySettings,
    val notifications: NotificationSettings,
    val preferredWorld: Int? = null,
)

/**
 * [publicAdventurersLog] is the opt-out the log's "Find a log" panel refers to. When false, every
 * `/players/{name}` route answers 403 to anyone but the owner and staff.
 */
@Serializable
data class PrivacySettings(
    val publicAdventurersLog: Boolean = true,
    val publicHiscores: Boolean = true,
    val showOnlineStatus: Boolean = true,
)

@Serializable
data class NotificationSettings(
    val email: Boolean = true,
    val membershipExpiry: Boolean = true,
)

/** Login form input. [username] accepts either a display name or an email address. */
@Serializable
data class Credentials(
    val username: String,
    val password: String,
    val remember: Boolean = false,
)

/** A partial update — a null field is left alone rather than cleared. */
@Serializable
data class AccountUpdate(
    val email: String? = null,
    val displayName: String? = null,
)

@Serializable
data class PasswordChange(
    val currentPassword: String,
    val newPassword: String,
)
