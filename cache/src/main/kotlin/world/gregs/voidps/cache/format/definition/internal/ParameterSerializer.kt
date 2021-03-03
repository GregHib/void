package world.gregs.voidps.cache.format.definition.internal

/*

@PublishedApi
@ExperimentalSerializationApi
internal object ParameterSerializer : KSerializer<Map<Int, Any>> {
    override val descriptor: SerialDescriptor = mapSerialDescriptor(Int.serializer().descriptor, String.serializer().descriptor)

    override fun serialize(encoder: Encoder, value: Map<Int, Any>) {
        encoder as DefinitionEncoder
        val writer = encoder.writer
        writer.writeByte(value.size)
        for ((key, v) in value) {
            writer.writeByte(v is String)
            writer.writeMedium(key)
            if (v is String) {
                writer.writeString(v)
            } else if (v is Int) {
                writer.writeInt(v)
            }
        }
    }

    override fun deserialize(decoder: Decoder): Map<Int, Any> {
        decoder as DefinitionDecoder
        val reader = decoder.reader
        val size = reader.readByte()
        val map = mutableMapOf<Int, Any>()
        repeat(size) {
            val string = reader.readBoolean()
            val id = reader.readMedium()
            map[id] = if (string) reader.readString() else reader.readInt()
        }
        return map
    }
}
*/
