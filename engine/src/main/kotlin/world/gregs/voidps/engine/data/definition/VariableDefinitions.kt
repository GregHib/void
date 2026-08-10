package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.client.variable.VariableValues
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.timedLoad

object VariableDefinitions {

    var definitions: Map<String, VariableDefinition> = emptyMap()
    private var varbitIds: Map<Int, String> = emptyMap()
    private var varpIds: Map<Int, String> = emptyMap()

    var loaded = false
        private set

    fun set(definitions: Map<String, VariableDefinition>, varbits: Map<Int, String> = emptyMap(), varps: Map<Int, String> = emptyMap()) {
        this.definitions = definitions
        this.varbitIds = varbits
        this.varpIds = varps
        loaded = true
    }

    fun clear() {
        definitions = emptyMap()
        varbitIds = emptyMap()
        varpIds = emptyMap()
        loaded = false
    }

    fun get(key: String) = definitions[key]

    fun getVarbit(id: Int) = varbitIds[id]

    fun getVarp(id: Int) = varpIds[id]

    fun load(files: ConfigFiles) = load(
        files.list(Settings["definitions.variables.players"]),
        files.list(Settings["definitions.variables.bits"]),
        files.list(Settings["definitions.variables.clients"]),
        files.list(Settings["definitions.variables.strings"]),
        files.list(Settings["definitions.variables.customs"]),
    )

    fun load(players: List<String>, playerBits: List<String>, clients: List<String>, clientStrings: List<String>, custom: List<String>): VariableDefinitions {
        timedLoad("variable definition") {
            val definitions = Object2ObjectOpenHashMap<String, VariableDefinition>()
            val varbitIds = Int2ObjectOpenHashMap<String>()
            val varpIds = Int2ObjectOpenHashMap<String>()
            val varcIds = Int2ObjectOpenHashMap<String>()
            val varcStrIds = Int2ObjectOpenHashMap<String>()
            for (file in players) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    definitions[stringId] = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
                    claim(varpIds, "varp", id, stringId, file)
                }
            }
            for (file in playerBits) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    definitions[stringId] = VariableDefinition.VarbitDefinition(id, values, default, persist, transmit)
                    claim(varbitIds, "varbit", id, stringId, file)
                }
            }
            for (file in clients) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    definitions[stringId] = VariableDefinition.VarcDefinition(id, values, default, persist, transmit)
                    claim(varcIds, "varc", id, stringId, file)
                }
            }
            for (file in clientStrings) {
                load(file) { id, stringId, _, default, persist, transmit ->
                    definitions[stringId] = VariableDefinition.VarcStrDefinition(id, default, persist, transmit)
                    claim(varcStrIds, "varc string", id, stringId, file)
                }
            }
            for (file in custom) {
                load(file, requireId = false) { _, stringId, values, default, persist, _ ->
                    definitions[stringId] = VariableDefinition.CustomVariableDefinition(values, default, persist)
                }
            }
            set(definitions, varbitIds, varpIds)
            this.definitions.size
        }
        return this
    }

    /**
     * Two variables sharing an [id] both write the same client variable, so setting one silently changes the other.
     */
    private fun claim(ids: Int2ObjectOpenHashMap<String>, type: String, id: Int, stringId: String, path: String) {
        val existing = ids.put(id, stringId)
        require(existing == null) { "Variables '$existing' and '$stringId' both use $type id $id, in '$path'." }
    }

    private fun load(path: String, requireId: Boolean = true, block: (Int, String, VariableValues, Any?, Boolean, Boolean) -> Unit) {
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
                require(!requireId || id != -1) { "Variable '$stringId' is missing an id, use a 'vars.toml' file for server-only variables. ${exception()}" }
                val varValue = VariableValues(values, format, default)
                block.invoke(id, stringId, varValue, default, persist, transmit)
            }
        }
    }
}
