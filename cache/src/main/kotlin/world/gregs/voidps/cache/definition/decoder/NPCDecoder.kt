package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.NPCS
import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.cache.definition.data.NPCDefinition

class NPCDecoder(
    val member: Boolean = true,
    private val parameters: Parameters = Parameters.EMPTY,
) : DefinitionDecoder<NPCDefinition>(NPCS) {

    override fun create(size: Int) = Array(size) { NPCDefinition(it, stringId = it.toString()) }

    override fun getFile(id: Int) = id and 0x7f

    override fun getArchive(id: Int) = id ushr 7

    override fun NPCDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1, 60, 160 -> buffer.skip(buffer.readUnsignedByte() * 2)
            2 -> name = buffer.readString()
            12 -> size = buffer.readUnsignedByte()
            in 30..34 -> options[-30 + opcode] = buffer.readString()
            40, 41, 121 -> buffer.skip(buffer.readUnsignedByte() * 4)
            42 -> buffer.skip(buffer.readUnsignedByte())
            93, 99, 107, 109, 111, 141, 143, 158, 159, 162 -> return
            95 -> combat = buffer.readShort()
            97, 98, 102, 103, 114, 122, 123, 137, 138, 139, 142 -> buffer.skip(2)
            100, 101, 125, 128, 140, 163, 165, 168 -> buffer.skip(1)
            106, 118 -> readTransforms(buffer, opcode == 118)
            113, 155, 164 -> buffer.skip(4)
            119 -> walkMode = buffer.readByte().toByte()
            127 -> renderEmote = buffer.readShort()
            134 -> {
                idleSound = buffer.readShort()
                if (idleSound == 65535) {
                    idleSound = -1
                }
                crawlSound = buffer.readShort()
                if (crawlSound == 65535) {
                    crawlSound = -1
                }
                walkSound = buffer.readShort()
                if (walkSound == 65535) {
                    walkSound = -1
                }
                runSound = buffer.readShort()
                if (runSound == 65535) {
                    runSound = -1
                }
                soundDistance = buffer.readUnsignedByte()
            }
            135, 136 -> buffer.skip(3)
            in 150..154 -> {
                options[opcode - 150] = buffer.readString()
                if (!member) {
                    options[opcode - 150] = null
                }
            }
            249 -> readParameters(buffer, parameters)
        }
    }
}
