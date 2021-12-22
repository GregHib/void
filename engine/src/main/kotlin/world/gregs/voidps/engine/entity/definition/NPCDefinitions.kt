package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.definition.data.Spot
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class NPCDefinitions(
    override val decoder: NPCDecoder
) : DefinitionsDecoder<NPCDefinition, NPCDecoder>() {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    init {
        modifications["fishing"] = { (it as Map<String, Any>).mapValues { Spot(it.value) } }
    }

    fun load(storage: FileStorage = get(), path: String = getProperty("npcDefinitionsPath")): NPCDefinitions {
        timedLoad("npc definition") {
            decoder.clear()
            load(storage.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        names = data.map { it.value["id"] as Int to it.key }.toMap()
        this.extras = data.mapModifications()
        return names.size
    }
}