package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.TEXTURE_DEFINITIONS
import world.gregs.voidps.cache.definition.data.TextureDefinition

class TextureDecoder(cache: world.gregs.voidps.cache.Cache) : DefinitionDecoder<TextureDefinition>(cache, TEXTURE_DEFINITIONS) {

    override fun create() = TextureDefinition()

    var metricsCount = 0

    override val last: Int
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