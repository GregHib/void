package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.CLIENT_VARP
import world.gregs.void.utility.get

/**
 * @author GregHib <greg@gregs.world>
 * @since July 04, 2020
 */
class VarpEncoder : Encoder(CLIENT_VARP) {

    /**
     * A variable player config; also known as "Config", known in the client as "clientvarp"
     * @param id The config id
     * @param value The value to pass to the config
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(3) {
        writeShort(id)
        writeByte(value, Modifier.ADD)
    }
}

fun Player.sendVarp(id: Int, value: Int) {
    if(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        get<VarpEncoder>().encode(this, id, value)
    } else {
        get<VarpLargeEncoder>().encode(this, id, value)
    }
}