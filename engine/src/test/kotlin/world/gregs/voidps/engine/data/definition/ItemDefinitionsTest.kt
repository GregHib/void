package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.yaml.Yaml

internal class ItemDefinitionsTest : DefinitionsDecoderTest<ItemDefinition, ItemDecoder, ItemDefinitions>() {

    override var decoder: ItemDecoder = ItemDecoder()
    override lateinit var definitions: Array<ItemDefinition>
    override val id: String = "lit_candle"
    override val intId: Int = 34

    override fun expected(): ItemDefinition {
        return ItemDefinition(intId, stringId = id, extras = mapOf(
            "examine" to "A candle."
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
