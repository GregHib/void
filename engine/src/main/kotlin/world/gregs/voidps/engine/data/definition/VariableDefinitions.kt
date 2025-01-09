package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.yaml.DefinitionIdsConfig
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import java.io.File
import kotlin.collections.set

class VariableDefinitions {

    private var definitions: Map<String, VariableDefinition> = emptyMap()
    private var varbitIds: Map<Int, String> = emptyMap()
    private var varpIds: Map<Int, String> = emptyMap()

    fun get(key: String) = definitions[key]

    fun getVarbit(id: Int) = varbitIds[id]

    fun getVarp(id: Int) = varpIds[id]

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitionsPath"]): VariableDefinitions {
        timedLoad("variable definition") {
            val definitions = Object2ObjectOpenHashMap<String, VariableDefinition>()
            val files = File(path).listFiles()?.filter { it.name.startsWith("variables-") } ?: emptyList()
            val varbitIds = Int2ObjectOpenHashMap<String>()
            val varpIds = Int2ObjectOpenHashMap<String>()
            for (file in files) {
                val type = file.nameWithoutExtension.removePrefix("variables-")
                val factory: (Map<String, Any>) -> VariableDefinition = when (type) {
                    "player" -> {
                        { VariableDefinition.VarpDefinition(it) }
                    }
                    "player-bit" -> {
                        { VariableDefinition.VarbitDefinition(it) }
                    }
                    "client" -> {
                        { VariableDefinition.VarcDefinition(it) }
                    }
                    "client-string" -> {
                        { VariableDefinition.VarcStrDefinition(it) }
                    }
                    else -> {
                        { VariableDefinition.CustomVariableDefinition(it) }
                    }
                }
                val config = object : DefinitionIdsConfig() {
                    override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) { 
                        if (indent == 0) {
                            val definition = factory.invoke(if (value is Int) {
                                mapOf("id" to value)
                            } else {
                                value as Map<String, Any>
                            })
                            definitions[key] = definition
                            if (type == "player") {
                                varpIds[definition.id] = key
                            } else if (type == "player-bit") {
                                varbitIds[definition.id] = key
                            }
                        } else {
                            super.set(map, key, value, indent, parentMap)
                        }
                    }
                }
                yaml.load<Any>(file.path, config)
            }
            this.varbitIds = varbitIds
            this.varpIds = varpIds
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }
}