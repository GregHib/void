package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Index

object PrefetchKeyGeneration {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheLibrary("./data/cache/")
        println(cache.archive(Index.DEFAULTS))
        println(cache.native("jaclib"))
        println(cache.native("jaggl"))
        println(cache.native("jagdx"))
        println(cache.native("jagmisc"))
        println(cache.native("sw3d"))
        println(cache.native("hw3d"))
        println(cache.native("jagtheora"))
        println(cache.archive(Index.SHADERS))
        println(cache.archive(Index.TEXTURE_DEFINITIONS))
        println(cache.archive(Index.CONFIGS))
        println(cache.archive(Index.OBJECTS))
        println(cache.archive(Index.ENUMS))
        println(cache.archive(Index.NPCS))
        println(cache.archive(Index.ITEMS))
        println(cache.archive(Index.ANIMATIONS))
        println(cache.archive(Index.GRAPHICS))
        println(cache.archive(Index.VAR_BIT))
        println(cache.archive(Index.QUICK_CHAT_MESSAGES))
        println(cache.archive(Index.QUICK_CHAT_MENUS))
        println(cache.archive(Index.PARTICLES))
        println(cache.archive(Index.BILLBOARDS))
        println(cache.group(Index.HUFFMAN, "huffman"))
        println(cache.archive(Index.INTERFACES))
        println(cache.archive(Index.CLIENT_SCRIPTS))
        println(cache.archive(Index.FONT_METRICS))
        println(cache.group(Index.WORLD_MAP, "details"))
    }

    private fun CacheLibrary.group(index: Int, name: String): Int {
        val idx = index(index)
        val archive = idx.archiveId(name)
        if(archive == -1) {
            return 0
        }
        return (idx.readArchiveSector(archive)?.size ?: 2) - 2
    }

    private fun CacheLibrary.archive(index: Int): Int {
        var total = 0
        index(index).archiveIds().forEach { archive ->
            total += index(index).readArchiveSector(archive)?.size ?: 0
        }
        total += index255?.readArchiveSector(index)?.size ?: 0
        return total
    }

    private fun CacheLibrary.native(name: String): Int {
        return group(Index.NATIVE_LIBRARIES, "windows/x86/$name.dll")
    }
}