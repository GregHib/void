package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.CLIENT_VARP_LARGE

/**
 * @author GregHib <greg@gregs.world>
 * @since July 04, 2020
 */
class VarpLargeEncoder : Encoder(CLIENT_VARP_LARGE) {

    /**
     * A variable player config; also known as "Config", known in the client as "clientvarp"
     * @param id The config id
     * @param value The value to pass to the config
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(6) {
        writeInt(value, Modifier.INVERSE, Endian.MIDDLE)
        writeShort(id, Modifier.ADD)
    }
}