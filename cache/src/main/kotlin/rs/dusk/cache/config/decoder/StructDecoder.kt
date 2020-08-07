package rs.dusk.cache.config.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.STRUCTS
import rs.dusk.cache.config.ConfigDecoder
import rs.dusk.cache.config.data.StructDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class StructDecoder(cache: Cache) : ConfigDecoder<StructDefinition>(cache, STRUCTS) {

    override fun create() = StructDefinition()

    override fun StructDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 249) {
            readParameters(buffer)
        }
    }
}