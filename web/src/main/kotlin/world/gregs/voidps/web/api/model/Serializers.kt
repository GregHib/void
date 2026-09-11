package world.gregs.voidps.web.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Every timestamp on the wire is an RFC 3339 instant in UTC. Model files opt in with
 * `@file:UseSerializers(InstantSerializer::class)` rather than annotating each property.
 *
 * `kotlin.time.Duration` needs no equivalent — kotlinx-serialization already encodes it as the
 * ISO-8601 string the spec asks for (`PT48H`).
 */
object InstantSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(DateTimeFormatter.ISO_INSTANT.format(value))
    }

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}
