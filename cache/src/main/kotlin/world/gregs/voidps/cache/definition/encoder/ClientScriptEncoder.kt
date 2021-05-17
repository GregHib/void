package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition

class ClientScriptEncoder(private val revision634: Boolean = false) : DefinitionEncoder<ClientScriptDefinition> {

    override fun Writer.encode(definition: ClientScriptDefinition) {
        if (definition.id == -1) {
            return
        }
        writeString(definition.name)
        for ((index, instruction) in definition.instructions.withIndex()) {
            writeShort(instruction)
            when (instruction) {
                3 -> writeString(definition.stringOperands!![index])
                54 -> if (!revision634) writeLong(definition.longOperands!![index])
                else -> {
                    if (instruction >= (if (revision634) 100 else 150) || instruction == 21 || instruction == 38 || instruction == 39) {
                        writeByte(definition.intOperands!![index])
                    } else {
                        writeInt(definition.intOperands!![index])
                    }
                }
            }
        }
        writeInt(definition.instructions.size)
        writeShort(definition.intVariableCount)
        writeShort(definition.stringVariableCount)
        if (!revision634) {
            writeShort(definition.longVariableCount)
        }
        writeShort(definition.intArgumentCount)
        writeShort(definition.stringArgumentCount)
        if (!revision634) {
            writeShort(definition.longArgumentCount)
        }
        val position = position()
        val table = definition.aHashTableArray9503
        if (table.isNullOrEmpty()) {
            writeByte(0)
        } else {
            writeByte(table.size)
            table.forEach { list ->
                writeShort(list.size)
                list.forEach { (key, value) ->
                    writeInt(key)
                    writeInt(value)
                }
            }
        }
        writeShort(position() - position)
    }

}