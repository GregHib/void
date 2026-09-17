package world.gregs.voidps.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.copy
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.network.login.AccountCreator
import world.gregs.voidps.network.login.Registration
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.network.login.registration.RegistrationValidator

/**
 * Creates accounts registered from the client's login screen.
 * The email address is the account name; a display name is derived from it until the player chooses their own on first login.
 */
class PlayerAccountCreator(
    private val storage: Storage,
    private val accounts: AccountManager,
    private val accountDefinitions: AccountDefinitions,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val gameContext: CoroutineDispatcher,
) : AccountCreator {
    private val logger = InlineLogger()

    override fun available(email: String): Int {
        if (!Settings["accounts.registration", false]) {
            return RegistrationResponse.REFUSED
        }
        if (accountDefinitions.getByAccount(email) != null) {
            return RegistrationResponse.EMAIL_IN_USE
        }
        return RegistrationResponse.SUCCESS
    }

    override suspend fun create(registration: Registration): Int {
        val email = registration.email.lowercase()
        val player = accounts.create(email, registration.passwordHash)
        // Checking and reserving the name together on the game thread stops two requests registering the same email
        val response = withContext(gameContext) {
            val response = available(email)
            if (response == RegistrationResponse.SUCCESS) {
                prepare(player)
                accountDefinitions.add(player)
            }
            response
        }
        if (response != RegistrationResponse.SUCCESS) {
            return response
        }
        try {
            val save = player.copy()
            val created = withContext(io) { storage.create(save) }
            if (!created) {
                withContext(gameContext) {
                    accountDefinitions.remove(email)
                }
                return RegistrationResponse.EMAIL_IN_USE
            }
            AuditLog.info("registered $email from ${registration.hostname}")
            logger.info { "Account registered for $email." }
            return RegistrationResponse.SUCCESS
        } catch (e: Exception) {
            logger.error(e) { "Failed to create account for $email." }
            withContext(gameContext) {
                accountDefinitions.remove(email)
            }
            return RegistrationResponse.UNAVAILABLE
        }
    }

    /**
     * Gives an email registered [player] a placeholder display name from the part before the @ and flags them to choose their own on first login
     * Must be called on the game thread before the player is added to [AccountDefinitions]
     */
    fun prepare(player: Player) {
        val base = DisplayNames.sanitise(RegistrationValidator.localPart(player.accountName))
        player["display_name"] = DisplayNames.unique(base) { accountDefinitions.get(it) != null }
        player["choose_name"] = true
    }
}
