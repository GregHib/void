package world.gregs.voidps.cache.memory

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Index
import world.gregs.voidps.type.Region
import java.io.File

object RemoveXteas {
    @JvmStatic
    fun main(args: Array<String>) {
        val xteas = InMemory.loadBinary(File("./data/xteas.dat"))
        val path = "./data/cache/test/"
        val lib = CacheLibrary(path)
        val index = lib.index(Index.MAPS)
        val indexId = Index.MAPS

        var regions = 0
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val id = Region.id(regionX, regionY)
                val keys = xteas[id]
                val name = "l${regionX}_$regionY"
                val data = lib.data(indexId, name, 0, keys) ?: continue
                lib.put(indexId, name, data)
                regions++
            }
        }
        index.flag()
        lib.update()
        println("Removed xteas from $regions regions.")
    }
}