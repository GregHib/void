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
import world.gregs.voidps.network.login.AccountNames
import world.gregs.voidps.network.login.Registration
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.network.login.registration.RegistrationValidator
import java.util.concurrent.ConcurrentHashMap

/**
 * Creates accounts registered from the client's login screen or the web api.
 * Email account names get a display name derived from the email until the player chooses their own on first login,
 * unless one is supplied; username account names double as the display name.
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
        if (!Settings["accounts.registration", false]) {
            return RegistrationResponse.REFUSED
        }
        return create(registration.email, registration.passwordHash, displayName = null, registration.hostname)
    }

    /**
     * Creates and persists an account regardless of the client registration setting
     * @param displayName explicit display name, or null to derive one from [accountName]
     * @return a [RegistrationResponse] code
     */
    suspend fun create(accountName: String, passwordHash: String, displayName: String?, hostname: String): Int {
        val name = AccountNames.normalise(accountName)
        if (taken(name) || displayName != null && accountDefinitions.get(displayName) != null) {
            return RegistrationResponse.EMAIL_IN_USE
        }
        if (!inFlight.add(name)) {
            return RegistrationResponse.EMAIL_IN_USE
        }
        try {
            val player = accounts.create(name, passwordHash)
            withContext(gameContext) {
                prepare(player, displayName)
                accountDefinitions.add(player)
            }
            val save = player.copy()
            val created = withContext(io) { storage.create(save) }
            if (!created) {
                withContext(gameContext) {
                    accountDefinitions.remove(name)
                }
                return RegistrationResponse.EMAIL_IN_USE
            }
            withContext(gameContext) {
                AuditLog.info("registered $name from $hostname")
            }
            logger.info { "Account registered for $name." }
            return RegistrationResponse.SUCCESS
        } catch (e: Exception) {
            logger.error(e) { "Failed to create account for $name." }
            withContext(gameContext) {
                accountDefinitions.remove(name)
            }
            return RegistrationResponse.UNAVAILABLE
        } finally {
            inFlight.remove(name)
        }
    }

    /**
     * Sets the display name of a new [player] and, when derived from an email, flags them to choose their own on first login
     * Must be called on the game thread before the player is added to [AccountDefinitions]
     */
    fun prepare(player: Player, displayName: String? = null) {
        player["registered"] = epochSeconds()
        if (displayName != null) {
            player["display_name"] = displayName
            return
        }
        if (!AccountNames.isEmail(player.accountName)) {
            player["display_name"] = player.accountName
            return
        }
        val base = DisplayNames.sanitise(RegistrationValidator.localPart(player.accountName))
        player["display_name"] = DisplayNames.unique(base) { accountDefinitions.get(it) != null }
        player["choose_name"] = true
    }

    /**
     * Whether [name] is in use as an account name or display name, or is currently being created
     */
    fun taken(name: String): Boolean = inFlight.contains(name) || accountDefinitions.getByAccount(name) != null || accountDefinitions.get(name) != null || storage.exists(name)
}
