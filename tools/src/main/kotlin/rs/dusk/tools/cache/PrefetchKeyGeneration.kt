package rs.dusk.tools.cache

import com.displee.cache.CacheLibrary
import rs.dusk.cache.Indices

object PrefetchKeyGeneration {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheLibrary("./cache/data/cache/")
        println(cache.archive(Indices.DEFAULTS))
        println(cache.native("jaclib"))
        println(cache.native("jaggl"))
        println(cache.native("jagdx"))
        println(cache.native("jagmisc"))
        println(cache.native("sw3d"))
        println(cache.native("hw3d"))
        println(cache.native("jagtheora"))
        println(cache.archive(Indices.SHADERS))
        println(cache.archive(Indices.TEXTURE_DEFINITIONS))
        println(cache.archive(Indices.CONFIGS))
        println(cache.archive(Indices.OBJECTS))
        println(cache.archive(Indices.ENUMS))
        println(cache.archive(Indices.NPCS))
        println(cache.archive(Indices.ITEMS))
        println(cache.archive(Indices.ANIMATIONS))
        println(cache.archive(Indices.GRAPHICS))
        println(cache.archive(Indices.VAR_BIT))
        println(cache.archive(Indices.QUICK_CHAT_MESSAGES))
        println(cache.archive(Indices.QUICK_CHAT_MENUS))
        println(cache.archive(Indices.PARTICLES))
        println(cache.archive(Indices.BILLBOARDS))
        println(cache.group(Indices.HUFFMAN, "huffman"))
        println(cache.archive(Indices.INTERFACES))
        println(cache.archive(Indices.CLIENT_SCRIPTS))
        println(cache.archive(Indices.FONT_METRICS))
        println(cache.group(Indices.WORLD_MAP, "details"))
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
        return group(Indices.NATIVE_LIBRARIES, "windows/x86/$name.dll")
    }
}