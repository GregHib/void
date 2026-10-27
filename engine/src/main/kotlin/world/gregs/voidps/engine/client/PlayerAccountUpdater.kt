package world.gregs.voidps.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.rename
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.network.login.AccountNames

enum class AccountUpdate {
    SUCCESS,
    NOT_FOUND,
    INVALID,
    TAKEN,
    BUSY,
    UNAVAILABLE,

    /** Staff accounts can only be changed in-game. */
    PROTECTED,
}

/**
 * Applies external changes (website password resets, display name changes) to accounts whether online or offline.
 * Online players are changed in memory and queued for saving; offline accounts are loaded, changed and saved on [io].
 * Accounts with rights are refused, and a password change logs the player out so any existing session ends.
 */
class PlayerAccountUpdater(
    private val storage: Storage,
    private val accountDefinitions: AccountDefinitions,
    private val saveQueue: SaveQueue,
    private val accounts: AccountManager,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val gameContext: CoroutineDispatcher,
) {
    private val logger = InlineLogger()

    private sealed interface Prepared {
        class Done(val result: AccountUpdate) : Prepared

        class Online(val player: Player) : Prepared

        class Offline(val definition: AccountDefinition) : Prepared
    }

    private interface Change {
        fun validate(definition: AccountDefinition): AccountUpdate = AccountUpdate.SUCCESS

        fun online(player: Player, definition: AccountDefinition)

        fun offline(save: PlayerSave, definition: AccountDefinition): PlayerSave

        fun commit(definition: AccountDefinition)

        val audit: String

        /** Whether an online player is logged out once changed */
        val logout: Boolean
            get() = false
    }

    suspend fun password(accountName: String, passwordHash: String): AccountUpdate = update(
        accountName,
        object : Change {
            override val audit = "password changed"

            override val logout = true

            override fun online(player: Player, definition: AccountDefinition) {
                player.passwordHash = passwordHash
                definition.passwordHash = passwordHash
            }

            override fun offline(save: PlayerSave, definition: AccountDefinition) = save.copy(password = passwordHash)

            override fun commit(definition: AccountDefinition) {
                definition.passwordHash = passwordHash
            }
        },
    )

    suspend fun rename(accountName: String, displayName: String): AccountUpdate = update(
        accountName,
        object : Change {
            override val audit = "renamed to $displayName"

            override fun validate(definition: AccountDefinition): AccountUpdate {
                if (!DisplayNames.valid(displayName)) {
                    return AccountUpdate.INVALID
                }
                if (accountDefinitions.taken(displayName, except = definition.accountName)) {
                    return AccountUpdate.TAKEN
                }
                return AccountUpdate.SUCCESS
            }

            override fun online(player: Player, definition: AccountDefinition) {
                player.rename(displayName)
            }

            override fun offline(save: PlayerSave, definition: AccountDefinition): PlayerSave {
                val history = (save.variables["name_history"] as? List<*>)?.filterIsInstance<String>().orEmpty()
                return save.copy(variables = save.variables + ("display_name" to displayName) + ("name_history" to history + definition.displayName))
            }

            override fun commit(definition: AccountDefinition) {
                accountDefinitions.update(definition.accountName, displayName, definition.displayName)
            }
        },
    )

    private suspend fun update(accountName: String, change: Change): AccountUpdate {
        val name = AccountNames.normalise(accountName)
        val prepared = withContext(gameContext) { prepare(name, change) }
        val definition = when (prepared) {
            is Prepared.Done -> return prepared.result
            is Prepared.Online -> {
                logout(prepared.player, change)
                return AccountUpdate.SUCCESS
            }
            is Prepared.Offline -> prepared.definition
        }
        val result = try {
            withContext(io) {
                val save = storage.load(definition.accountName) ?: return@withContext AccountUpdate.NOT_FOUND
                if (staff(save.variables["rights"])) {
                    return@withContext AccountUpdate.PROTECTED
                }
                storage.save(listOf(change.offline(save, definition)))
                AccountUpdate.SUCCESS
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to update account ${definition.accountName}." }
            return AccountUpdate.UNAVAILABLE
        }
        if (result != AccountUpdate.SUCCESS) {
            return result
        }
        val player = withContext(gameContext) { commit(definition, change) }
        if (player != null) {
            logout(player, change)
        }
        return AccountUpdate.SUCCESS
    }

    /**
     * Validates and applies [change] to an online player, or returns the offline account to update in storage
     */
    private fun prepare(name: String, change: Change): Prepared {
        val definition = accountDefinitions.getByAccount(name) ?: return Prepared.Done(AccountUpdate.NOT_FOUND)
        val player = Players.findByAccount(definition.accountName)
        if (player != null && player.rights != PlayerRights.None) {
            return Prepared.Done(AccountUpdate.PROTECTED)
        }
        val result = change.validate(definition)
        if (result != AccountUpdate.SUCCESS) {
            return Prepared.Done(result)
        }
        if (player != null) {
            apply(player, definition, change)
            return Prepared.Online(player)
        }
        if (saveQueue.saving(definition.accountName)) {
            return Prepared.Done(AccountUpdate.BUSY)
        }
        return Prepared.Offline(definition)
    }

    /**
     * Updates the in-memory definition after an offline save
     * @return the player if they logged in while the save was being written and were changed directly instead
     */
    private fun commit(definition: AccountDefinition, change: Change): Player? {
        // Logged in while the save was being written so their in-memory state is stale
        val player = Players.findByAccount(definition.accountName)
        if (player != null) {
            apply(player, definition, change)
            return player
        }
        change.commit(definition)
        AuditLog.info("${definition.accountName} ${change.audit}")
        return null
    }

    private suspend fun logout(player: Player, change: Change) {
        if (change.logout) {
            withContext(gameContext) { accounts.logout(player, safely = false) }
        }
    }

    private fun staff(rights: Any?): Boolean = (rights as? String ?: "none").lowercase() != "none"

    private fun apply(player: Player, definition: AccountDefinition, change: Change) {
        change.online(player, definition)
        saveQueue.save(player)
        AuditLog.event(player, change.audit)
    }
}
