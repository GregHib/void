package world.gregs.voidps.network

import world.gregs.voidps.engine.entity.character.player.Players

class NetworkTask(
    private val players: Players,
    private val protocol: Map<Int, Decoder>
) : Runnable {

    override fun run() {
        players.forEach { player ->
            player.client?.packets?.let { packets ->
                for (packet in packets.replayCache) {
                    val decoder = protocol[packet.opcode]
                    decoder?.decode(player, packet.packet)
                }
                packets.resetReplayCache()
            }
        }
    }
}