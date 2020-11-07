package rs.dusk.engine.data.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import java.io.File

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class FileLoader(private val quotes: Boolean = false) {
    val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).apply {
        if (!quotes) {
            enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        }
        disable(YAMLGenerator.Feature.SPLIT_LINES)
    })
    private val logger = InlineLogger()

    init {
        mapper.findAndRegisterModules()
        logger.info { "YAML file mapper loaded." }
    }

    inline fun <reified T : Any> load(path: String) = mapper.readValue(File(path), T::class.java)

    inline fun <reified T : Any> loadOrNull(path: String): T? {
        val file = File(path)
        return if (file.exists()) {
            try {
                mapper.readValue(file, T::class.java)
            } catch (e: MismatchedInputException) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    fun <T : Any> save(path: String, data: T) {
        val file = File(path)
        return save(file, data)
    }

    fun <T : Any> save(file: File, data: T) {
        return mapper.writeValue(file, data)
    }

}

val fileLoaderModule = module {
    single { FileLoader() }
}