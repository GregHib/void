package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition

class ClientScriptDecoder(cache: world.gregs.voidps.cache.Cache, private val revision634: Boolean) : DefinitionDecoder<ClientScriptDefinition>(cache, Indices.CLIENT_SCRIPTS) {

    override val last: Int
        get() = cache.lastArchiveId(index)

    override fun getFile(id: Int): Int {
        return 0
    }

    override fun create() = ClientScriptDefinition()

    override fun readLoop(definition: ClientScriptDefinition, buffer: Reader) {
        definition.read(-1, buffer)
    }

    override fun ClientScriptDefinition.read(opcode: Int, buffer: Reader) {
        buffer.position(buffer.length - 2)
        val i = buffer.readShort()
        val length: Int = buffer.length - (2 + i) - if (revision634) 12 else 16
        buffer.position(length)
        val instructionCount = buffer.readInt()
        intVariableCount = buffer.readShort()
        stringVariableCount = buffer.readShort()
        if (!revision634) {
            longVariableCount = buffer.readShort()
        }
        intArgumentCount = buffer.readShort()
        stringArgumentCount = buffer.readShort()
        if (!revision634) {
            longArgumentCount = buffer.readShort()
        }
        val count = buffer.readUnsignedByte()
        if (count > 0) {
            val list = mutableListOf<List<Pair<Int, Int>>>()
            repeat(count) {
                val size = buffer.readShort()
                val hashtable = mutableListOf<Pair<Int, Int>>()
                list.add(hashtable)
                repeat(size) {
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
            } else if (!revision634 && clientOpcode == 54) {
                if (longOperands == null) {
                    longOperands = LongArray(instructionCount)
                }
                longOperands!![index] = buffer.readLong()
            } else {
                if (intOperands == null) {
                    intOperands = IntArray(instructionCount)
                }
                if (clientOpcode < 100 && clientOpcode != 21 && clientOpcode != 38 && clientOpcode != 39) {
                    intOperands!![index] = buffer.readInt()
                } else {
                    intOperands!![index] = buffer.readUnsignedByte()
                }
            }
            instructions[index++] = clientOpcode
        }
    }

}