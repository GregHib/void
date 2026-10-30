package content.bot.behaviour

import content.bot.behaviour.navigation.NavigationGraph
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.engine.data.snapshot.decodeValue
import world.gregs.voidps.engine.data.snapshot.encodeValue
import world.gregs.voidps.type.Tile

internal object NavigationEdgesSerializer : KSerializer<List<NavigationGraph.Companion.Edge>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("content.bot.behaviour.navigation.NavigationEdges") {
        element("from", Tile.serializer().descriptor)
        element("to", Tile.serializer().descriptor)
        element("cost", Int.serializer().descriptor)
        element("actions", String.serializer().descriptor)
        element("requirements", String.serializer().descriptor)
        element("debug", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: List<NavigationGraph.Companion.Edge>) {
        encoder.encodeInt(value.size)
        for (edge in value) {
            encoder.encodeInt(edge.from.id)
            encoder.encodeInt(edge.to.id)
            encoder.encodeInt(edge.cost)
            encoder.encodeInt(edge.actions.size)
            for (action in edge.actions) {
                encoder.encodeValue(action)
            }
            encoder.encodeInt(edge.requirements.size)
            for (requirement in edge.requirements) {
                encoder.encodeValue(requirement)
            }
            encoder.encodeString(edge.debug)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): List<NavigationGraph.Companion.Edge> = List(decoder.decodeInt()) {
        NavigationGraph.Companion.Edge(
            Tile(decoder.decodeInt()),
            Tile(decoder.decodeInt()),
            decoder.decodeInt(),
            List(decoder.decodeInt()) { decoder.decodeValue() as Pair<String, Map<String, Any>> },
            List(decoder.decodeInt()) { decoder.decodeValue() as Pair<String, List<Map<String, Any>>> },
            decoder.decodeString(),
        )
    }
}
