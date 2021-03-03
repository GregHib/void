package world.gregs.voidps.cache.format.definition.internal

/*import world.gregs.voidps.cache.format.definition.Colours

@PublishedApi
@ExperimentalSerializationApi
@Serializer(forClass = Colours::class)
internal object ColourSerializer : KSerializer<Colours> {
    override val descriptor: SerialDescriptor = serialDescriptor<Colours>()

    override fun serialize(encoder: Encoder, value: Colours) {
        encoder.encodeInline(descriptor).apply {
            encodeByte(value.modified.size.toByte())
            for (i in value.modified.indices) {
                encodeShort(value.original[i])
                encodeShort(value.modified[i])
            }
        }
    }

    override fun deserialize(decoder: Decoder): Colours {
        val size = decoder.decodeByte().toInt()
        val original = ShortArray(size)
        val modified = ShortArray(size)
        repeat(size) { i ->
            original[i] = decoder.decodeShort()
            modified[i] = decoder.decodeShort()
        }
        return Colours(original, modified)
    }
}*/
