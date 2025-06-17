package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition

class ClientScriptEncoder(private val revision667: Boolean = false) : DefinitionEncoder<ClientScriptDefinition> {

    override fun Writer.encode(definition: ClientScriptDefinition) {
        if (definition.id == -1) {
            return
        }
        writeString(definition.name)
        for ((index, instruction) in definition.instructions.withIndex()) {
            writeShort(instruction)
            when (instruction) {
                3 -> writeString(definition.stringOperands!![index])
                54 -> if (revision667) writeLong(definition.longOperands!![index])
                else -> {
                    if (instruction >= (if (revision667) 150 else 100) || instruction == 21 || instruction == 38 || instruction == 39) {
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
        if (revision667) {
            writeShort(definition.longVariableCount)
        }
        writeShort(definition.intArgumentCount)
        writeShort(definition.stringArgumentCount)
        if (revision667) {
            writeShort(definition.longArgumentCount)
        }
        val position = position()
        val table = definition.switchStatementIndices
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
