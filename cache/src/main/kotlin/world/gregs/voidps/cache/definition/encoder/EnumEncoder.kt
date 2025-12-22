package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.Unicode
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.EnumDefinition

class EnumEncoder : DefinitionEncoder<EnumDefinition> {

    override fun Writer.encode(definition: EnumDefinition) {
        if (definition.id == -1) {
            return
        }

        if (definition.keyType != 0.toChar()) {
            writeByte(1)
            writeByte(Unicode.charToByte(definition.keyType))
        }

        if (definition.valueType != 0.toChar()) {
            writeByte(2)
            writeByte(Unicode.charToByte(definition.valueType))
        }

        if (definition.defaultString != "null") {
            writeByte(3)
            writeString(definition.defaultString)
        }

        if (definition.defaultInt != 0) {
            writeByte(4)
            writeInt(definition.defaultInt)
        }

        if (definition.length != 0) {
            val map = definition.map
            if (map != null) {
                val strings = map.filterValues { it is String }.mapValues { it.value as String }
                if (strings.isNotEmpty()) {
                    writeByte(5)
                    writeShort(strings.size)
                    for ((key, value) in strings) {
                        writeInt(key)
                        writeString(value)
                    }
                } else {
                    val integers = map.filterValues { it is Int }.mapValues { it.value as Int }
                    if (integers.isNotEmpty()) {
                        writeByte(6)
                        writeShort(integers.size)
                        for ((key, value) in integers) {
                            writeInt(key)
                            writeInt(value)
                        }
                    }
                }
            }
        }
        writeByte(0)
    }
}
