package world.gregs.voidps.engine.data.definition

import io.mockk.every
import io.mockk.mockk
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.item.Item

internal class InventoryDefinitionsTest : DefinitionsDecoderTest<InventoryDefinition, InventoryDecoder, InventoryDefinitions>() {

    override var decoder: InventoryDecoder = InventoryDecoder()
    override lateinit var definitions: Array<InventoryDefinition>
    override val id: String = "bobs_brilliant_axes"
    override val intId: Int = 1

    override fun expected(): InventoryDefinition {
        return InventoryDefinition(
            intId,
            stringId = id,
            ids = IntArray(0),
            amounts = IntArray(0),
            extras = mapOf(
                "defaults" to listOf(
                    Item("bronze_pickaxe", 10),
                    Item("bronze_hatchet", 10),
                    Item("iron_hatchet", 10),
                    Item("steel_hatchet", 10),
                    Item("iron_battleaxe", 10),
                    Item("steel_battleaxe", 10),
                    Item("mithril_battleaxe", 10)
                ),
                "shop" to true
            )
        )
    }

    override fun empty(): InventoryDefinition {
        return InventoryDefinition(-1)
    }

    override fun definitions(): InventoryDefinitions {
        return InventoryDefinitions(definitions)
    }

    override fun load(definitions: InventoryDefinitions) {
        val uri = InventoryDefinitionsTest::class.java.getResource("test-inventory.toml")!!
        val itemDefs = mockk<ItemDefinitions>(relaxed = true)
        every { itemDefs.get(any<String>()) }.returns(ItemDefinition(1))
        definitions.load(listOf(uri.path), emptyList(), itemDefs)
    }
}
