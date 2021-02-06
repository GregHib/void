package world.gregs.voidps.tools

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Indices

object MoveCameraClientScript {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache667 = CacheLibrary("./data/667/")
        val cache634 = CacheLibrary("./data/cache/")

        val index667 = cache667.index(Indices.CLIENT_SCRIPTS)
        val index634 = cache634.index(Indices.CLIENT_SCRIPTS)

        index634.add(index667.archive(4731))
        cache634.update()
        println("Done")
    }
}