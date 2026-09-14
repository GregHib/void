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
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.login.AccountCreator
import world.gregs.voidps.network.login.Registration
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.network.login.registration.RegistrationValidator
import java.util.concurrent.ConcurrentHashMap

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
    private val inFlight: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override fun available(email: String): Int {
        if (!Settings["accounts.registration", false]) {
            return RegistrationResponse.REFUSED
        }
        if (taken(email)) {
            return RegistrationResponse.EMAIL_IN_USE
        }
        return RegistrationResponse.SUCCESS
    }

    override suspend fun create(registration: Registration): Int {
        val email = registration.email.lowercase()
        val response = available(email)
        if (response != RegistrationResponse.SUCCESS) {
            return response
        }
        if (!inFlight.add(email)) {
            return RegistrationResponse.EMAIL_IN_USE
        }
        try {
            val player = accounts.create(email, registration.passwordHash)
            withContext(gameContext) {
                prepare(player)
                accountDefinitions.add(player)
            }
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
        } finally {
            inFlight.remove(email)
        }
    }

    /**
     * Gives an email registered [player] a unique placeholder display name and flags them to choose their own on first login
     * Must be called on the game thread before the player is added to [AccountDefinitions]
     */
    fun prepare(player: Player) {
        val base = DisplayNames.sanitise(RegistrationValidator.localPart(player.accountName))
        val displayName = DisplayNames.unique(base) { accountDefinitions.get(it) != null }
        player["display_name"] = displayName
        player["choose_name"] = true
        player["registered"] = epochSeconds()
    }

    private fun taken(email: String): Boolean = inFlight.contains(email) || accountDefinitions.getByAccount(email) != null || storage.exists(email)
}
