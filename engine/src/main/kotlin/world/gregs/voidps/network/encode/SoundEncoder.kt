package world.gregs.voidps.network.encode

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Protocol.PLAY_MUSIC
import world.gregs.voidps.network.writeByteSubtract
import world.gregs.voidps.network.writeShortAddLittle

fun Player.play(
    music: Int,
    delay: Int = 100,
    volume: Int = 255
) = client?.send(PLAY_MUSIC, 4) {
    writeByteSubtract(delay)
    writeByteSubtract(volume)
    writeShortAddLittle(music)
}