package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.yaml.Yaml

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val yaml = Yaml()
        val categories = CategoryDefinitions().load(yaml, property("categoryDefinitionsPath"))
        val ammo = AmmoDefinitions().load(yaml, property("ammoDefinitionsPath"))
        val parameters = ParameterDefinitions(categories, ammo).load(yaml, property("parameterDefinitionsPath"))
        val definitions = NPCDecoder(true, parameters).load(cache)
        val decoder = NPCDefinitions(definitions).load(yaml, property("npcDefinitionsPath"))
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.transforms?.contains(171) == true || def.transforms?.contains(4610) == true) {
                println("$i ${def.name} ${def.transforms?.toList()}")
            }
            if (def.name.contains("brimstail", ignoreCase = true)) {
                println("$i ${def.name} ${def.extras} ${def.varp} ${def.varbit}")
            }
        }
    }
}