package world.gregs.voidps.cache.type.data

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.Transforms
import world.gregs.voidps.cache.type.Params
import world.gregs.voidps.cache.type.Type

/**
 * A combination of useful [world.gregs.voidps.cache.definition.data.NPCDefinitionFull]
 * values and toml [Params]
 */
class NpcType(override val id: Int = -1) : Type, Params, Transforms, Parameterized {
    var name: String = "null"
        internal set
    var size: Int = 1
        internal set
    var options: Array<String?> = arrayOf(null, null, null, null, null, "Examine")
        internal set
    var combat: Int = -1
        internal set
    override var varbit: Int = -1
    override var varp: Int = -1
    override var transforms: IntArray? = null
    var walkMode: Byte = 0
        internal set
    var renderEmote: Int = -1
        internal set
    var idleSound: Int = -1
        internal set
    var crawlSound: Int = -1
        internal set
    var walkSound: Int = -1
        internal set
    var runSound: Int = -1
        internal set
    var soundDistance: Int = 0
        internal set
    override var stringId: String = ""
    override var params: Map<Int, Any>? = null

    override fun decode(reader: Reader) {
        while (true) {
            when (val opcode = reader.readUnsignedByte()) {
                0 -> break
                1, 60, 160 -> reader.skip(reader.readUnsignedByte() * 2)
                2 -> name = reader.readString()
                12 -> size = reader.readUnsignedByte()
                in 30..34 -> options[-30 + opcode] = reader.readString()
                40, 41, 121 -> reader.skip(reader.readUnsignedByte() * 4)
                42 -> reader.skip(reader.readUnsignedByte())
                93, 99, 107, 109, 111, 141, 143, 158, 159, 162 -> return
                95 -> combat = reader.readShort()
                97, 98, 102, 103, 114, 122, 123, 137, 138, 139, 142 -> reader.skip(2)
                100, 101, 125, 128, 140, 163, 165, 168 -> reader.skip(1)
                106, 118 -> readTransforms(reader, opcode == 118)
                113, 155, 164 -> reader.skip(4)
                119 -> walkMode = reader.readByte().toByte()
                127 -> renderEmote = reader.readShort()
                134 -> {
                    idleSound = reader.readShort()
                    if (idleSound == 65535) {
                        idleSound = -1
                    }
                    crawlSound = reader.readShort()
                    if (crawlSound == 65535) {
                        crawlSound = -1
                    }
                    walkSound = reader.readShort()
                    if (walkSound == 65535) {
                        walkSound = -1
                    }
                    runSound = reader.readShort()
                    if (runSound == 65535) {
                        runSound = -1
                    }
                    soundDistance = reader.readUnsignedByte()
                }
                135, 136 -> reader.skip(3)
                in 150..154 -> options[opcode - 150] = reader.readString()
                249 -> readParameters(reader)
            }
        }
    }

    override fun encode(writer: Writer) {
        if (id == -1) {
            return
        }
        if (name != "null") {
            writer.writeByte(2)
            writer.writeString(name)
        }
        if (size != 1) {
            writer.writeByte(12)
            writer.writeByte(size)
        }
        for (index in 0 until 5) {
            val option = options[index] ?: continue
            writer.writeByte(30 + index)
            writer.writeString(option)
        }
        if (combat != -1) {
            writer.writeByte(95)
            writer.writeShort(combat)
        }
        writeTransforms(writer, 106, 118)
        if (walkMode.toInt() != 0) {
            writer.writeByte(119)
            writer.writeByte(walkMode.toInt())
        }
        if (renderEmote != -1) {
            writer.writeByte(127)
            writer.writeShort(renderEmote)
        }
        if (idleSound != -1 || crawlSound != -1 || walkSound != -1 || runSound != -1 || soundDistance != 0) {
            writer.writeByte(134)
            writer.writeShort(idleSound)
            writer.writeShort(crawlSound)
            writer.writeShort(walkSound)
            writer.writeShort(runSound)
            writer.writeByte(soundDistance)
        }
        // FIXME member options
//        for (index in 0 until 5) {
//            val option = options[index] ?: continue
//            writer.writeByte(150 + index)
//            writer.writeString(option)
//        }
        writeParameters(writer)
        writer.writeByte(0)
    }

    override fun toString(): String {
        return "NpcType(id=$id, name='$name', size=$size, options=${options.contentToString()}, combat=$combat, varbit=$varbit, varp=$varp, transforms=${transforms.contentToString()}, walkMode=$walkMode, renderEmote=$renderEmote, idleSound=$idleSound, crawlSound=$crawlSound, walkSound=$walkSound, runSound=$runSound, soundDistance=$soundDistance, stringId='$stringId', params=$params)"
    }
}