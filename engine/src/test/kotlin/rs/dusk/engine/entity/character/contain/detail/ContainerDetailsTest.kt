package rs.dusk.engine.entity.character.contain.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetailsTest
import rs.dusk.engine.entity.character.contain.StackMode

internal class ContainerDetailsTest : EntityDetailsTest<ContainerDetail, ContainerDetails>() {

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id, "stack" to "Always")
    }

    override fun populated(id: Int): ContainerDetail {
        return ContainerDetail(id, StackMode.Always)
    }

    override fun detail(id: Int): ContainerDetail {
        return ContainerDetail(id)
    }

    override fun details(id: Map<Int, ContainerDetail>, names: BiMap<Int, String>): ContainerDetails {
        return ContainerDetails(id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<ContainerDetails> {
        return ContainerDetailsLoader(loader)
    }

}