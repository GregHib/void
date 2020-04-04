package org.redrune.engine.data.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import java.io.File

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class FileLoader {
    val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
    private val logger = InlineLogger()

    init {
        mapper.findAndRegisterModules()
        logger.info { "YAML file mapper loaded." }
    }

    inline fun <reified T : Any> load(path: String): T? {
        val file = File(path)
        return if (file.exists()) {
            mapper.readValue(file, T::class.java)
        } else {
            null
        }
    }

    fun <T : Any> save(path: String, data: T) {
        val file = File(path)
        return mapper.writeValue(file, data)
    }

}

val fileLoaderModule = module {
    single { FileLoader() }
}