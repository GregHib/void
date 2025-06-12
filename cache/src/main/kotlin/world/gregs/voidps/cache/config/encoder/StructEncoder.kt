package world.gregs.voidps.cache.config.encoder

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.config.ConfigEncoder
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.definition.Parameters

class StructEncoder(parameters: Parameters) : ConfigEncoder<StructDefinition>() {

    private val parameters = parameters.parameters.map { it.value to it.key }.toMap()
    private val logger = InlineLogger()

    override fun Writer.encode(definition: StructDefinition) {
        val extras = definition.extras
        if (!extras.isNullOrEmpty()) {
            writeByte(249)
            val count = extras.count { parameters.containsKey(it.key) && (it.value is String || it.value is Int || it.value is Short || it.value is Byte || it.value is Boolean) }
            writeByte(count)
            for ((key, value) in extras) {
                val id = parameters[key]
                if (id == null) {
                    logger.warn { "Unknown parameter: $key in struct ${definition.id}" }
                    continue
                }
                when (value) {
                    is String -> {
                        writeByte(true)
                        writeMedium(id)
                        writeString(value)
                    }
                    is Int -> {
                        writeByte(false)
                        writeMedium(id)
                        writeInt(value)
                    }
                    is Short -> {
                        writeByte(false)
                        writeMedium(id)
                        writeInt(value.toInt())
                    }
                    is Byte -> {
                        writeByte(false)
                        writeMedium(id)
                        writeInt(value.toInt())
                    }
                    is Boolean -> {
                        writeByte(false)
                        writeMedium(id)
                        writeInt(if (value) 1 else 0)
                    }
                    else -> logger.warn { "Unsupported value type '${value::class}' for $key in struct ${definition.id}" }
                }
            }
        }
        writeByte(0)
    }
}