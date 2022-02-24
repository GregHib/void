package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.data.Spot
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class NPCDefinitions(
    decoder: NPCDecoder
) : DefinitionsDecoder<NPCDefinition> {

    override val definitions: Array<NPCDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("npc definition", definitions.size, start)
    }

    override fun empty() = NPCDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("npcDefinitionsPath")): NPCDefinitions {
        timedLoad("npc extra") {
            val modifications = DefinitionModifications()
            modifications["fishing"] = { map: Map<String, Map<String, Any>> -> map.mapValues { value -> Spot(value.value) } }
            decode(storage, path, modifications)
        }
        return this
    }

}