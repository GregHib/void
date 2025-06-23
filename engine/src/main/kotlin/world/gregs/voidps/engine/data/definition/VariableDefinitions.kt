package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.client.variable.VariableValues
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.timedLoad
import kotlin.collections.set

class VariableDefinitions {

    private var definitions: Map<String, VariableDefinition> = emptyMap()
    private var varbitIds: Map<Int, String> = emptyMap()
    private var varpIds: Map<Int, String> = emptyMap()

    fun get(key: String) = definitions[key]

    fun getVarbit(id: Int) = varbitIds[id]

    fun getVarp(id: Int) = varpIds[id]

    fun load(players: List<String>, playerBits: List<String>, clients: List<String>, clientStrings: List<String>, custom: List<String>): VariableDefinitions {
        timedLoad("variable definition") {
            val definitions = Object2ObjectOpenHashMap<String, VariableDefinition>()
            val varbitIds = Int2ObjectOpenHashMap<String>()
            val varpIds = Int2ObjectOpenHashMap<String>()
            for (file in players) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    definitions[stringId] = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
                    varpIds[id] = stringId
                }
            }
            for (file in playerBits) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    definitions[stringId] = VariableDefinition.VarbitDefinition(id, values, default, persist, transmit)
                    varbitIds[id] = stringId
                }
            }
            for (file in clients) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    definitions[stringId] = VariableDefinition.VarcDefinition(id, values, default, persist, transmit)
                }
            }
            for (file in clientStrings) {
                load(file) { id, stringId, _, default, persist, transmit ->
                    definitions[stringId] = VariableDefinition.VarcStrDefinition(id, default, persist, transmit)
                }
            }
            for (file in custom) {
                load(file) { _, stringId, values, default, persist, _ ->
                    definitions[stringId] = VariableDefinition.CustomVariableDefinition(values, default, persist)
                }
            }
            this.varbitIds = varbitIds
            this.varpIds = varpIds
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

    fun load(path: String, block: (Int, String, VariableValues, Any?, Boolean, Boolean) -> Unit) {
        Config.fileReader(path) {
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
                block.invoke(id, stringId, varValue, default, persist, transmit)
            }
        }
    }
}
