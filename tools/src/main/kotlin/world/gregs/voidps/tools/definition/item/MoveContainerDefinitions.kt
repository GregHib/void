package world.gregs.voidps.tools.definition.item

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Configs
import world.gregs.voidps.cache.Indices

object MoveContainerDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache718 = CacheLibrary("${System.getProperty("user.home")}/Downloads/rs718_cache/")
        val cache634 = CacheLibrary("./data/cache")

        val archive718 = cache718.index(Indices.CONFIGS).archive(Configs.CONTAINERS)!!
        val archive634 = cache634.index(Indices.CONFIGS).archive(Configs.CONTAINERS)!!

        val size = archive634.last()?.id ?: -1

        for (i in 0 until size) {
            archive634.add(archive718.file(i) ?: continue)
        }
        cache634.update()
        println("Done")
    }
}