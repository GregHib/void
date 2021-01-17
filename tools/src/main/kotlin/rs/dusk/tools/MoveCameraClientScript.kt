package rs.dusk.tools

import com.displee.cache.CacheLibrary
import rs.dusk.cache.Indices

object MoveCameraClientScript {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache667 = CacheLibrary("./cache/data/cache/")
        val cache634 = CacheLibrary("./cache/data/634/")

        val index667 = cache667.index(Indices.CLIENT_SCRIPTS)
        val index634 = cache634.index(Indices.CLIENT_SCRIPTS)

        index634.add(index667.archive(4731))
        cache634.update()
        println("Done")
    }
}