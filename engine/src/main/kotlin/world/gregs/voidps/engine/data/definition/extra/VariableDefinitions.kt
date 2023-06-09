package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
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
    private var varbitIds: Map<Int, String> = emptyMap()
    private var varpIds: Map<Int, String> = emptyMap()

    fun get(key: String) = definitions[key]

    fun getVarbit(id: Int) = varbitIds[id]

    fun getVarp(id: Int) = varpIds[id]

    fun load(storage: FileStorage = world.gregs.voidps.engine.get(), path: String = getProperty("definitionsPath")): VariableDefinitions {
        timedLoad("variable definition") {
            val map = mutableMapOf<String, VariableDefinition>()
            val files = File(path).listFiles()?.filter { it.name.startsWith("variables-") } ?: emptyList()
            val varbitIds = mutableMapOf<Int, String>()
            val varpIds = mutableMapOf<Int, String>()
            for (file in files) {
                val type = file.nameWithoutExtension.removePrefix("variables-")
                val factory = when (type) {
                    "player" -> VariableDefinition.varp()
                    "player-bit" -> VariableDefinition.varbit()
                    "client" -> VariableDefinition.varc()
                    "client-string" -> VariableDefinition.varcStr()
                    else -> VariableDefinition.custom()
                }
                val data = storage.load<Map<String, Any>>(file.path).mapIds()
                for ((key, value) in data) {
                    check(!map.containsKey(key)) { "All variable names must be unique. Duplicate: $key" }
                    val definition = factory.invoke(value)
                    map[key] = definition
                    if (type == "player") {
                        varpIds[definition.id] = key
                    } else if (type == "player-bit") {
                        varbitIds[definition.id] = key
                    }
                }
            }
            this.varbitIds = Int2ObjectOpenHashMap(varbitIds)
            this.varpIds = Int2ObjectOpenHashMap(varpIds)
            definitions = Object2ObjectOpenHashMap(map)
            definitions.size
        }
        return this
    }
}