package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition

class ClientScriptDecoder(private val revision667: Boolean = false) : DefinitionDecoder<ClientScriptDefinition>(Index.CLIENT_SCRIPTS) {

    override fun size(cache: Cache): Int = cache.lastArchiveId(index)

    override fun create(size: Int) = Array(size) { ClientScriptDefinition(it) }

    override fun readId(reader: Reader): Int = reader.readShort()

    override fun getFile(id: Int): Int = 0

    override fun readLoop(definition: ClientScriptDefinition, buffer: Reader) {
        definition.read(-1, buffer)
    }

    override fun load(definitions: Array<ClientScriptDefinition>, reader: Reader) {
        val id = readId(reader)
        val length = reader.readInt()
        val bytes = ByteArray(length)
        reader.readBytes(bytes)
        read(definitions, id, ArrayReader(bytes))
    }

    override fun ClientScriptDefinition.read(opcode: Int, buffer: Reader) {
        buffer.position(buffer.length - 2)
        val offset = buffer.readShort()
        val length: Int = buffer.length - 2 - offset - (if (revision667) 16 else 12)
        buffer.position(length)
        val instructionCount = buffer.readInt()
        intVariableCount = buffer.readShort()
        stringVariableCount = buffer.readShort()
        if (revision667) {
            longVariableCount = buffer.readShort()
        }
        intArgumentCount = buffer.readShort()
        stringArgumentCount = buffer.readShort()
        if (revision667) {
            longArgumentCount = buffer.readShort()
        }
        val count = buffer.readUnsignedByte()
        if (count > 0) {
            val list = mutableListOf<List<Pair<Int, Int>>>()
            for (i in 0 until count) {
                val size = buffer.readShort()
                val hashtable = mutableListOf<Pair<Int, Int>>()
                list.add(hashtable)
                for (j in 0 until size) {
                    hashtable.add(buffer.readInt() to buffer.readInt())
                }
            }
            switchStatementIndices = list.toTypedArray()
        }
        buffer.position(0)
        name = buffer.readString()
        instructions = IntArray(instructionCount)
        var index = 0
        while (buffer.position() < length) {
            val clientOpcode = buffer.readShort()
            if (clientOpcode == 3) {
                if (stringOperands == null) {
                    stringOperands = arrayOfNulls(instructionCount)
                }
                stringOperands!![index] = buffer.readString().intern()
            } else if (revision667 && clientOpcode == 54) {
                if (longOperands == null) {
                    longOperands = LongArray(instructionCount)
                }
                longOperands!![index] = buffer.readLong()
            } else {
                if (intOperands == null) {
                    intOperands = IntArray(instructionCount)
                }
                if (clientOpcode < 150 && clientOpcode != 21 && clientOpcode != 38 && clientOpcode != 39) {
                    intOperands!![index] = buffer.readInt()
                } else {
                    intOperands!![index] = buffer.readUnsignedByte()
                }
            }
            instructions[index++] = clientOpcode
        }
    }
}
