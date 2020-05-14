package rs.dusk.engine.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.PooledMapList
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.update.Visual
import rs.dusk.engine.model.entity.index.update.VisualEncoder
import rs.dusk.engine.model.entity.index.update.Visuals
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class NPCVisualsTask(
    private val npcs: PooledMapList<NPC>,
    private val encoders: Array<VisualEncoder<Visual>>
) : ParallelEngineTask() {

    private val logger = InlineLogger()

    override fun run() {
        npcs.forEach { entity ->
            defers.add(update(entity.visuals))
        }
        val took = measureTimeMillis {
            super.run()
        }
        if (took > 0) {
            logger.info { "NPC visual encoding took ${took}ms" }
        }
    }

    /**
     * Encodes [Visual] changes into an insertion and delta update
     */
    fun update(visuals: Visuals) = GlobalScope.async {
        if (visuals.flag == 0) {
            visuals.update = null
            return@async
        }
        encodeUpdate(visuals)
        visuals.flag = 0
    }

    /**
     * Encodes all flagged visuals into one reusable [Visuals.update]
     */
    fun encodeUpdate(visuals: Visuals) {
        val writer = BufferWriter()
        writeFlag(writer, visuals.flag)
        encoders.forEach { encoder ->
            if (!visuals.flagged(encoder.mask)) {
                return@forEach
            }
            val visual = visuals.aspects[encoder.mask] ?: return@forEach
            encoder.encode(writer, visual)
        }
        visuals.update = writer.toArray()
    }

    fun writeFlag(writer: Writer, dataFlag: Int) {
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or 0x80
        }
        if (flag >= 0x10000) {
            flag = flag or 0x8000
        }

        writer.writeByte(flag)

        if (flag >= 0x100) {
            writer.writeByte(flag shr 8)
        }
        if (flag >= 0x10000) {
            writer.writeByte(flag shr 16)
        }
    }

}