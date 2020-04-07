package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.STRUTS
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.StructDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class StrutDecoder : ConfigDecoder<StructDefinition>(STRUTS) {

    override fun create() = StructDefinition()

    override fun StructDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 249) {
            readParameters(buffer)
        }
    }
}