package rs.dusk.engine.entity.item.detail

import com.google.common.collect.BiMap
import io.mockk.mockk
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetailsTest

internal class ItemDetailsTest : EntityDetailsTest<ItemDetail, ItemDetails>() {

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun populated(id: Int): ItemDetail {
        return ItemDetail(id)
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