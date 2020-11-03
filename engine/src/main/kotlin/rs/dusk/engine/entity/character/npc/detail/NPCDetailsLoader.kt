package rs.dusk.engine.entity.character.npc.detail

import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

class NPCDetailsLoader(private val loader: FileLoader, private val decoder: NPCDecoder) : TimedLoader<NPCDetails>("npc detail") {

    override fun load(args: Array<out Any?>): NPCDetails {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return NPCDetails(decoder, data, names)
    }
}