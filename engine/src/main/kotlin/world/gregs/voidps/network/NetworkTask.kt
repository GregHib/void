package world.gregs.voidps.network

import world.gregs.voidps.engine.entity.character.player.Players

class NetworkTask(
    private val players: Players,
    private val codec: NetworkCodec
) : Runnable {

    override fun run() {
        players.forEach { player ->
            player.client?.packets?.let { packets ->
                for (packet in packets.replayCache) {
                    val decoder = codec.getDecoder(packet.opcode)
                    decoder?.decode(player, packet.packet)
                }
                packets.resetReplayCache()
            }
        }
    }
}