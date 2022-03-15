package world.gregs.voidps.engine.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.logging.InlineLogger
import java.io.File

class FileStorage private constructor(
    val mapper: ObjectMapper = yamlMapper(false)
) {

    constructor(quotes: Boolean = false, json: Boolean = false) : this(if (json) jsonMapper() else yamlMapper(quotes))

    init {
        mapper.findAndRegisterModules()
        logger.info { "YAML file mapper loaded." }
    }

    private val writer = mapper.writerWithDefaultPrettyPrinter()

    inline fun <reified T : Any> load(path: String): T = mapper.readValue(File(path), T::class.java)

    inline fun <reified T : Any> loadOrNull(path: String): T? {
        val file = File(path)
        return if (file.exists()) {
            try {
                mapper.readValue(file, T::class.java)
            } catch (e: MismatchedInputException) {
                logger.warn(e) { "Error reading file $path" }
                null
            }
        } else {
            null
        }
    }

    fun <T : Any> save(path: String, data: T, create: Boolean = false) {
        val file = File(path)
        if (create && !file.exists()) {
            file.createNewFile()
        }
        return save(file, data)
    }

    fun <T : Any> save(file: File, data: T) {
        return writer.writeValue(file, data)
    }

    companion object {
        val logger = InlineLogger()
        private fun yamlMapper(quotes: Boolean) = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).apply {
            if (!quotes) {
                enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            }
            disable(YAMLGenerator.Feature.SPLIT_LINES)
        })

        private fun jsonMapper() = jacksonObjectMapper().apply {
            enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
            val module = SimpleModule()
            module.addSerializer(DoubleArray::class.java, object : StdSerializer<DoubleArray>(DoubleArray::class.java) {
                override fun serialize(value: DoubleArray, gen: JsonGenerator, provider: SerializerProvider) {
                    gen.writeStartArray()
                    for (double in value) {
                        gen.writeNumber(double.toBigDecimal())
                    }
                    gen.writeEndArray()
                }
            })
            module.addSerializer(Double::class.java, object : StdSerializer<Double>(Double::class.java) {
                override fun serialize(value: Double, gen: JsonGenerator, provider: SerializerProvider) {
                    gen.writeNumber(value.toBigDecimal())
                }
            })
            registerModule(module)
        }
    }
}