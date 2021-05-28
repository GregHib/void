package world.gregs.voidps.tools.definition.item

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import java.io.File

object ItemDefinitionPatcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())
        val loader = FileLoader(true)
        val current = ItemDefinitions(ItemDecoder(koin.get())).load(loader, "./data/definitions/item-definition-extras.yml")
        val newer = ItemDefinitions(ItemDecoder(koin.get())).load(loader, "./item-definition-extras.yml")
        val map = mutableMapOf<Int, Double>()
        for (id in decoder.indices) {
            val def = current.getOrNull(id) ?: continue
            val def2 = newer.getOrNull(id) ?: continue
            if (!def.has("weight") && def2.has("weight")) {
                map[id] = def2.getOrNull("weight") as Double
            }
        }

        val linkedMap = linkedMapOf<String, Map<String, Any>>()
        for (id in decoder.indices) {
            val def = current.getOrNull(id) ?: continue
            val changes = def.extras.toMutableMap()
            val weight = map[id]
            if (weight != null) {
                changes["weight"] = weight
            }
            changes.remove("equip")
            val name = current.getNameOrNull(id)
            if (name != null) {
                linkedMap[name] = changes
            } else {
                println("No name for $id $changes")
            }
        }
        val file = File("./item-definition-extras-patched.yml")
        loader.save(file, linkedMap)
    }
}