package world.gregs.void.cache.config.decoder

import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.Cache
import world.gregs.void.cache.Configs.MAP_SCENES
import world.gregs.void.cache.config.ConfigDecoder
import world.gregs.void.cache.config.data.MapSceneDefinition

/**
 * @author GregHib <greg@gregs.world>
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