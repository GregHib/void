package world.gregs.voidps.cache.type.codec

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.NpcType
import world.gregs.voidps.cache.type.Type
import java.io.File
import kotlin.collections.iterator

object NpcTypeCodec : TypeCodec<NpcType>() {

    override fun create(size: Int, block: (NpcType) -> Unit) = Array(size) { NpcType(it).also(block) }

    override fun read(type: NpcType, reader: Reader) {
        while (reader.position() < reader.length) {
            when (val opcode = reader.readUnsignedByte()) {
                0 -> break
                2 -> type.name = reader.readString()
                12 -> type.size = reader.readUnsignedByte()
                in 30..34 -> type.options[opcode - 30] = reader.readString()
                95 -> type.combat = reader.readShort()
                106, 118 -> readTransforms(type, reader, opcode == 118)
                119 -> type.walkMode = reader.readByte().toByte()
                127 -> type.renderEmote = reader.readShort()
                134 -> {
                    type.idleSound = reader.readShort()
                    if (type.idleSound == 65535) {
                        type.idleSound = -1
                    }
                    type.crawlSound = reader.readShort()
                    if (type.crawlSound == 65535) {
                        type.crawlSound = -1
                    }
                    type.walkSound = reader.readShort()
                    if (type.walkSound == 65535) {
                        type.walkSound = -1
                    }
                    type.runSound = reader.readShort()
                    if (type.runSound == 65535) {
                        type.runSound = -1
                    }
                    type.soundDistance = reader.readUnsignedByte()
                }
                in 150..154 -> type.options[opcode - 150] = reader.readString()
                249 -> type.params = readParams(reader)
            }
        }
    }

    override fun write(definition: NpcType, writer: Writer) {
        if (definition.id == -1) {
            return
        }

        val name = definition.name
        if (name != "null") {
            writer.writeByte(2)
            writer.writeString(name)
        }

        if (definition.size != 1) {
            writer.writeByte(12)
            writer.writeByte(definition.size)
        }

        val options = definition.options
        for (index in 0 until 5) {
            val option = options[index] ?: continue
            writer.writeByte(30 + index)
            writer.writeString(option)
        }

        if (definition.combat != -1) {
            writer.writeByte(95)
            writer.writeShort(definition.combat)
        }

        writeTransforms(writer, definition.transforms, definition.varbit, definition.varp, 106, 118)

        if (definition.renderEmote != -1) {
            writer.writeByte(127)
            writer.writeShort(definition.renderEmote)
        }

        if (definition.idleSound != -1 || definition.crawlSound != -1 || definition.walkSound != -1 || definition.runSound != -1 || definition.soundDistance != 0) {
            writer.writeByte(134)
            writer.writeShort(definition.idleSound)
            writer.writeShort(definition.crawlSound)
            writer.writeShort(definition.walkSound)
            writer.writeShort(definition.runSound)
            writer.writeByte(definition.soundDistance)
        }

        writeParams(writer, definition.params)

        writer.writeByte(0)
    }

    private fun readParams(reader: Reader): Map<Int, Any>? {
        val length = reader.readUnsignedByte()
        if (length == 0) {
            return null
        }
        val params = Int2ObjectArrayMap<Any>()
        for (i in 0 until length) {
            val string = reader.readUnsignedBoolean()
            val id = reader.readUnsignedMedium()
            params[id] = if (string) reader.readString() else reader.readInt()
        }
        return params
    }

    private fun writeTransforms(writer: Writer, transforms: IntArray?, varbit: Int, varp: Int, opcode: Int, extendedOpcode: Int) {
        if (transforms == null || !(varbit != -1 || varp != -1)) {
            return
        }
        val last = transforms.last()
        val extended = last != -1
        writer.writeByte(if (extended) extendedOpcode else opcode)
        writer.writeShort(varbit)
        writer.writeShort(varp)

        if (extended) {
            writer.writeShort(last)
        }
        writer.writeByte(transforms.size - 2)
        for (i in 0 until transforms.size - 1) {
            writer.writeShort(transforms[i])
        }
    }

    private fun readTransforms(type: NpcType, reader: Reader, isLast: Boolean) {
        type.varbit = reader.readShort()
        if (type.varbit == 65535) {
            type.varbit = -1
        }
        type.varp = reader.readShort()
        if (type.varp == 65535) {
            type.varp = -1
        }
        var last = -1
        if (isLast) {
            last = reader.readUnsignedShort()
            if (last == 65535) {
                last = -1
            }
        }
        val length = reader.readUnsignedByte()
        type.transforms = IntArray(length + 2)
        for (count in 0..length) {
            type.transforms!![count] = reader.readUnsignedShort()
            if (type.transforms!![count] == 65535) {
                type.transforms!![count] = -1
            }
        }
        type.transforms!![length + 1] = last
    }

    private fun writeParams(writer: Writer, params: Map<Int, Any>?) {
        if (params == null) {
            return
        }
        writer.writeByte(249)
        writer.writeByte(params.size)
        for ((id, value) in params) {
            writer.writeByte(value is String)
            writer.writeMedium(id)
            if (value is String) {
                writer.writeString(value)
            } else if (value is Int) {
                writer.writeInt(value)
            }
        }
    }
}