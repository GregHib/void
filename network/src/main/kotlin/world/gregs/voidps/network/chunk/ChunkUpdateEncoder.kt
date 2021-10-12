package world.gregs.voidps.network.chunk

import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.encode.*
import world.gregs.voidps.network.chunk.update.*
import world.gregs.voidps.network.writeByteInverse
import world.gregs.voidps.network.writeByteSubtract

class ChunkUpdateEncoder {

    private val objectAdditionEncoder = ObjectAdditionEncoder()
    private val objectAnimationEncoder = ObjectAnimationEncoder()
    private val objectRemoveEncoder = ObjectRemoveEncoder()
    private val floorItemAdditionEncoder = FloorItemAdditionEncoder()
    private val floorItemRemovalEncoder = FloorItemRemovalEncoder()
    private val floorItemRevealEncoder = FloorItemRevealEncoder()
    private val floorItemUpdateEncoder = FloorItemUpdateEncoder()
    private val graphicAdditionEncoder = GraphicAdditionEncoder()
    private val projectileAdditionEncoder = ProjectileAdditionEncoder()
    private val soundAdditionEncoder = SoundAdditionEncoder()

    fun encode(client: Client, messages: List<ChunkUpdate>, chunkOffsetX: Int, chunkOffsetY: Int, chunkPlane: Int) {
        client.send(Protocol.BATCH_UPDATE_CHUNK, messages.sumBy { it.size + 1 } + 3, Client.SHORT) {
            writeByteInverse(chunkOffsetX)
            writeByteSubtract(chunkPlane)
            writeByteSubtract(chunkOffsetY)
            messages.forEach { update ->
                when (update) {
                    is ObjectAddition -> objectAdditionEncoder.encode(this, update)
                    is ObjectAnimation -> objectAnimationEncoder.encode(this, update)
                    is ObjectRemoval -> objectRemoveEncoder.encode(this, update)
                    is FloorItemAddition -> floorItemAdditionEncoder.encode(this, update)
                    is FloorItemRemoval -> floorItemRemovalEncoder.encode(this, update)
                    is FloorItemReveal -> floorItemRevealEncoder.encode(this, update)
                    is FloorItemUpdate -> floorItemUpdateEncoder.encode(this, update)
                    is GraphicAddition -> graphicAdditionEncoder.encode(this, update)
                    is ProjectileAddition -> projectileAdditionEncoder.encode(this, update)
                    is SoundAddition -> soundAdditionEncoder.encode(this, update)
                }
            }
        }
    }
}