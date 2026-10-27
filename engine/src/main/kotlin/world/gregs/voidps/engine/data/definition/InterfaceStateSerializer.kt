package world.gregs.voidps.engine.data.definition

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.data.snapshot.serializer.DefinitionArraySerializer
import world.gregs.voidps.engine.data.snapshot.serializer.StringIdsSerializer

internal object InterfaceStateSerializer : KSerializer<InterfaceState> {
    private val definitions = DefinitionArraySerializer(InterfaceDefinition::class, InterfaceDefinition.serializer())
    private val ids = StringIdsSerializer(default = -1)
    private val componentIds = StringIdsSerializer(default = null)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.engine.data.definition.InterfaceState") {
        element("definitions", definitions.descriptor)
        element("ids", ids.descriptor)
        element("componentIds", componentIds.descriptor)
    }

    override fun serialize(encoder: Encoder, value: InterfaceState) {
        definitions.serialize(encoder, value.definitions)
        ids.serialize(encoder, value.ids)
        componentIds.serialize(encoder, value.componentIds)
    }

    override fun deserialize(decoder: Decoder) = InterfaceState(definitions.deserialize(decoder), ids.deserialize(decoder), componentIds.deserialize(decoder))
}
