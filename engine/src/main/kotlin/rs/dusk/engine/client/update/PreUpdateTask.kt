package rs.dusk.engine.client.update

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.EngineTask
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.Visuals
import rs.dusk.utility.inject
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PreUpdateTask(tasks: EngineTasks) : EngineTask() {

    val defers: Deque<Deferred<Unit>> = LinkedList()
    val players: Players by inject()
    val npcs: NPCs by inject()

    val playerEncoders: Array<VisualEncoder<Visual>> by inject(named("playerVisualEncoders"))
    val npcEncoders: Array<VisualEncoder<Visual>> by inject(named("npcVisualEncoders"))

    init {
        tasks.add(this)
    }

    override fun run() = runBlocking {
        players.forEach { player ->
            defers.add(update(player, playerEncoders, 0x800))
        }
        npcs.forEach { npc ->
            defers.add(update(npc, npcEncoders, 0x8000))
        }
        while (defers.isNotEmpty()) {
            defers.poll().await()
        }
    }

    fun update(entity: Indexed, encoders: Array<VisualEncoder<Visual>>, mask: Int) = GlobalScope.async {
        val visuals = entity.visuals
        if (visuals.flag == 0) {
            return@async
        }
        val writer = BufferWriter()
        writer.writeFlag(visuals, mask)
        encoders.forEach { encoder ->
            val visual = visuals.aspects[encoder.clazz] ?: return@forEach
            encoder.encode(writer, visual)
        }
        visuals.encoded = writer.buffer.array()
    }

    fun Writer.writeFlag(visuals: Visuals, mask: Int) {
        var dataFlag = visuals.flag

        if (dataFlag >= 0x100) {
            dataFlag = dataFlag or 0x80
        }
        if (dataFlag >= 0x10000) {
            dataFlag = dataFlag or mask
        }

        writeByte(dataFlag)

        if (dataFlag >= 0x100) {
            writeByte(dataFlag shr 8)
        }
        if (dataFlag >= 0x10000) {
            writeByte(dataFlag shr 16)
        }
    }

}