package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.client.variable.BooleanValues
import world.gregs.voidps.engine.client.variable.IntValues
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.client.variable.NoValues
import world.gregs.voidps.engine.client.variable.StringValues
import world.gregs.voidps.engine.client.variable.VariableValues
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.snapshot.Snapshot
import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter
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

    fun load(files: ConfigFiles): VariableDefinitions {
        val players = files.list(Settings["definitions.variables.players"])
        val playerBits = files.list(Settings["definitions.variables.bits"])
        val clients = files.list(Settings["definitions.variables.clients"])
        val clientStrings = files.list(Settings["definitions.variables.strings"])
        val custom = files.list(Settings["definitions.variables.customs"])
        val snapshot = files.snapshot("variables", players + playerBits + clients + clientStrings + custom)
        return load(players, playerBits, clients, clientStrings, custom, snapshot)
    }

    fun load(players: List<String>, playerBits: List<String>, clients: List<String>, clientStrings: List<String>, custom: List<String>, snapshot: Snapshot? = null): VariableDefinitions {
        timedLoad("variable definition") {
            if (snapshot != null && restore(snapshot)) {
                return@timedLoad this.definitions.size
            }
            val definitions = Object2ObjectOpenHashMap<String, VariableDefinition>()
            val varbitIds = Int2ObjectOpenHashMap<String>()
            val varpIds = Int2ObjectOpenHashMap<String>()
            val varcIds = Int2ObjectOpenHashMap<String>()
            val varcStrIds = Int2ObjectOpenHashMap<String>()
            for (file in players) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    claim(definitions, stringId, file)
                    claim(varpIds, "varp", id, stringId, file)
                    definitions[stringId] = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
                }
            }
            for (file in playerBits) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    claim(definitions, stringId, file)
                    claim(varbitIds, "varbit", id, stringId, file)
                    definitions[stringId] = VariableDefinition.VarbitDefinition(id, values, default, persist, transmit)
                }
            }
            for (file in clients) {
                load(file) { id, stringId, values, default, persist, transmit ->
                    claim(definitions, stringId, file)
                    claim(varcIds, "varc", id, stringId, file)
                    definitions[stringId] = VariableDefinition.VarcDefinition(id, values, default, persist, transmit)
                }
            }
            for (file in clientStrings) {
                load(file) { id, stringId, _, default, persist, transmit ->
                    claim(definitions, stringId, file)
                    claim(varcStrIds, "varc string", id, stringId, file)
                    definitions[stringId] = VariableDefinition.VarcStrDefinition(id, default, persist, transmit)
                }
            }
            for (file in custom) {
                load(file, requireId = false) { _, stringId, values, default, persist, _ ->
                    claim(definitions, stringId, file)
                    definitions[stringId] = VariableDefinition.CustomVariableDefinition(values, default, persist)
                }
            }
            set(definitions, varbitIds, varpIds)
            snapshot?.let { save(it) }
            this.definitions.size
        }
        return this
    }

    private fun save(snapshot: Snapshot) = snapshot.write {
        int(definitions.size)
        for ((stringId, definition) in definitions) {
            string(stringId)
            byte(
                when (definition) {
                    is VariableDefinition.VarpDefinition -> VARP
                    is VariableDefinition.VarbitDefinition -> VARBIT
                    is VariableDefinition.VarcDefinition -> VARC
                    is VariableDefinition.VarcStrDefinition -> VARC_STR
                    is VariableDefinition.CustomVariableDefinition -> CUSTOM
                    else -> throw IllegalArgumentException("Unsupported variable type ${definition.javaClass}")
                },
            )
            int(definition.id)
            values(definition.values)
            value(definition.defaultValue)
            boolean(definition.persistent)
            boolean(definition.transmit)
        }
    }

    private fun SnapshotWriter.values(values: VariableValues) {
        when (values) {
            NoValues -> byte(0)
            IntValues -> byte(1)
            StringValues -> byte(2)
            BooleanValues -> byte(3)
            is ListValues -> {
                byte(4)
                value(values.values)
            }
            is MapValues -> {
                byte(5)
                value(values.values)
            }
            is BitwiseValues -> {
                byte(6)
                value(values.values)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun values(reader: SnapshotReader): VariableValues = when (val tag = reader.byte()) {
        0 -> NoValues
        1 -> IntValues
        2 -> StringValues
        3 -> BooleanValues
        4 -> ListValues(reader.value() as List<Any>)
        5 -> MapValues(reader.value() as Map<Any, Int>)
        6 -> BitwiseValues(reader.value() as List<Any>)
        else -> throw IllegalStateException("Unknown variable values tag $tag")
    }

    private fun restore(snapshot: Snapshot): Boolean = snapshot.restore { reader ->
        val definitions = Object2ObjectOpenHashMap<String, VariableDefinition>()
        val varbitIds = Int2ObjectOpenHashMap<String>()
        val varpIds = Int2ObjectOpenHashMap<String>()
        repeat(reader.int()) {
            val stringId = reader.string()
            val type = reader.byte()
            val id = reader.int()
            val values = values(reader)
            val default = reader.value()
            val persistent = reader.boolean()
            val transmit = reader.boolean()
            definitions[stringId] = when (type) {
                VARP -> {
                    varpIds[id] = stringId
                    VariableDefinition.VarpDefinition(id, values, default, persistent, transmit)
                }
                VARBIT -> {
                    varbitIds[id] = stringId
                    VariableDefinition.VarbitDefinition(id, values, default, persistent, transmit)
                }
                VARC -> VariableDefinition.VarcDefinition(id, values, default, persistent, transmit)
                VARC_STR -> VariableDefinition.VarcStrDefinition(id, default, persistent, transmit)
                CUSTOM -> VariableDefinition.CustomVariableDefinition(values, default, persistent)
                else -> throw IllegalStateException("Unknown variable type $type")
            }
        }
        set(definitions, varbitIds, varpIds)
    }

    private const val VARP = 0
    private const val VARBIT = 1
    private const val VARC = 2
    private const val VARC_STR = 3
    private const val CUSTOM = 4

    /**
     * Two variables sharing an [id] both write the same client variable, so setting one silently changes the other.
     */
    private fun claim(ids: Int2ObjectOpenHashMap<String>, type: String, id: Int, stringId: String, path: String) {
        val existing = ids.put(id, stringId)
        require(existing == null) { "Variables '$existing' and '$stringId' both use $type id $id, in '$path'." }
    }

    /**
     * A name declared twice is silently won by whichever file loads last, so the other definition never applies.
     */
    private fun claim(definitions: Map<String, VariableDefinition>, stringId: String, path: String) {
        require(!definitions.containsKey(stringId)) { "Variable '$stringId' is declared more than once, in '$path'." }
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
