package world.gregs.voidps.engine.data.definition

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

    override fun definitions(): InventoryDefinitions = InventoryDefinitions.init(definitions)

    override fun load(definitions: InventoryDefinitions) {
        val uri = InventoryDefinitionsTest::class.java.getResource("test-inventory.toml")!!
        ItemDefinitions.set(Array(8) { ItemDefinition(it) }, mapOf(
            "bronze_pickaxe" to 1,
            "bronze_hatchet" to 2,
            "iron_hatchet" to 3,
            "steel_hatchet" to 4,
            "iron_battleaxe" to 5,
            "steel_battleaxe" to 6,
            "mithril_battleaxe" to 7,
        ))
        definitions.load(emptyList(), listOf(uri.path))
    }
}
