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
    override val decoder: ItemDecoder
) : DefinitionsDecoder<ItemDefinition, ItemDecoder>() {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    private var equipmentCount = 0
    private val equipmentIndices = (0 until decoder.last).associateWith {
        val def = decoder.getOrNull(it)
        if (def != null && (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0)) {
            equipmentCount++
        } else {
            -1
        }
    }

    init {
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
    }

    fun load(storage: FileStorage = get(), path: String = getProperty("itemDefinitionsPath")): ItemDefinitions {
        timedLoad("item definition") {
            decoder.clear()
            load(storage.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        names = data.map { it.value["id"] as Int to it.key }.toMap()
        this.extras = data.mapModifications()
        return names.size
    }
}