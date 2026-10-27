package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.client.variable.BooleanValues
import world.gregs.voidps.engine.client.variable.IntValues
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.client.variable.NoValues
import world.gregs.voidps.engine.client.variable.StringValues
import world.gregs.voidps.engine.client.variable.VariableValues
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.snapshot.decodeValue
import world.gregs.voidps.engine.data.snapshot.encodeValue

internal object VariablesStateSerializer : KSerializer<VariablesState> {
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
