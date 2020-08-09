package rs.dusk.engine.entity.obj.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetailsTest

internal class ObjectDetailsTest : EntityDetailsTest<ObjectDetail, ObjectDetails>() {

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun detail(id: Int): ObjectDetail {
        return ObjectDetail(id)
    }

    override fun details(id: Map<Int, ObjectDetail>, names: BiMap<Int, String>): ObjectDetails {
        return ObjectDetails(id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<ObjectDetails> {
        return ObjectDetailsLoader(loader)
    }
}