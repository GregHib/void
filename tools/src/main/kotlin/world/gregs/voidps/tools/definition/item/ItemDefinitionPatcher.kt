package world.gregs.voidps.tools.definition.item

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.yaml.YamlParser
import java.io.File

object ItemDefinitionPatcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())
        val parser = YamlParser()
        val storage = FileStorage(true)
        val current = ItemDefinitions(ItemDecoder(koin.get())).load(parser, "./data/definitions/items.yml")
        val newer = ItemDefinitions(ItemDecoder(koin.get())).load(parser, "./items.yml")
        val map = mutableMapOf<Int, Double>()
        for (id in decoder.indices) {
            val def = current.getOrNull(id) ?: continue
            val def2 = newer.getOrNull(id) ?: continue
            if (!def.has("weight") && def2.has("weight")) {
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
        val file = File("./item-definition-extras-patched.yml")
        storage.save(file, linkedMap)
    }
}