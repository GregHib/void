package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder

internal class ItemDefinitionsTest : DefinitionsDecoderTest<ItemDefinition, ItemDecoder, ItemDefinitions>() {

    override var decoder: ItemDecoder = ItemDecoder()
    override lateinit var definitions: Array<ItemDefinition>
    override val id: String = "lit_candle"
    override val intId: Int = 34

    override fun expected(): ItemDefinition = ItemDefinition(
        intId,
        stringId = id,
        extras = mutableMapOf(
            "examine" to "A candle.",
        ),
    )

    override fun empty(): ItemDefinition = ItemDefinition(-1)

    override fun definitions(): ItemDefinitions = ItemDefinitions(definitions)

    override fun load(definitions: ItemDefinitions) {
        val uri = ItemDefinitionsTest::class.java.getResource("test-item.toml")!!
        definitions.load(listOf(uri.path))
    }
}
