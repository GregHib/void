package world.gregs.voidps.network.file

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import java.util.*

fun prefetchKeys(cache: Cache, properties: Properties): IntArray {
    val existing = properties["prefetch.keys"] as? String
    return existing?.split(",")?.map { it.toInt() }?.toIntArray() ?: generatePrefetchKeys(cache)
}

/**
 * Generates cache prefetch keys used to determine total cache download percentage
 * Note: Can vary between revisions, compare with your client.
 */
fun generatePrefetchKeys(cache: Cache) = intArrayOf(
    archive(cache, Index.DEFAULTS),
    native(cache, "jaclib"),
    native(cache, "jaggl"),
    native(cache, "jagdx"),
    native(cache, "jagmisc"),
    native(cache, "sw3d"),
    native(cache, "hw3d"),
    native(cache, "jagtheora"),
    archive(cache, Index.SHADERS),
    archive(cache, Index.TEXTURE_DEFINITIONS),
    archive(cache, Index.CONFIGS),
    archive(cache, Index.OBJECTS),
    archive(cache, Index.ENUMS),
    archive(cache, Index.NPCS),
    archive(cache, Index.ITEMS),
    archive(cache, Index.ANIMATIONS),
    archive(cache, Index.GRAPHICS),
    archive(cache, Index.VAR_BIT),
    archive(cache, Index.QUICK_CHAT_MESSAGES),
    archive(cache, Index.QUICK_CHAT_MENUS),
    archive(cache, Index.PARTICLES),
    archive(cache, Index.BILLBOARDS),
    file(cache, Index.HUFFMAN, "huffman"),
    archive(cache, Index.INTERFACES),
    archive(cache, Index.CLIENT_SCRIPTS),
    archive(cache, Index.FONT_METRICS),
    file(cache, Index.WORLD_MAP, "details"),
)

/**
 *  Length of archive with [name] in [index]
 */
fun file(cache: Cache, index: Int, name: String): Int {
    val archive = cache.archiveId(index, name)
    if (archive == -1) {
        return 0
    }
    return (cache.sector(index, archive)?.size ?: 2) - 2
}

/**
 * Length of all archives in [index]
 */
fun archive(cache: Cache, index: Int): Int {
    var total = 0
    for (archive in cache.archives(index)) {
        total += cache.sector(index, archive)?.size ?: 0
    }
    total += cache.sector(255, index)?.size ?: 0
    return total
}

fun native(cache: Cache, name: String) = file(cache, Index.NATIVE_LIBRARIES, "windows/x86/$name.dll")
