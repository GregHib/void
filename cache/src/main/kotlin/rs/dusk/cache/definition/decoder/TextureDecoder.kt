package rs.dusk.cache.definition.decoder

import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices.TEXTURE_DEFINITIONS
import rs.dusk.cache.definition.data.TextureDefinition
import rs.dusk.core.io.read.BufferReader
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class TextureDecoder : DefinitionDecoder<TextureDefinition>(TEXTURE_DEFINITIONS) {

    override fun create() = TextureDefinition()

    var metricsCount = 0

    override val size: Int
        get() = metricsCount

    init {
        val data = getData(0, 0)
        if (data != null) {
            decode(BufferReader(data))
        }
    }

    override fun readData(id: Int) = dataCache[id]

    fun decode(buffer: Reader) {
        metricsCount = buffer.readShort()
        for (id in 0 until metricsCount) {
            if (buffer.readUnsignedBoolean()) {
                dataCache[id] = TextureDefinition(id = id)
            }
        }

        for (texture in dataCache.values) {
            texture.useTextureColour = !buffer.readUnsignedBoolean()
        }
        for (texture in dataCache.values) {
            texture.aBoolean1204 = buffer.readUnsignedBoolean()
        }
        for (texture in dataCache.values) {
            texture.aBoolean1205 = buffer.readUnsignedBoolean()
        }
        for (texture in dataCache.values) {
            texture.aByte1217 = buffer.readByte().toByte()
        }
        for (texture in dataCache.values) {
            texture.aByte1225 = buffer.readByte().toByte()
        }
        for (texture in dataCache.values) {
            texture.type = buffer.readByte().toByte()
        }
        for (texture in dataCache.values) {
            texture.aByte1213 = buffer.readByte().toByte()
        }
        for (texture in dataCache.values) {
            texture.colour = buffer.readShort()
        }
        for (texture in dataCache.values) {
            texture.aByte1211 = buffer.readByte().toByte()
        }
        for (texture in dataCache.values) {
            texture.aByte1203 = buffer.readByte().toByte()
        }
        for (texture in dataCache.values) {
            texture.aBoolean1222 = buffer.readUnsignedBoolean()
        }
        for (texture in dataCache.values) {
            texture.aBoolean1216 = buffer.readUnsignedBoolean()
        }
        for (texture in dataCache.values) {
            texture.aByte1207 = buffer.readByte().toByte()
        }
        for (texture in dataCache.values) {
            texture.aBoolean1212 = buffer.readUnsignedBoolean()
        }
        for (texture in dataCache.values) {
            texture.aBoolean1210 = buffer.readUnsignedBoolean()
        }
        for (texture in dataCache.values) {
            texture.aBoolean1215 = buffer.readUnsignedBoolean()
        }
        for (texture in dataCache.values) {
            texture.anInt1202 = buffer.readUnsignedByte()
        }
        for (texture in dataCache.values) {
            texture.anInt1206 = buffer.readInt()
        }
        for (texture in dataCache.values) {
            texture.anInt1226 = buffer.readUnsignedByte()
        }
    }

    override fun TextureDefinition.read(opcode: Int, buffer: Reader) {
        throw IllegalStateException("Shouldn't be used.")
    }

    override fun clear() {
    }
}