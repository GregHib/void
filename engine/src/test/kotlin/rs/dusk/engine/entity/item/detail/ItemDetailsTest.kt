package rs.dusk.engine.entity.item.detail

import com.google.common.collect.BiMap
import io.mockk.mockk
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetailsTest
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.engine.entity.item.ItemDrop

internal class ItemDetailsTest : EntityDetailsTest<ItemDetail, ItemDetails>() {

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
            "demise" to "Wilderness",
            "destroy" to "No going back",
            "examine" to "Floating hands"
        )
    }

    override fun populated(id: Int): ItemDetail {
        return ItemDetail(
            id = id,
            slot = EquipSlot.Hands,
            type = EquipType.Sleeveless,
            weight = 1.01,
            edible = true,
            tradeable = false,
            alchable = false,
            bankable = false,
            individual = true,
            limit = 100,
            demise = ItemDrop.Wilderness,
            destroy = "No going back",
            examine = "Floating hands"
        )
    }

    override fun detail(id: Int): ItemDetail {
        return ItemDetail(id)
    }

    override fun details(id: Map<Int, ItemDetail>, names: BiMap<Int, String>): ItemDetails {
        return ItemDetails(id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<ItemDetails> {
        return ItemDetailsLoader(loader, mockk(relaxed = true))
    }

}