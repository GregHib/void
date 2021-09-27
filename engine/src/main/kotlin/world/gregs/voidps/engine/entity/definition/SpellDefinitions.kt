package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

class SpellDefinitions {

    private lateinit var definitions: Map<String, SpellDefinition>

    fun get(key: String) = definitions[key] ?: SpellDefinition()

    fun load(loader: FileLoader = get(), path: String = getProperty("spellDefinitionsPath")): SpellDefinitions {
        timedLoad("spell definition") {
            val data: Map<String, Any> = loader.load(path)
            load(data.mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        definitions = data.map { (key, value) -> key to SpellDefinition(value) }.toMap()
        return definitions.size
    }

}