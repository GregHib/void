package rs.dusk.cache.definition.decoder

import rs.dusk.buffer.read.Reader
import rs.dusk.cache.Cache
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices
import rs.dusk.cache.definition.data.ClientScriptDefinition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since December 12, 2020
 */
class ClientScriptDecoder(cache: Cache) : DefinitionDecoder<ClientScriptDefinition>(cache, Indices.CLIENT_SCRIPTS) {

    override val size: Int
        get() = cache.lastArchiveId(index)

    override fun getFile(id: Int): Int {
        return 0
    }

    override fun create() = ClientScriptDefinition()

    override fun readLoop(definition: ClientScriptDefinition, buffer: Reader) {
        definition.read(-1, buffer)
    }

    override fun ClientScriptDefinition.read(opcode: Int, buffer: Reader) {
        buffer.buffer.position(buffer.length - 2)
        val i = buffer.readShort()
        val position: Int = buffer.length - (2 + i) - 16
        buffer.buffer.position(position)
        val instructionCount = buffer.readInt()
        intVariableCount = buffer.readShort()
        stringVariableCount = buffer.readShort()
        longVariableCount = buffer.readShort()
        intArgumentCount = buffer.readShort()
        stringArgumentCount = buffer.readShort()
        longArgumentCount = buffer.readShort()
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
            aHashTableArray9503 = list.toTypedArray()
        }
        buffer.buffer.position(0)
        name = buffer.readString()
        instructions = IntArray(instructionCount)
        var index = 0
        while (buffer.buffer.position() < position) {
            val clientOpcode = buffer.readShort()
            if (clientOpcode == 3) {
                if (stringOperands == null) {
                    stringOperands = arrayOfNulls(instructionCount)
                }
                stringOperands!![index] = buffer.readString().intern()
            } else if (clientOpcode == 54) {
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