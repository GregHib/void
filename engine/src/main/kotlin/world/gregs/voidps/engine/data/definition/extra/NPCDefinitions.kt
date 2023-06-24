package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.definition.Transforms
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionModifications
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class NPCDefinitions(
    decoder: NPCDecoder
) : DefinitionsDecoder<NPCDefinition> {

    override lateinit var definitions: Array<NPCDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("npc definition", definitions.size, start)
    }

    override fun empty() = NPCDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("npcDefinitionsPath"), itemDefinitions: ItemDefinitions = get()): NPCDefinitions {
        timedLoad("npc extra") {
            val modifications = DefinitionModifications()
            modifications["fishing"] = { map: Map<String, Map<String, Any>> -> map.mapValues { value -> Spot(value.value, itemDefinitions) } }
            modifications.transform(Transforms.transformer)
            decode(storage, path, modifications)
        }
        return this
    }

}