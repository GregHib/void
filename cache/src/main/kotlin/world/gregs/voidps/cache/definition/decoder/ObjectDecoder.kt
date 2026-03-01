package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.OBJECTS
import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.cache.definition.data.ObjectDefinition

open class ObjectDecoder(
    val member: Boolean = true,
    val lowDetail: Boolean = false,
    private val parameters: Parameters = Parameters.EMPTY,
) : DefinitionDecoder<ObjectDefinition>(OBJECTS) {

    override fun create(size: Int) = Array(size) { ObjectDefinition(it) }

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun ObjectDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> skip(buffer)
            5 -> {
                skip(buffer)
                skip(buffer)
            }
            2 -> name = buffer.readString()
            14 -> sizeX = buffer.readUnsignedByte()
            15 -> sizeY = buffer.readUnsignedByte()
            17 -> {
                solid = 0
                block = block and ObjectDefinition.PROJECTILE.inv()
            }
            18 -> {
                block = block and ObjectDefinition.PROJECTILE.inv()
            }
            19 -> interactive = buffer.readUnsignedByte()
            21, 22, 23, 64, 73, 82, 88, 89, 91, 94, 97, 98, 103, 105, 168, 169, 177 -> return
            24, 65, 66, 67, 70, 71, 72, 93, 95, 102, 107, 164, 165, 166, 167 -> buffer.skip(2)
            27 -> solid = 1
            28, 29, 39, 75, 81, 101, 104, 178 -> buffer.skip(1)
            in 30..34 -> {
                if (options == null) {
                    options = arrayOf(null, null, null, null, null, "Examine")
                }
                options!![opcode - 30] = buffer.readString()
            }
            40, 41 -> buffer.skip(buffer.readUnsignedByte() * 4)
            42 -> buffer.skip(buffer.readUnsignedByte())
            62 -> mirrored = true
            69 -> blockFlag = buffer.readUnsignedByte()
            74 -> block = block and ObjectDefinition.ROUTE.inv()
            77, 92 -> readTransforms(buffer, opcode == 92)
            78, 99, 100 -> buffer.skip(3)
            79 -> {
                buffer.skip(5)
                buffer.skip(buffer.readUnsignedByte() * 2)
            }
            106 -> buffer.skip(buffer.readUnsignedByte() * 3)
            in 150..154 -> {
                if (options == null) {
                    options = arrayOf(null, null, null, null, null, "Examine")
                }
                options!![opcode - 150] = buffer.readString()
                if (!member) {
                    options!![opcode - 150] = null
                }
            }
            160 -> buffer.skip(buffer.readUnsignedByte() * 2)
            162, 163, 173 -> buffer.skip(4)
            170, 171 -> buffer.readSmart()
            249 -> readParameters(buffer, parameters)
        }
    }

    companion object {
        private fun skip(buffer: Reader) {
            val length = buffer.readUnsignedByte()
            for (i in 0 until length) {
                buffer.skip(1)
                buffer.skip(buffer.readUnsignedByte() * 2)
            }
        }
    }
}
