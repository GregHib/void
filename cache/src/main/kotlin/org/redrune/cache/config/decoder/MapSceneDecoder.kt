package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.MAP_SCENES
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.MapSceneDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class MapSceneDecoder : ConfigDecoder<MapSceneDefinition>(MAP_SCENES) {

    override fun create() = MapSceneDefinition()

    override fun MapSceneDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> sprite = buffer.readShort()
            2 -> colour = buffer.readMedium()
            3 -> aBoolean1741 = true
            4 -> sprite = -1
        }
    }
}