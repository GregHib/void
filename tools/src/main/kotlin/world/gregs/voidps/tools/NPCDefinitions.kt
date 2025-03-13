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
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val yaml = Yaml()
        val categories = CategoryDefinitions().load(property("definitions.categories"))
        val ammo = AmmoDefinitions().load(property("definitions.ammoGroups"))
        val parameters = ParameterDefinitions(categories, ammo).load(yaml, property("definitions.parameters"))
        val definitions = NPCDecoder(true, parameters).load(cache)
        val decoder = NPCDefinitions(definitions).load(yaml, property("definitions.npcs"))
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.name.contains("Talent scout", ignoreCase = true)) {
                println("$i ${def.name} ${def.extras} ${def.transforms?.contentToString()} ${def.options.contentDeepToString()}")
            }
        }
    }
}