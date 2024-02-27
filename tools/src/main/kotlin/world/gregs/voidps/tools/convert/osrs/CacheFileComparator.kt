package world.gregs.voidps.tools.convert.osrs

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.property
import java.io.File

object CacheFileComparator {
    private class ByteWrapper(val array: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (other is ByteWrapper) {
                return array.contentEquals(other.array)
            }
            if (other is ByteArray) {
                return array.contentEquals(other)
            }
            return super.equals(other)
        }

        override fun hashCode(): Int {
            return array.contentHashCode()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val mappings = mapOf(
            Index.SOUND_EFFECTS to "sound",
            Index.MUSIC to "music",
            Index.SPRITES to "sprite",
            Index.MUSIC_EFFECTS to "jingle",
            Index.CLIENT_SCRIPTS to "clientscript",
        )

        val osrsCache = CacheDelegate("${System.getProperty("user.home")}\\Downloads\\osrs-215-cache\\")
        val cache = CacheDelegate(property("cachePath"))
        val directory = File("./temp/osrs/generated/")

        for ((index, mappingFile) in mappings) {
            val osrsNames = File("./temp/osrs/mappings/${mappingFile}.rscm").readLines().filter { it.contains(":") }.associate {
                val (name, id) = it.split(":")
                id.toInt() to name
            }
            val allData = mutableMapOf<ByteWrapper, Int>()
            for (archive in osrsCache.archives(index)) {
                val data = osrsCache.data(index, archive) ?: continue
                if (data.isEmpty()) {
                    continue
                }
                allData[ByteWrapper(data)] = archive
            }

            val output = directory.resolve("634-${mappingFile}-names.txt")
            var matches = 0
            var names = 0
            for (archive in cache.archives(index)) {
                val data = cache.data(index, archive) ?: continue
                val wrapper = ByteWrapper(data)
                if (allData.containsKey(wrapper)) {
                    matches++
                    val osrsArchive = allData[wrapper]!!
                    if (osrsNames.containsKey(osrsArchive)) {
                        output.appendText("${osrsNames[osrsArchive]}:${archive}\n")
                        names++
                    }
                }
            }
            println("Matches: $matches for index: $index names: $names")
        }
    }
}