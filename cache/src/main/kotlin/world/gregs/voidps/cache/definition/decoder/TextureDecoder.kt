package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.TEXTURE_DEFINITIONS
import world.gregs.voidps.cache.definition.data.TextureDefinition

class TextureDecoder : DefinitionDecoder<TextureDefinition>(TEXTURE_DEFINITIONS) {

    override fun create() = TextureDefinition()
    lateinit var data: Array<TextureDefinition?>

    var metricsCount = 0

    override fun size(cache: Cache): Int {
        return metricsCount
    }

    init {
        val data = getData(0, 0)
        if (data != null) {
            decode(BufferReader(data))
        }
    }

    override fun readData(id: Int) = data[id]

    fun decode(buffer: Reader) {
        metricsCount = buffer.readShort()
        data = arrayOfNulls(metricsCount)
        for (id in 0 until metricsCount) {
            if (buffer.readUnsignedBoolean()) {
                data[id] = TextureDefinition(id = id)
            }
        }

        for (texture in data) {
            texture?.useTextureColour = !buffer.readUnsignedBoolean()
        }
        for (texture in data) {
            texture?.aBoolean1204 = buffer.readUnsignedBoolean()
        }
        for (texture in data) {
            texture?.aBoolean1205 = buffer.readUnsignedBoolean()
        }
        for (texture in data) {
            texture?.aByte1217 = buffer.readByte().toByte()
        }
        for (texture in data) {
            texture?.aByte1225 = buffer.readByte().toByte()
        }
        for (texture in data) {
            texture?.type = buffer.readByte().toByte()
        }
        for (texture in data) {
            texture?.aByte1213 = buffer.readByte().toByte()
        }
        for (texture in data) {
            texture?.colour = buffer.readShort()
        }
        for (texture in data) {
            texture?.aByte1211 = buffer.readByte().toByte()
        }
        for (texture in data) {
            texture?.aByte1203 = buffer.readByte().toByte()
        }
        for (texture in data) {
            texture?.aBoolean1222 = buffer.readUnsignedBoolean()
        }
        for (texture in data) {
            texture?.aBoolean1216 = buffer.readUnsignedBoolean()
        }
        for (texture in data) {
            texture?.aByte1207 = buffer.readByte().toByte()
        }
        for (texture in data) {
            texture?.aBoolean1212 = buffer.readUnsignedBoolean()
        }
        for (texture in data) {
            texture?.aBoolean1210 = buffer.readUnsignedBoolean()
        }
        for (texture in data) {
            texture?.aBoolean1215 = buffer.readUnsignedBoolean()
        }
        for (texture in data) {
            texture?.anInt1202 = buffer.readUnsignedByte()
        }
        for (texture in data) {
            texture?.anInt1206 = buffer.readInt()
        }
        for (texture in data) {
            texture?.anInt1226 = buffer.readUnsignedByte()
        }
    }

    override fun TextureDefinition.read(opcode: Int, buffer: Reader) {
        throw IllegalStateException("Shouldn't be used.")
    }

    override fun clear() {
    }
}