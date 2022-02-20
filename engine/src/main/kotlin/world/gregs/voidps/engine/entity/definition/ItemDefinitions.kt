package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.config.ItemOnItemDefinition
import world.gregs.voidps.engine.entity.definition.data.Catch
import world.gregs.voidps.engine.entity.definition.data.Fire
import world.gregs.voidps.engine.entity.definition.data.Ore
import world.gregs.voidps.engine.entity.definition.data.Uncooked
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty
import world.gregs.voidps.engine.utility.toIntRange

class ItemDefinitions(
    decoder: ItemDecoder
) : DefinitionsDecoded<ItemDefinition> {

    override val definitions: Array<ItemDefinition>
    override lateinit var ids: Map<String, Int>
    private val equipmentIndices: Map<Int, Int>

    val size = decoder.last

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        var count = 0
        val map = mutableMapOf<Int, Int>()
        for (def in definitions) {
            if (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0) {
                map[def.id] = count++
            }
        }
        equipmentIndices = map
        timedLoad("item definition", definitions.size, start)
    }

    override fun empty() = ItemDefinition.EMPTY


    fun load(storage: FileStorage = get(), path: String = getProperty("itemDefinitionsPath")): ItemDefinitions {
        timedLoad("item extra") {
            val modifications = DefinitionModifications()
            modifications["slot"] = { EquipSlot.valueOf(it as String) }
            modifications["type"] = { EquipType.valueOf(it as String) }
            modifications["kept"] = { ItemKept.valueOf(it as String) }
            modifications.add { map ->
                map["equip"] = equipmentIndices.getOrDefault(map["id"] as Int, -1)
            }
            modifications.map("fishing") { Catch(it) }
            modifications.map("firemaking") { Fire(it) }
            modifications.map("mining") { Ore(it) }
            modifications.map("cooking") { Uncooked(it) }
            modifications["make"] = { list: List<Map<String, Any>> -> list.map { map -> ItemOnItemDefinition(map) } }
            modifications["heals"] = { if (it is Int) it..it else if (it is String) it.toIntRange() else 0..0 }
            val data = storage.loadMapIds(path)
            val names = data.map { it.value["id"] as Int to it.key }.toMap()
            ids = data.map { it.key to it.value["id"] as Int }.toMap()
            val extras = modifications.apply(data)
            apply(names, extras)
            names.size
        }
        return this
    }
}