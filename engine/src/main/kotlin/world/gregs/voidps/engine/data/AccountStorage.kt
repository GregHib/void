package world.gregs.voidps.engine.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players

/**
 * Changes accounts whether their player is online or offline.
 * Online players are edited in memory and queued for saving; offline accounts are edited in [storage],
 * or in the save queue if their player has only just logged out, and their [definitions] entry is kept in step.
 */
class AccountStorage(
    private val storage: OfflineStorage,
    private val saveQueue: SaveQueue,
    private val definitions: AccountDefinitions,
    private val gameContext: CoroutineDispatcher,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {

    /**
     * Applies [online] to the player on the game thread if they're logged in, otherwise [offline] to their save on [io].
     * Both are needed as a player can log in or out while their save is being written.
     * @return false if no account named [accountName] exists
     */
    suspend fun edit(accountName: String, online: suspend (Player) -> Unit, offline: (PlayerSave) -> PlayerSave): Boolean {
        val name = definitions.getByAccount(accountName)?.accountName ?: return false
        if (withContext(gameContext) { editLive(name, online, offline) }) {
            return true
        }
        val save = withContext(io) { storage.edit(name, offline) } ?: return false
        withContext(gameContext) {
            // Logged in or out while the save was being written so their copy will overwrite it
            if (!editLive(name, online, offline)) {
                update(save)
            }
        }
        return true
    }

    /**
     * Edits an online player or a save still waiting to be written
     */
    private suspend fun editLive(name: String, online: suspend (Player) -> Unit, offline: (PlayerSave) -> PlayerSave): Boolean {
        val player = Players.findByAccount(name)
        if (player != null) {
            online(player)
            saveQueue.save(player)
            update(player.copy())
            return true
        }
        val save = saveQueue.edit(name, offline) ?: return false
        update(save)
        return true
    }

    /**
     * Keeps the definition, which logins are checked against, matching the saved password and display name
     */
    private fun update(save: PlayerSave) {
        val definition = definitions.getByAccount(save.name) ?: return
        definition.passwordHash = save.password
        val displayName = save.variables["display_name"] as? String ?: save.name
        if (displayName != definition.displayName) {
            definitions.update(save.name, displayName, definition.displayName)
        }
    }
}
