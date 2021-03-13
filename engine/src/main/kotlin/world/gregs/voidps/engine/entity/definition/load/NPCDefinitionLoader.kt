package world.gregs.voidps.engine.entity.definition.load

import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.NPCDefinitions

class NPCDefinitionLoader(private val loader: FileLoader, private val decoder: NPCDecoder) : TimedLoader<NPCDefinitions>("npc definition") {

    override fun load(args: Array<out Any?>): NPCDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        val changed = data.mapValues {
            val copy = data[it.value["copy"]]
            if (copy != null) {
                val mut = copy.toMutableMap()
                mut["id"] = it.value["id"] as Int
                mut
            } else {
                it.value
            }
        }
        count = names.size
        return NPCDefinitions(decoder, changed, names)
    }
}