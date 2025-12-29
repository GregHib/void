package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.TEXTURE_DEFINITIONS
import world.gregs.voidps.cache.definition.data.MaterialDefinition

class MaterialDecoder : DefinitionDecoder<MaterialDefinition>(TEXTURE_DEFINITIONS) {

    override fun create(size: Int) = Array(size) { MaterialDefinition(it) }

    override fun getArchive(id: Int) = 0

    override fun getFile(id: Int) = 0

    override fun load(cache: Cache): Array<MaterialDefinition> {
        val start = System.currentTimeMillis()
        val data = cache.data(index, 0, 0)!!
        val reader = ArrayReader(data)
        val size = reader.readShort()
        val definitions = create(size)
        load(definitions, reader)
        logger.info { "$size ${this::class.simpleName} definitions loaded in ${System.currentTimeMillis() - start}ms" }
        return definitions
    }

    override fun load(definitions: Array<MaterialDefinition>, reader: Reader) {
        val data = mutableListOf<MaterialDefinition>()
        for (index in definitions.indices) {
            if (reader.readUnsignedBoolean()) {
                data.add(definitions[index])
            }
        }

        for (texture in data) {
            texture.useTextureColour = !reader.readUnsignedBoolean()
        }
        for (texture in data) {
            texture.aBoolean1204 = reader.readUnsignedBoolean()
        }
        for (texture in data) {
            texture.aBoolean1205 = reader.readUnsignedBoolean()
        }
        for (texture in data) {
            texture.aByte1217 = reader.readByte().toByte()
        }
        for (texture in data) {
            texture.aByte1225 = reader.readByte().toByte()
        }
        for (texture in data) {
            texture.type = reader.readByte().toByte()
        }
        for (texture in data) {
            texture.aByte1213 = reader.readByte().toByte()
        }
        for (texture in data) {
            texture.colour = reader.readShort()
        }
        for (texture in data) {
            texture.aByte1211 = reader.readByte().toByte()
        }
        for (texture in data) {
            texture.aByte1203 = reader.readByte().toByte()
        }
        for (texture in data) {
            texture.aBoolean1222 = reader.readUnsignedBoolean()
        }
        for (texture in data) {
            texture.aBoolean1216 = reader.readUnsignedBoolean()
        }
        for (texture in data) {
            texture.aByte1207 = reader.readByte().toByte()
        }
        for (texture in data) {
            texture.aBoolean1212 = reader.readUnsignedBoolean()
        }
        for (texture in data) {
            texture.aBoolean1210 = reader.readUnsignedBoolean()
        }
        for (texture in data) {
            texture.aBoolean1215 = reader.readUnsignedBoolean()
        }
        for (texture in data) {
            texture.anInt1202 = reader.readUnsignedByte()
        }
        for (texture in data) {
            texture.anInt1206 = reader.readInt()
        }
        for (texture in data) {
            texture.anInt1226 = reader.readUnsignedByte()
        }
    }

    override fun MaterialDefinition.read(opcode: Int, buffer: Reader) = throw IllegalStateException("Shouldn't be used.")
}
