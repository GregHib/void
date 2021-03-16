package world.gregs.voidps.network

import world.gregs.voidps.engine.entity.character.player.Players

class NetworkTask(
    private val players: Players,
    private val codec: NetworkCodec
) : Runnable {

    override fun run() {
        players.forEach {
            for (packet in it.client?.packets?.replayCache ?: return@forEach) {
                val decoder = codec.getDecoder(packet.opcode)
                decoder?.decode(it, packet.packet)
            }
            it.client?.packets?.resetReplayCache()
        }
    }
}