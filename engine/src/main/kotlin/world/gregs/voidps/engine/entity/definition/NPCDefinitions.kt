package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

class NPCDefinitions(
    override val decoder: NPCDecoder
) : DefinitionsDecoder<NPCDefinition, NPCDecoder> {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    fun load(loader: FileLoader = get(), path: String = getProperty("npcDefinitionsPath")): NPCDefinitions {
        timedLoad("npc definition") {
            decoder.clear()
            load(loader.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        names = data.map { it.value["id"] as Int to it.key }.toMap()
        this.extras = data.mapValues {
            val copy = data[it.value["copy"]]
            if (copy != null) {
                val mut = copy.toMutableMap()
                mut["id"] = it.value["id"] as Int
                mut
            } else {
                it.value
            }
        }
        return names.size
    }
}