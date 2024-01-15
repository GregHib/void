package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Index

object PrefetchKeyGeneration {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheLibrary("./data/cache/")
        print(cache)
    }

    fun print(cache: CacheLibrary) {
        print("prefetchKeys=")
        print("${cache.archive(Index.DEFAULTS)},")
        print("${cache.native("jaclib")},")
        print("${cache.native("jaggl")},")
        print("${cache.native("jagdx")},")
        print("${cache.native("jagmisc")},")
        print("${cache.native("sw3d")},")
        print("${cache.native("hw3d")},")
        print("${cache.native("jagtheora")},")
        print("${cache.archive(Index.SHADERS)},")
        print("${cache.archive(Index.TEXTURE_DEFINITIONS)},")
        print("${cache.archive(Index.CONFIGS)},")
        print("${cache.archive(Index.OBJECTS)},")
        print("${cache.archive(Index.ENUMS)},")
        print("${cache.archive(Index.NPCS)},")
        print("${cache.archive(Index.ITEMS)},")
        print("${cache.archive(Index.ANIMATIONS)},")
        print("${cache.archive(Index.GRAPHICS)},")
        print("${cache.archive(Index.VAR_BIT)},")
        print("${cache.archive(Index.QUICK_CHAT_MESSAGES)},")
        print("${cache.archive(Index.QUICK_CHAT_MENUS)},")
        print("${cache.archive(Index.PARTICLES)},")
        print("${cache.archive(Index.BILLBOARDS)},")
        print("${cache.group(Index.HUFFMAN, "huffman")},")
        print("${cache.archive(Index.INTERFACES)},")
        print("${cache.archive(Index.CLIENT_SCRIPTS)},")
        print("${cache.archive(Index.FONT_METRICS)},")
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