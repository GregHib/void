package world.gregs.voidps.web.avatar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.tools.avatar.PhotoBooth
import world.gregs.voidps.tools.avatar.PhotoSnapshot
import world.gregs.voidps.tools.avatar.SnapshotRepository
import world.gregs.voidps.web.api.model.AvatarRender
import java.io.File
import java.time.Instant

/**
 * Renders photo booth avatars (see Iconis.saveSnapshot) into [directory] and serves them from there.
 * A stored image is reused until the player takes a newer snapshot, so each look is only rendered once.
 *
 * The [PhotoBooth] decodes every item, identity kit and animation definition, so it's only built
 * the first time an avatar actually needs rendering.
 */
class AvatarService(
    storage: Storage,
    cache: Cache,
    private val directory: File,
    private val size: Int,
    /** Resolves a display or account name to the account name saves are keyed by. */
    private val accountName: (String) -> String? = { it },
) {
    private val snapshots = SnapshotRepository(storage)
    private val booth by lazy { PhotoBooth(cache) }

    /**
     * The [type] image for [name], rendering it first if it's missing or older than their latest snapshot.
     * Null if the player doesn't exist, has never used the booth or has nothing to render.
     */
    suspend fun image(name: String, type: String): File? = withContext(Dispatchers.IO) {
        val account = accountName(name) ?: name
        val snapshot = snapshots.load(account) ?: return@withContext null
        val file = PhotoBooth.file(directory, account, type)
        if (file.exists() && file.lastModified() >= snapshot.time * 1000) {
            return@withContext file
        }
        render(account, snapshot) ?: return@withContext null
        file.takeIf { it.exists() }
    }

    /** Re-renders [name]'s avatar images regardless of what's already on disk. */
    suspend fun generate(name: String): AvatarRender? = withContext(Dispatchers.IO) {
        val account = accountName(name) ?: name
        val snapshot = snapshots.load(account) ?: return@withContext null
        render(account, snapshot)
    }

    private fun render(account: String, snapshot: PhotoSnapshot): AvatarRender? {
        val avatar = booth.render(snapshot, size) ?: return null
        for ((type, image) in listOf(PhotoBooth.FULL to avatar.full, PhotoBooth.CHAT to avatar.chat)) {
            val file = PhotoBooth.file(directory, account, type)
            if (image != null) PhotoBooth.write(image, file) else file.delete()
        }
        return AvatarRender(
            name = account,
            full = avatar.full != null,
            chat = avatar.chat != null,
            snapshotAt = Instant.ofEpochSecond(snapshot.time).toString(),
            renderedAt = Instant.now().toString(),
        )
    }

    companion object {
        val TYPES = setOf(PhotoBooth.FULL, PhotoBooth.CHAT)
    }
}
