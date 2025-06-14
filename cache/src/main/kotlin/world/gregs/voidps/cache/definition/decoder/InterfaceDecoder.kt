package world.gregs.voidps.cache.definition.decoder

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.INTERFACES
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition

class InterfaceDecoder : DefinitionDecoder<InterfaceDefinition>(INTERFACES) {

    override fun create(size: Int) = Array(size) { InterfaceDefinition(it) }

    override fun size(cache: Cache): Int = cache.lastArchiveId(index)

    override fun load(definitions: Array<InterfaceDefinition>, reader: Reader) {
        val packed = readId(reader)
        val id = InterfaceDefinition.id(packed)
        val definition = definitions[id]
        val componentDefinition = InterfaceComponentDefinition(packed)
        if (!componentDefinition.isEmpty(reader)) {
            if (definition.components == null) {
                definition.components = Int2ObjectOpenHashMap(2)
            }
            definition.components!![InterfaceDefinition.componentId(packed)] = componentDefinition
        }
    }

    override fun load(definitions: Array<InterfaceDefinition>, cache: Cache, id: Int) {
        val archiveId = getArchive(id)
        val lastArchive = cache.lastFileId(index, archiveId)
        if (lastArchive == -1) {
            return
        }
        val definition = definitions[id]
        val components = Int2ObjectOpenHashMap<InterfaceComponentDefinition>(2)
        for (i in 0..lastArchive) {
            val data = cache.data(index, archiveId, i)
            if (data != null) {
                val componentDefinition = InterfaceComponentDefinition(id = InterfaceDefinition.pack(id, i))
                if (!componentDefinition.isEmpty(BufferReader(data))) {
                    components[i] = componentDefinition
                }
            }
        }
        if (components.isNotEmpty()) {
            definition.components = components
        }
    }

    private fun InterfaceComponentDefinition.isEmpty(buffer: Reader): Boolean {
        var empty = true
        buffer.readUnsignedByte()
        var type = buffer.readUnsignedByte()
        if (type and 0x80 != 0) {
            type = type and 0x7f
            buffer.readString()
        }
        buffer.skip(10)
        val horizontalSizeMode = buffer.readByte()
        val verticalSizeMode = buffer.readByte()
        buffer.skip(5)
        if (type == 0) {
            buffer.readShort()
            buffer.readShort()
            buffer.skip(1)
        } else if (type == 3) {
            buffer.skip(6)
        } else if (type == 4) {
            buffer.skip(2)
            buffer.readString()
            buffer.skip(9)
        } else if (type == 5) {
            buffer.skip(19)
        } else if (type == 6) {
            buffer.skip(2)
            val data = buffer.readUnsignedByte()
            val bool = 0x1 and data == 1
            val centreType = data and 0x2 == 2
            if (bool) {
                buffer.skip(12)
            } else if (centreType) {
                buffer.skip(14)
            }
            buffer.skip(2)
            if (horizontalSizeMode != 0) {
                buffer.skip(2)
            }
            if (verticalSizeMode != 0) {
                buffer.skip(2)
            }
        } else if (type == 9) {
            buffer.skip(6)
        }
        val setting = buffer.readUnsignedMedium()
        var code = buffer.readUnsignedByte()
        if (code != 0) {
            while (code != 0) {
                buffer.skip(3)
                code = buffer.readUnsignedByte()
            }
        }
        buffer.readString()
        val data = buffer.readUnsignedByte()
        val optionCount = data and 0xf
        if (optionCount > 0) {
            options = Array(optionCount) {
                val string = buffer.readString()
                if (string.isBlank()) {
                    null
                } else {
                    empty = false
                    string
                }
            }
        }
        val iconCount = data shr 4
        if (iconCount > 0) {
            buffer.skip(3)
        }
        if (iconCount > 1) {
            buffer.skip(3)
        }
        buffer.readString()
        buffer.skip(3)
        buffer.readString()
        if (setting and 0x3fda8 shr 11 != 0) {
            buffer.skip(6)
        }
        information = decodeScript(buffer)
        if (information != null) {
            empty = false
        }
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        skipScript(buffer)
        buffer.skip(buffer.readUnsignedByte() * 4)
        buffer.skip(buffer.readUnsignedByte() * 4)
        buffer.skip(buffer.readUnsignedByte() * 4)
        buffer.skip(buffer.readUnsignedByte() * 4)
        buffer.skip(buffer.readUnsignedByte() * 4)
        return empty
    }

    override fun InterfaceDefinition.read(opcode: Int, buffer: Reader) = throw IllegalStateException("Shouldn't be used.")

    companion object {
        private fun decodeScript(buffer: Reader): Array<Any>? {
            val length = buffer.readUnsignedByte()
            if (length == 0) {
                return null
            }
            return Array(length) { if (buffer.readUnsignedBoolean()) buffer.readString() else buffer.readInt() }
        }

        private fun skipScript(buffer: Reader) {
            val length = buffer.readUnsignedByte()
            if (length == 0) {
                return
            }
            for (i in 0 until length) {
                val string = buffer.readUnsignedBoolean()
                if (string) buffer.readString() else buffer.skip(4)
            }
        }
    }
}
