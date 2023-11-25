package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.yaml.Yaml

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val definitions = NPCDecoder(true).loadCache(cache)
        val decoder = NPCDefinitions(definitions).load(Yaml(), "./data/definitions/npcs.yml")
        val set = mutableSetOf<Int>()
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println("$i ${def.name} ${def.extras}")
        }
        println(set.sorted())
    }
}