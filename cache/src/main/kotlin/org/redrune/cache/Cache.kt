package org.redrune.cache

import com.displee.cache.CacheLibrary
import com.github.michaelbull.logging.InlineLogger
import org.redrune.utility.constants.GameConstants
import org.redrune.utility.constants.NetworkConstants.Companion.FILE_SERVER_RSA_MODULUS
import org.redrune.utility.constants.NetworkConstants.Companion.FILE_SERVER_RSA_PRIVATE

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
object Cache : CacheLibrary(GameConstants.CACHE_DIRECTORY!!) {

    private val logger = InlineLogger()

    private val versionTable = Cache.generateNewUkeys(FILE_SERVER_RSA_PRIVATE, FILE_SERVER_RSA_MODULUS)

    fun load() {
        logger.info { "Cache read from $path" }
    }

    fun getFile(indexId: Int, archiveId: Int): ByteArray? {
        if (indexId == 255 && archiveId == 255) {
            return versionTable
        }
        val index = if(indexId == 255) index255 else index(indexId)
        if (index == null) {
            logger.warn { "Unable to find valid index for file request [indexId=$indexId, archiveId=$archiveId]}" }
            return null
        }
        val archiveSector = index.readArchiveSector(archiveId)
        if (archiveSector == null) {
            logger.warn { "Unable to read archive sector $archiveId in index $indexId" }
            return null
        }
        return archiveSector.data
    }

    fun valid(index: Int, archive: Int): Boolean {
        if (archive < 0) {
            return false
        }
        if (index != 255) {
            if (!exists(index) || index(index).archive(archive) == null) {
                return false
            }
        } else if (archive != 255) {
            if (!index(index).contains(archive)) {
                return false
            }
        }
        return true
    }

}