package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept

internal class ItemDefinitionsTest : DefinitionsDecoderTest<ItemDefinition, ItemDecoder, ItemDefinitions>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf(
            "id" to id,
            "slot" to "Hands",
            "type" to "Sleeveless",
            "weight" to 1.01,
            "edible" to true,
            "tradeable" to false,
            "alchable" to false,
            "bankable" to false,
            "individual" to true,
            "limit" to 100,
            "kept" to "Wilderness",
            "destroy" to "No going back",
            "examine" to "Floating hands"
        )
    }

    override fun populated(id: Int): Map<String, Any> {
        return mapOf(
            "id" to id,
            "slot" to EquipSlot.Hands,
            "type" to EquipType.Sleeveless,
            "weight" to 1.01,
            "edible" to true,
            "tradeable" to false,
            "alchable" to false,
            "bankable" to false,
            "individual" to true,
            "limit" to 100,
            "kept" to ItemKept.Wilderness,
            "destroy" to "No going back",
            "examine" to "Floating hands",
            "equip" to -1
        )
    }

    override fun definition(id: Int): ItemDefinition {
        return ItemDefinition(id, stringId = id.toString())
    }

    override fun definitions(decoder: ItemDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): ItemDefinitions {
        return ItemDefinitions(decoder).apply {
            load(id)
            this.names = names
        }
    }

}