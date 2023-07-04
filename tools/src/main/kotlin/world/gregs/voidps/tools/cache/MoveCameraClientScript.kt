package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Index

object MoveCameraClientScript {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache667 = CacheLibrary("./data/667/")
        val cache634 = CacheLibrary("./data/cache/")

        val index667 = cache667.index(Index.CLIENT_SCRIPTS)
        val index634 = cache634.index(Index.CLIENT_SCRIPTS)

        index634.add(index667.archive(4731))
        cache634.update()
        println("Done")
    }
}