package world.gregs.voidps.engine.data.definition.extra

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoderTest
import world.gregs.yaml.Yaml

internal class ItemDefinitionsTest : DefinitionsDecoderTest<ItemDefinition, ItemDecoder, ItemDefinitions>() {

    override lateinit var decoder: ItemDecoder
    override lateinit var definitions: Array<ItemDefinition>
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
        return ItemDefinitions(definitions)
    }

    override fun load(definitions: ItemDefinitions) {
        definitions.load(Yaml(), "../data/definitions/items.yml")
    }


}
