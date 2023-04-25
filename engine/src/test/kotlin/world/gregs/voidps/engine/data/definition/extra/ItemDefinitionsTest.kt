package world.gregs.voidps.engine.data.definition.extra

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoderTest

internal class ItemDefinitionsTest : DefinitionsDecoderTest<ItemDefinition, ItemDecoder, ItemDefinitions>() {

    override lateinit var decoder: ItemDecoder
    override val id: String = "lit_candle"
    override val intId: Int = 34

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun expected(): ItemDefinition {
        return ItemDefinition(intId, stringId = id, extras = mapOf(
            "id" to intId,
            "examine" to "A candle.",
            "equip" to -1
        ))
    }

    override fun empty(): ItemDefinition {
        return ItemDefinition(-1)
    }

    override fun definitions(): ItemDefinitions {
        return ItemDefinitions(decoder)
    }

    override fun load(definitions: ItemDefinitions) {
        definitions.load(FileStorage(), "../data/definitions/items.yml")
    }


}
