package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.client.variable.VariableValues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.timedLoad
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.set
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

class VariableDefinitions {

    private var definitions: Map<String, VariableDefinition> = emptyMap()
    private var varbitIds: Map<Int, String> = emptyMap()
    private var varpIds: Map<Int, String> = emptyMap()

    fun get(key: String) = definitions[key]

    fun getVarbit(id: Int) = varbitIds[id]

    fun getVarp(id: Int) = varpIds[id]

    fun load(path: String = Settings["definitions.variables"]): VariableDefinitions {
        timedLoad("variable definition") {
            val definitions = Object2ObjectOpenHashMap<String, VariableDefinition>()
            val varbitIds = Int2ObjectOpenHashMap<String>()
            val varpIds = Int2ObjectOpenHashMap<String>()
            for (file in Files.list(Path.of(path))) {
                if (file.extension != "toml") {
                    continue
                }
                val type = file.nameWithoutExtension.removePrefix("variables-")
                Config.fileReader(file.pathString) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        var values: Any? = null
                        var format: String? = null
                        var default: Any? = null
                        var persist = false
                        var transmit = true
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "persist" -> persist = boolean()
                                "transmit" -> transmit = boolean()
                                "default" -> default = value()
                                "format" -> format = string()
                                "values" -> values = value()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        val varValue = VariableValues(values, format, default)
                        when (type) {
                            "player" -> {
                                definitions[stringId] = VariableDefinition.VarpDefinition(id, varValue, default, persist, transmit)
                                varpIds[id] = stringId
                            }
                            "player-bit" -> {
                                definitions[stringId] = VariableDefinition.VarbitDefinition(id, varValue, default, persist, transmit)
                                varbitIds[id] = stringId
                            }
                            "client" -> definitions[stringId] = VariableDefinition.VarcDefinition(id, varValue, default, persist, transmit)
                            "client-string" -> definitions[stringId] = VariableDefinition.VarcStrDefinition(id, default, persist, transmit)
                            "custom" -> definitions[stringId] = VariableDefinition.CustomVariableDefinition(varValue, default, persist)
                            else -> throw IllegalArgumentException("Unexpected variable type: '$type'.")
                        }
                    }
                }
            }
            this.varbitIds = varbitIds
            this.varpIds = varpIds
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }
}