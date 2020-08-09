package rs.dusk.engine.entity.gfx.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetailsTest

internal class GraphicDetailsTest : EntityDetailsTest<GraphicDetail, GraphicDetails>() {

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun detail(id: Int): GraphicDetail {
        return GraphicDetail(id)
    }

    override fun details(id: Map<Int, GraphicDetail>, names: BiMap<Int, String>): GraphicDetails {
        return GraphicDetails(id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<GraphicDetails> {
        return GraphicDetailsLoader(loader)
    }

}