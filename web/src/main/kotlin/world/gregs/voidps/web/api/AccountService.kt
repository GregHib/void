package world.gregs.voidps.web.api

/**
 * Account operations exposed over the HTTP api, implemented by the game server
 */
interface AccountService {
    fun status(): ServerStatus

    /**
     * Looks up an account by account name or display name
     */
    fun account(name: String): AccountInfo?

    /**
     * Creates an account for a username or email [name]
     * @param displayName required when [name] is an email address, otherwise must be absent or equal to [name]
     * @param ip address of the end user, used for rate limiting
     */
    suspend fun create(name: String, password: String, displayName: String?, ip: String): AccountResult

    suspend fun password(name: String, password: String): AccountResult

    suspend fun rename(name: String, displayName: String): AccountResult
}

enum class AccountResult {
    SUCCESS,
    INVALID_NAME,
    INVALID_DISPLAY_NAME,
    INVALID_PASSWORD,
    NAME_TAKEN,
    NOT_FOUND,
    BUSY,
    REFUSED,
    RATE_LIMITED,
    UNAVAILABLE,
}

data class ServerStatus(
    val name: String,
    val world: Int,
    val worldName: String,
    val revision: Int,
    val players: Int,
    val uptime: Long,
)

data class AccountInfo(
    val accountName: String,
    val displayName: String,
    val previousName: String,
    val online: Boolean,
)

/**
 * @param token shared secret expected as "Authorization: Bearer <token>"
 */
data class ApiConfig(
    val token: String,
    val accounts: AccountService,
)
