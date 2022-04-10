package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.config.ItemOnItemDefinition
import world.gregs.voidps.engine.entity.definition.data.*
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty
import world.gregs.voidps.engine.utility.toIntRange
import world.gregs.voidps.network.visual.update.player.EquipSlot

class ItemDefinitions(
    decoder: ItemDecoder
) : DefinitionsDecoder<ItemDefinition> {

    override val definitions: Array<ItemDefinition>
    override lateinit var ids: Map<String, Int>

    val size = decoder.last

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("item definition", definitions.size, start)
    }

    override fun empty() = ItemDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("itemDefinitionsPath")): ItemDefinitions {
        timedLoad("item extra") {
            val equipment = mutableMapOf<Int, Int>()
            var count = 0
            for (def in definitions) {
                if (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0) {
                    equipment[def.id] = count++
                }
            }
            val modifications = DefinitionModifications()
            modifications["slot"] = { EquipSlot.valueOf(it as String) }
            modifications["type"] = { EquipType.valueOf(it as String) }
            modifications["kept"] = { ItemKept.valueOf(it as String) }
            modifications.add { map ->
                map["equip"] = equipment.getOrDefault(map["id"] as Int, -1)
            }
            modifications.map("fishing") { Catch(it) }
            modifications.map("firemaking") { Fire(it) }
            modifications.map("mining") { Ore(it) }
            modifications.map("cooking") { Uncooked(it) }
            modifications.map("tanning") { Tanning(it) }
            modifications.map("spinning") { Spinning(it) }
            modifications.map("pottery") { Pottery(it) }
            modifications.map("weaving") { Weaving(it) }
            modifications["make"] = { list: List<Map<String, Any>> -> list.map { map -> ItemOnItemDefinition(map) } }
            modifications["heals"] = { if (it is Int) it..it else if (it is String) it.toIntRange() else 0..0 }
            decode(storage, path, modifications)
        }
        return this
    }
}