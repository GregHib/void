package rs.dusk.engine.entity.character.npc.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetailsTest

internal class NPCDetailsTest : EntityDetailsTest<NPCDetail, NPCDetails>() {

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun detail(id: Int): NPCDetail {
        return NPCDetail(id)
    }

    override fun details(id: Map<Int, NPCDetail>, names: BiMap<Int, String>): NPCDetails {
        return NPCDetails(id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<NPCDetails> {
        return NPCDetailsLoader(loader)
    }
}