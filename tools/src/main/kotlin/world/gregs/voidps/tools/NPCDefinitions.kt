package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.parameterNames
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
//            if (def.name.contains("sir prysin", true)) {
            for((key, value) in def.params ?: continue) {
                if(!parameterNames.containsKey(key)) {
                    set.add(key.toInt())
                    println("Unknown param $i ${def.name} $key=$value ${def.params}")
                }
            }
//                println("$i ${def.name} ${definitions[i].params?.mapKeys { ItemParameters.parameters.get(it.key) ?: it.key.toString() }}")
//            }
            val key = "stab_def"
            if(def.has(key) && def.get(key, -1) != def.get("${key}_2", -1)) {
                println("$i ${def.name} ${def.get(key, -1)} ${def.get("${key}_2", -1)}")
            }
        }
        println(set.sorted())
    }
}