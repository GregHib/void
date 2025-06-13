package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Index
import world.gregs.voidps.type.Region

object RemoveXteas {

    fun remove(library: CacheLibrary, xteas: Xteas) {
        println("Removing all xteas...")
        val indexId = Index.MAPS
        val index = library.index(indexId)

        var regions = 0
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val id = Region.id(regionX, regionY)
                val keys = xteas[id]
                val name = "l${regionX}_$regionY"
                val data = library.data(indexId, name, 0, keys) ?: continue
                library.put(indexId, name, data)
                regions++
            }
        }
        index.flag()
        library.update()
        println("Removed xteas from $regions regions.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val xteas = Xteas().load("./tools/src/main/resources/xteas.dat", Xteas.DEFAULT_KEY, Xteas.DEFAULT_VALUE)
        val path = "./data/cache/test/"
        val lib = CacheLibrary(path)

        remove(lib, xteas)
    }
}