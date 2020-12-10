package rs.dusk.cache.config.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.MAP_SCENES
import rs.dusk.cache.config.ConfigDecoder
import rs.dusk.cache.config.data.MapSceneDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class MapSceneDecoder(cache: Cache) : ConfigDecoder<MapSceneDefinition>(cache, MAP_SCENES) {

    override fun create() = MapSceneDefinition()

    override fun MapSceneDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> sprite = buffer.readShort()
            2 -> colour = buffer.readUnsignedMedium()
            3 -> aBoolean1741 = true
            4 -> sprite = -1
        }
    }
}