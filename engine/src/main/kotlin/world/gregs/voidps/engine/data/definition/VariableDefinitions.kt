package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.decodeValue
import world.gregs.voidps.engine.data.snapshot.encodeValue

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

    fun install(state: VariablesState) = set(state.definitions, state.varbits, state.varps)

    fun load(files: ConfigFiles): VariableDefinitions {
        install(
            parse(
                files.list(Settings["definitions.variables.players"]),
                files.list(Settings["definitions.variables.bits"]),
                files.list(Settings["definitions.variables.clients"]),
                files.list(Settings["definitions.variables.strings"]),
                files.list(Settings["definitions.variables.customs"]),
            ),
        )
        return this
    }

    fun parse(players: List<String>, playerBits: List<String>, clients: List<String>, clientStrings: List<String>, custom: List<String>): VariablesState {
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
        return VariablesState(definitions, varbitIds, varpIds)
    }

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

class VariablesState(
    val definitions: Map<String, VariableDefinition>,
    val varbits: Map<Int, String>,
    val varps: Map<Int, String>,
)

object VariableLoader : Loader<VariablesState> {
    override val name = "variables"
    override val files = listOf(
        "definitions.variables.players",
        "definitions.variables.bits",
        "definitions.variables.clients",
        "definitions.variables.strings",
        "definitions.variables.customs",
    )
    override val codec = SnapshotFormat.codec(VariablesStateSerializer)

    override fun load(context: LoadContext): VariablesState = VariableDefinitions.parse(
        context.list("definitions.variables.players"),
        context.list("definitions.variables.bits"),
        context.list("definitions.variables.clients"),
        context.list("definitions.variables.strings"),
        context.list("definitions.variables.customs"),
    )

    override fun install(state: VariablesState) = VariableDefinitions.install(state)
}

private const val VARP = 0
private const val VARBIT = 1
private const val VARC = 2
private const val VARC_STR = 3
private const val CUSTOM = 4

private object VariablesStateSerializer : KSerializer<VariablesState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.engine.data.definition.VariablesState") {
        element("stringId", String.serializer().descriptor)
        element("type", Byte.serializer().descriptor)
        element("id", Int.serializer().descriptor)
        element("values", Byte.serializer().descriptor)
        element("default", Boolean.serializer().descriptor)
        element("persistent", Boolean.serializer().descriptor)
        element("transmit", Boolean.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: VariablesState) {
        encoder.encodeInt(value.definitions.size)
        for ((stringId, definition) in value.definitions) {
            encoder.encodeString(stringId)
            encoder.encodeByte(
                when (definition) {
                    is VariableDefinition.VarpDefinition -> VARP
                    is VariableDefinition.VarbitDefinition -> VARBIT
                    is VariableDefinition.VarcDefinition -> VARC
                    is VariableDefinition.VarcStrDefinition -> VARC_STR
                    is VariableDefinition.CustomVariableDefinition -> CUSTOM
                    else -> throw IllegalArgumentException("Unsupported variable type ${definition.javaClass}")
                }.toByte(),
            )
            encoder.encodeInt(definition.id)
            values(encoder, definition.values)
            encoder.encodeValue(definition.defaultValue)
            encoder.encodeBoolean(definition.persistent)
            encoder.encodeBoolean(definition.transmit)
        }
    }

    override fun deserialize(decoder: Decoder): VariablesState {
        val definitions = Object2ObjectOpenHashMap<String, VariableDefinition>()
        val varbitIds = Int2ObjectOpenHashMap<String>()
        val varpIds = Int2ObjectOpenHashMap<String>()
        repeat(decoder.decodeInt()) {
            val stringId = decoder.decodeString()
            val type = decoder.decodeByte().toInt()
            val id = decoder.decodeInt()
            val values = values(decoder)
            val default = decoder.decodeValue()
            val persistent = decoder.decodeBoolean()
            val transmit = decoder.decodeBoolean()
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
        return VariablesState(definitions, varbitIds, varpIds)
    }

    private fun values(encoder: Encoder, values: VariableValues) {
        when (values) {
            NoValues -> encoder.encodeByte(0)
            IntValues -> encoder.encodeByte(1)
            StringValues -> encoder.encodeByte(2)
            BooleanValues -> encoder.encodeByte(3)
            is ListValues -> {
                encoder.encodeByte(4)
                encoder.encodeValue(values.values)
            }
            is MapValues -> {
                encoder.encodeByte(5)
                encoder.encodeValue(values.values)
            }
            is BitwiseValues -> {
                encoder.encodeByte(6)
                encoder.encodeValue(values.values)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun values(decoder: Decoder): VariableValues = when (val tag = decoder.decodeByte().toInt()) {
        0 -> NoValues
        1 -> IntValues
        2 -> StringValues
        3 -> BooleanValues
        4 -> ListValues(decoder.decodeValue() as List<Any>)
        5 -> MapValues(decoder.decodeValue() as Map<Any, Int>)
        6 -> BitwiseValues(decoder.decodeValue() as List<Any>)
        else -> throw IllegalStateException("Unknown variable values tag $tag")
    }
}
