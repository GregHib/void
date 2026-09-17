import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.AccountUpdate
import world.gregs.voidps.engine.client.PlayerAccountCreator
import world.gregs.voidps.engine.client.PlayerAccountUpdater
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.login.AccountNames
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.network.login.registration.RegistrationValidator
import world.gregs.voidps.web.api.AccountInfo
import world.gregs.voidps.web.api.AccountResult
import world.gregs.voidps.web.api.AccountService
import world.gregs.voidps.web.api.ServerStatus

/**
 * Bridges the web api onto the engine's account creation and update services
 */
class WebAccountService(
    private val creator: PlayerAccountCreator,
    private val updater: PlayerAccountUpdater,
    private val definitions: AccountDefinitions,
    private val limiter: RegistrationLimiter,
    private val clock: () -> Long = System::currentTimeMillis,
) : AccountService {
    private val started = clock()

    override fun status() = ServerStatus(
        name = Settings["server.name", "Void"],
        world = Settings.world,
        worldName = Settings.worldName,
        revision = Settings["server.revision", 0],
        players = Players.size,
        uptime = (clock() - started) / 1000L,
    )

    override fun account(name: String): AccountInfo? {
        val definition = definitions.getByAccount(AccountNames.normalise(name)) ?: definitions.get(normalise(name)) ?: return null
        return AccountInfo(definition.accountName, definition.displayName, definition.previousName, Players.findByAccount(definition.accountName) != null)
    }

    override suspend fun create(name: String, password: String, displayName: String?, ip: String): AccountResult {
        if (!Settings["web.api.registration", true]) {
            return AccountResult.REFUSED
        }
        val email = AccountNames.isEmail(name)
        val accountName = if (email) name.lowercase() else normalise(name)
        val display = displayName?.let { normalise(it) }
        if (email && RegistrationValidator.email(accountName) != RegistrationResponse.SUCCESS) {
            return AccountResult.INVALID_NAME
        }
        if (!email && !DisplayNames.valid(accountName)) {
            return AccountResult.INVALID_NAME
        }
        if (email && (display == null || !DisplayNames.valid(display))) {
            return AccountResult.INVALID_DISPLAY_NAME
        }
        if (!email && display != null && display != accountName) {
            return AccountResult.INVALID_DISPLAY_NAME
        }
        if (RegistrationValidator.password(password, accountName) != RegistrationResponse.SUCCESS) {
            return AccountResult.INVALID_PASSWORD
        }
        // Only well-formed requests count towards the limit so typos don't lock a player out
        if (!limiter.allow(ip)) {
            return AccountResult.RATE_LIMITED
        }
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        return when (creator.create(accountName, hash, if (email) display else null, ip)) {
            RegistrationResponse.SUCCESS -> AccountResult.SUCCESS
            RegistrationResponse.EMAIL_IN_USE -> AccountResult.NAME_TAKEN
            RegistrationResponse.REFUSED -> AccountResult.REFUSED
            else -> AccountResult.UNAVAILABLE
        }
    }

    override suspend fun password(name: String, password: String): AccountResult {
        val accountName = AccountNames.normalise(name)
        if (RegistrationValidator.password(password, accountName) != RegistrationResponse.SUCCESS) {
            return AccountResult.INVALID_PASSWORD
        }
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        return updater.password(accountName, hash).result()
    }

    override suspend fun rename(name: String, displayName: String): AccountResult = updater.rename(AccountNames.normalise(name), normalise(displayName)).result()

    private fun AccountUpdate.result(): AccountResult = when (this) {
        AccountUpdate.SUCCESS -> AccountResult.SUCCESS
        AccountUpdate.NOT_FOUND -> AccountResult.NOT_FOUND
        AccountUpdate.INVALID -> AccountResult.INVALID_DISPLAY_NAME
        AccountUpdate.TAKEN -> AccountResult.NAME_TAKEN
        AccountUpdate.BUSY -> AccountResult.BUSY
        AccountUpdate.UNAVAILABLE -> AccountResult.UNAVAILABLE
    }

    private companion object {
        private val spaces = Regex(" +")

        /**
         * Underscores and dashes are shown as spaces in display names, matching the client's name entry
         */
        fun normalise(name: String): String = name.replace('_', ' ').replace('-', ' ').replace(spaces, " ").trim()
    }
}
