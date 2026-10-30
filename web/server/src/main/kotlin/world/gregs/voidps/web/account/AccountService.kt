package world.gregs.voidps.web.account

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rename
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.network.login.AccountNames
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.network.login.registration.RegistrationValidator
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.AccountResponse

/**
 * Creates and changes accounts on behalf of a trusted backend such as a website, applying changes to the
 * running server so they take effect without an account reload.
 */
class AccountService(
    private val loader: PlayerAccountLoader,
    private val storage: AccountStorage,
    private val accounts: AccountManager,
    private val definitions: AccountDefinitions,
    private val limiter: RegistrationLimiter,
) {

    /**
     * Looks up an account by its account name, never its display name, so a request can only ever reach one account
     */
    fun account(name: String): AccountResponse? {
        val definition = definitions.getByAccount(accountName(name)) ?: return null
        return AccountResponse(definition.accountName, definition.displayName, definition.previousName, Players.findByAccount(definition.accountName) != null)
    }

    /**
     * Whether [name] is free to use as a new account name or display name
     */
    fun available(name: String): Boolean {
        val accountName = accountName(name)
        validateName(accountName)
        return !definitions.taken(accountName)
    }

    /**
     * Creates an account for a username or email [name]
     * @param displayName required when [name] is an email address, otherwise must be absent or equal to [name]
     * @param ip address of the end user, used for rate limiting
     */
    suspend fun create(name: String, password: String, displayName: String?, ip: String): AccountResponse {
        if (!Settings["web.api.registration", true]) {
            throw ApiException.Forbidden("Account creation is disabled.")
        }
        val email = AccountNames.isEmail(name)
        val accountName = accountName(name)
        val display = displayName?.let { normalise(it) }
        validateName(accountName)
        if (email && (display == null || !DisplayNames.valid(display)) || !email && display != null && display != accountName) {
            throw ApiException.Validation("displayName", "Display name must be 1-12 letters, numbers or spaces and is required for email accounts.")
        }
        validatePassword(password, accountName)
        // Only well-formed requests count towards the limit so typos don't lock a player out
        if (!limiter.allow(ip)) {
            throw ApiException.RateLimited(retryAfterSeconds = ((limiter.remaining(ip) + 999) / 1000).toInt().coerceAtLeast(1))
        }
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        when (loader.create(accountName, hash, if (email) display else null, ip)) {
            RegistrationResponse.SUCCESS -> return account(accountName) ?: throw unavailable()
            RegistrationResponse.EMAIL_IN_USE -> throw nameTaken()
            RegistrationResponse.REFUSED -> throw ApiException.Forbidden("Account creation is disabled.")
            else -> throw unavailable()
        }
    }

    /**
     * Sets the password of account [name]; an online player is logged out so their session doesn't outlive the reset
     */
    suspend fun password(name: String, password: String) {
        val accountName = existing(name)
        validatePassword(password, accountName)
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        val edited = storage.edit(
            accountName,
            online = { player ->
                protect(player)
                player.passwordHash = hash
                accounts.logout(player, safely = false)
            },
            offline = { save ->
                protect(save)
                save.copy(password = hash)
            },
        )
        if (!edited) {
            throw ApiException.NotFound("account", name)
        }
        AuditLog.info("$accountName password changed")
    }

    /**
     * Changes the display name of account [name]
     */
    suspend fun rename(name: String, displayName: String): AccountResponse {
        val accountName = existing(name)
        val display = normalise(displayName)
        if (!DisplayNames.valid(display)) {
            throw ApiException.Validation("displayName", "Display name must be 1-12 letters, numbers or spaces.")
        }
        if (definitions.taken(display, except = accountName)) {
            throw nameTaken()
        }
        val edited = storage.edit(
            accountName,
            online = { player ->
                protect(player)
                if (player.name != display) {
                    player.rename(display)
                }
            },
            offline = { save ->
                protect(save)
                save.rename(display)
            },
        )
        if (!edited) {
            throw ApiException.NotFound("account", name)
        }
        AuditLog.info("$accountName renamed to $display")
        return account(accountName) ?: throw unavailable()
    }

    /**
     * The stored account name of account [name], which may differ in case or spacing
     */
    private fun existing(name: String): String = definitions.getByAccount(accountName(name))?.accountName ?: throw ApiException.NotFound("account", name)

    private fun validateName(accountName: String) {
        val valid = if (AccountNames.isEmail(accountName)) RegistrationValidator.email(accountName) == RegistrationResponse.SUCCESS else DisplayNames.valid(accountName)
        if (!valid) {
            throw ApiException.Validation("name", "Name must be 1-12 letters, numbers or spaces, or a valid email address.")
        }
    }

    private fun validatePassword(password: String, accountName: String) {
        if (RegistrationValidator.password(password, accountName) != RegistrationResponse.SUCCESS) {
            throw ApiException.Validation("password", "Password must be 5-20 letters or numbers and not easily guessed.")
        }
    }

    /**
     * Staff accounts can only be changed in-game
     */
    private fun protect(player: Player) = protect(player.rights != PlayerRights.None)

    private fun protect(save: PlayerSave) = protect((save.variables["rights"] as? String ?: "none") != "none")

    private fun protect(staff: Boolean) {
        if (staff) {
            throw ApiException.Forbidden("Staff accounts can only be changed in-game.")
        }
    }

    /**
     * The offline equivalent of [Player.rename]
     */
    private fun PlayerSave.rename(displayName: String): PlayerSave {
        val previous = variables["display_name"] as? String ?: name
        val history = (variables["name_history"] as? List<*>)?.filterIsInstance<String>().orEmpty()
        return copy(variables = variables + ("display_name" to displayName) + ("name_history" to history + previous))
    }

    private fun nameTaken() = ApiException.Conflict("name_taken", "That name is already in use.")

    private fun unavailable() = ApiException.Unavailable("The game server could not complete the request.")

    private companion object {
        private val spaces = Regex(" +")

        /**
         * Underscores and dashes are shown as spaces in display names, matching the client's name entry
         */
        fun normalise(name: String): String = name.replace('_', ' ').replace('-', ' ').replace(spaces, " ").trim()

        /**
         * Emails are compared in lowercase, usernames get the same spacing as display names
         */
        fun accountName(name: String): String = if (AccountNames.isEmail(name)) name.lowercase() else normalise(name)
    }
}
