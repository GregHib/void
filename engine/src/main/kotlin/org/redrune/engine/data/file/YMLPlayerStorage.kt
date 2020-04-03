package org.redrune.engine.data.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import org.redrune.engine.data.StorageStrategy
import org.redrune.engine.entity.model.Player
import java.io.File

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class YMLPlayerStorage(private val path: String) : StorageStrategy<Player> {
    private val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
    private val logger = InlineLogger()

    init {
        mapper.findAndRegisterModules()
        logger.info { "YAML mapper loaded." }
    }

    private fun file(name: String) = File("$path\\$name.yml")

    override fun load(name: String): Player? {
        val file = file(name)
        return if (file.exists()) {
            mapper.readValue(file, Player::class.java)
        } else {
            null
        }
    }

    override fun save(name: String, data: Player) {
        val file = file(name)
        return mapper.writeValue(file, data)
    }
}

val ymlPlayerModule = module {
    single { YMLPlayerStorage(getProperty("savePath")) as StorageStrategy<Player> }
}
