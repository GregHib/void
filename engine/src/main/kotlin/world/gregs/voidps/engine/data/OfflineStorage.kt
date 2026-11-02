package world.gregs.voidps.engine.data

/**
 * Changes accounts which aren't logged in by loading, editing and saving them back to [storage]
 */
class OfflineStorage(private val storage: Storage) {

    /**
     * @return the edited save, or null if no account named [accountName] exists
     */
    fun edit(accountName: String, change: (PlayerSave) -> PlayerSave): PlayerSave? {
        val save = storage.load(accountName) ?: return null
        val edited = change(save)
        storage.save(listOf(edited))
        return edited
    }
}
