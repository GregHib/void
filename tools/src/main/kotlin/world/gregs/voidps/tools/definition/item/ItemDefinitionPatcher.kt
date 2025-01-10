package world.gregs.voidps.tools.definition.item

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.tools.property
import world.gregs.yaml.Yaml

object ItemDefinitionPatcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val decoder = ItemDecoder().load(cache)
        val yaml = Yaml()
        val current = ItemDefinitions(ItemDecoder().load(cache)).load(yaml, property("definitions.items"))
        val newer = ItemDefinitions(ItemDecoder().load(cache)).load(yaml, "./items.yml")
        val map = mutableMapOf<Int, Double>()
        for (id in decoder.indices) {
            val def = current.getOrNull(id) ?: continue
            val def2 = newer.getOrNull(id) ?: continue
            if (!def.contains("weight") && def2.contains("weight")) {
                map[id] = def2.getOrNull("weight") ?: continue
            }
        }

        val linkedMap = linkedMapOf<String, Map<String, Any>>()
        for (id in decoder.indices) {
            val def = current.getOrNull(id) ?: continue
            val changes = def.extras?.toMutableMap() ?: mutableMapOf()
            val weight = map[id]
            if (weight != null) {
                changes["weight"] = weight
            }
            changes.remove("equip")
            val name = current.getOrNull(id)?.stringId
            if (name != null) {
                linkedMap[name] = changes
            } else {
                println("No name for $id $changes")
            }
        }
        yaml.save("./item-definition-extras-patched.yml", linkedMap)
    }
}