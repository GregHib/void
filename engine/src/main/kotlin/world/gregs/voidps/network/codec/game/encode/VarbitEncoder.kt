package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.CLIENT_VARBIT
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
 * @since July 04, 2020
 */
class VarbitEncoder : Encoder(CLIENT_VARBIT) {

    /**
     * A variable bit; also known as "ConfigFile", known in the client as "clientvarpbit"
     * @param id The file id
     * @param value The value to pass to the config file
     */
    fun encode(
        player: Player,
        id: Int,
        value: Int
    ) = player.send(3) {
        writeByte(value, type = Modifier.SUBTRACT)
        writeShort(id, type = Modifier.ADD)
    }
}

fun Player.sendVarbit(id: Int, value: Int) {
    if(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
        get<VarbitEncoder>().encode(this, id, value)
    } else {
        get<VarbitLargeEncoder>().encode(this, id, value)
    }
}