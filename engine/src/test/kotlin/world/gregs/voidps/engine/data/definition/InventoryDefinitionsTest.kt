package world.gregs.voidps.engine.data.definition

import io.mockk.mockk
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.config.decoder.InventoryDecoder

internal class InventoryDefinitionsTest : DefinitionsDecoderTest<InventoryDefinition, InventoryDecoder, InventoryDefinitions>() {

    override var decoder: InventoryDecoder = InventoryDecoder()
    override lateinit var definitions: Array<InventoryDefinition>
    override val id: String = "bobs_brilliant_axes"
    override val intId: Int = 1

    override fun expected(): InventoryDefinition {
        return InventoryDefinition(intId,
            stringId = id,
            ids = IntArray(0),
            amounts = IntArray(0),
            extras = mapOf(
                "shop" to true,
                "defaults" to listOf(mapOf("bronze_pickaxe" to 10),
                    mapOf("bronze_hatchet" to 10),
                    mapOf("iron_hatchet" to 10),
                    mapOf("steel_hatchet" to 10),
                    mapOf("iron_battleaxe" to 10),
                    mapOf("steel_battleaxe" to 10),
                    mapOf("mithril_battleaxe" to 10))))
    }

    override fun empty(): InventoryDefinition {
        return InventoryDefinition(-1)
    }

    override fun definitions(): InventoryDefinitions {
        return InventoryDefinitions(definitions)
    }

    override fun load(definitions: InventoryDefinitions) {
        definitions.load("../data/definitions/inventories.toml", mockk(relaxed = true))
    }
}
