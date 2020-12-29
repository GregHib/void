package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_SPRITE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceSpriteMessageEncoder : MessageEncoder(INTERFACE_SPRITE) {

    /**
     * Sends a sprite to a interface component
     * @param id The id of the parent interface
     * @param component The index of the component
     * @param sprite The sprite id
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int,
        sprite: Int
    ) = player.send(6) {
        writeShort(sprite, order = Endian.LITTLE)
        writeInt(id shl 16 or component)
    }
}