package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.engine.client.variable.VariableType
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.data.definition.config.VariableDefinition
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class VariableDefinitions {

    private var definitions: Map<String, VariableDefinition> = emptyMap()
    private var ids: Map<VariableType, Map<Int, String>> = emptyMap()

    fun getKey(type: VariableType, id: Int) = ids[type]?.get(id)

    fun get(key: String) = definitions[key]

    fun getValue(key: String) = definitions.getValue(key)

    fun load(storage: FileStorage = world.gregs.voidps.engine.get(), path: String = getProperty("definitionsPath")): VariableDefinitions {
        timedLoad("variable definition") {
            val map = mutableMapOf<String, VariableDefinition>()
            val files = File(path).listFiles()?.filter { it.name.startsWith("variables-") } ?: emptyList()
            for (file in files) {
                val type = when (file.nameWithoutExtension.removePrefix("variables-")) {
                    "player" -> VariableType.Varp
                    "player-bit" -> VariableType.Varbit
                    "client" -> VariableType.Varc
                    "client-string" -> VariableType.Varcstr
                    else -> VariableType.Custom
                }
                val data = storage.load<Map<String, Any>>(file.path).mapIds()
                for ((key, value) in data) {
                    check(!map.containsKey(key)) { "All variable names must be unique. Duplicate: $key" }
                    map[key] = VariableDefinition(value, type = type)
                }
            }
            definitions = map
            definitions.size
        }
        return this
    }
}