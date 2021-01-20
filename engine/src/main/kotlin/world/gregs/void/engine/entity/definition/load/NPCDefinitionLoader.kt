package world.gregs.void.engine.entity.definition.load

import world.gregs.void.cache.definition.decoder.NPCDecoder
import world.gregs.void.engine.TimedLoader
import world.gregs.void.engine.data.file.FileLoader
import world.gregs.void.engine.entity.definition.NPCDefinitions

class NPCDefinitionLoader(private val loader: FileLoader, private val decoder: NPCDecoder) : TimedLoader<NPCDefinitions>("npc definition") {

    override fun load(args: Array<out Any?>): NPCDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return NPCDefinitions(decoder, data, names)
    }
}