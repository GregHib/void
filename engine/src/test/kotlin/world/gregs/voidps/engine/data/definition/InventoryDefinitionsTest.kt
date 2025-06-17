package world.gregs.voidps.engine.data.definition

import io.mockk.every
import io.mockk.mockk
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.definition.data.ItemDefinition

internal class InventoryDefinitionsTest : DefinitionsDecoderTest<InventoryDefinition, InventoryDecoder, InventoryDefinitions>() {

    override var decoder: InventoryDecoder = InventoryDecoder()
    override lateinit var definitions: Array<InventoryDefinition>
    override val id: String = "bobs_brilliant_axes"
    override val intId: Int = 1

    override fun expected(): InventoryDefinition = InventoryDefinition(
        intId,
        stringId = id,
        length = 7,
        ids = intArrayOf(1, 2, 3, 4, 5, 6, 7),
        amounts = intArrayOf(10, 10, 10, 10, 10, 10, 10),
        extras = mapOf(
            "shop" to true,
        ),
    )

    override fun empty(): InventoryDefinition = InventoryDefinition(-1)

    override fun definitions(): InventoryDefinitions = InventoryDefinitions(definitions)

    override fun load(definitions: InventoryDefinitions) {
        val uri = InventoryDefinitionsTest::class.java.getResource("test-inventory.toml")!!
        val itemDefs = mockk<ItemDefinitions>(relaxed = true)
        every { itemDefs.get(any<String>()) }.returns(ItemDefinition(0))
        every { itemDefs.get("bronze_pickaxe") }.returns(ItemDefinition(1))
        every { itemDefs.get("bronze_hatchet") }.returns(ItemDefinition(2))
        every { itemDefs.get("iron_hatchet") }.returns(ItemDefinition(3))
        every { itemDefs.get("steel_hatchet") }.returns(ItemDefinition(4))
        every { itemDefs.get("iron_battleaxe") }.returns(ItemDefinition(5))
        every { itemDefs.get("steel_battleaxe") }.returns(ItemDefinition(6))
        every { itemDefs.get("mithril_battleaxe") }.returns(ItemDefinition(7))
        every { itemDefs.contains(any<String>()) }.returns(true)
        definitions.load(emptyList(), listOf(uri.path), itemDefs)
    }
}
