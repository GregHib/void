package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.definition.data.FiremakingFire
import world.gregs.voidps.engine.entity.definition.data.FishingCatch
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

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
        modifications["fishing"] = { FishingCatch(it as Map<String, Any>) }
        modifications["firemaking"] = { FiremakingFire(it as Map<String, Any>) }
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