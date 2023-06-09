package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.data.definition.config.SpellDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class SpellDefinitions {

    private lateinit var definitions: Map<String, SpellDefinition>

    fun get(key: String) = definitions[key] ?: SpellDefinition()

    fun load(storage: FileStorage = get(), path: String = getProperty("spellDefinitionsPath")): SpellDefinitions {
        timedLoad("spell definition") {
            val data: Map<String, Any> = storage.load(path)
            load(data.mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        definitions = Object2ObjectOpenHashMap(data.map { (key, value) -> key to SpellDefinition(key, value) }.toMap())
        return definitions.size
    }

}