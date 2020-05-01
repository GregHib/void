package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.core.qualifier.named
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.npc.COMBAT_LEVEL_MASK
import rs.dusk.engine.entity.model.visual.visuals.npc.NAME_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.APPEARANCE_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.FACE_DIRECTION_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.MOVEMENT_SPEED_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.MOVEMENT_TYPE_MASK
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PreUpdateEncodingTask(tasks: EngineTasks) : ParallelEngineTask(tasks, 2) {

    private val logger = InlineLogger()
    val players: Players by inject()
    val npcs: NPCs by inject()

    val playerEncoders: Array<VisualEncoder<Visual>> by inject(named("playerVisualEncoders"))
    val npcEncoders: Array<VisualEncoder<Visual>> by inject(named("npcVisualEncoders"))

    // Order of these is important
    val playerMasks = intArrayOf(
        MOVEMENT_TYPE_MASK,
        APPEARANCE_MASK,
        MOVEMENT_SPEED_MASK,
        FACE_DIRECTION_MASK
    )

    val npcMasks = intArrayOf(
        NAME_MASK,
        COMBAT_LEVEL_MASK
    )

    override fun run() {
        players.forEach { player ->
            defers.add(update(player, playerEncoders, playerMasks, 0x800))
        }
//        npcs.forEach { npc ->
//            defers.add(update(npc, npcEncoders, npcMasks, 0x8000))
//        }
        val took = measureTimeMillis {
            super.run()
        }
        if (took > 0) {
            logger.info { "Update encoding took ${took}ms" }
        }
    }

    fun update(entity: Indexed, encoders: Array<VisualEncoder<Visual>>, masks: IntArray, mask: Int) =
        GlobalScope.async {
            val visuals = entity.visuals
            val flag = visuals.flag
            if (flag == 0) {
                visuals.update = null
                return@async
            }

            // Write update
            val updateWriter = BufferWriter()
            updateWriter.writeFlag(flag, mask)

            encoders.forEach { encoder ->
                if (!visuals.flagged(encoder.mask)) {
                    return@forEach
                }
                // Re-encode flagged aspects
                val visual = visuals.aspects[encoder.mask] ?: return@forEach// FIXME npc masks can overlap with players
                val writer = BufferWriter()
                encoder.encode(writer, visual)
                val encoded = writer.toArray()
                visuals.encoded[encoder.mask] = encoded
                // Update
                updateWriter.writeBytes(encoded)
            }

            visuals.update = updateWriter.toArray()

            // Re-write base
            val updateBase = masks.any { mask -> visuals.flagged(mask) }
            if (updateBase) {
                masks.forEach {
                    if (visuals.encoded[it] == null) {
                        val writer = BufferWriter()
                        val visual = visuals.aspects[mask]
                            ?: return@forEach logger.warn { "Unable to find base visual ${mask}." }
                        encoders.first { en -> en.mask == mask }.encode(writer, visual)
                        visuals.encoded[it] = writer.toArray()
                    }
                }
                val writer = BufferWriter()
                writer.writeFlag(masks.sum(), mask)
                masks.forEach { mask ->
                    val encoded = visuals.encoded[mask] ?: return@forEach
                    writer.writeBytes(encoded)
                }
                visuals.base = writer.toArray()
            }

            visuals.flag = 0
        }


    fun Writer.writeFlag(dataFlag: Int, mask: Int) {
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or 0x80
        }
        if (flag >= 0x10000) {
            flag = flag or mask
        }

        writeByte(flag)

        if (flag >= 0x100) {
            writeByte(flag shr 8)
        }
        if (flag >= 0x10000) {
            writeByte(flag shr 16)
        }
    }

}