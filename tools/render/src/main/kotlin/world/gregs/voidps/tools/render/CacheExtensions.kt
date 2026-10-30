package world.gregs.voidps.tools.render

import world.gregs.voidps.cache.Cache

/**
 * Whether [file] of [archive] is present in [index]; stands in for the
 * client's js5 "is this file downloaded yet" checks, which always succeed
 * for a local cache as long as the file exists.
 */
internal fun Cache.exists(index: Int, archive: Int, file: Int = 0): Boolean = archive >= 0 && file >= 0 && file <= lastFileId(index, archive)
