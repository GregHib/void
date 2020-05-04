package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.PooledMapList
import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.Visuals
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class VisualEncodingTask<T : Indexed>(
    val entities: PooledMapList<T>,
    val encoders: Array<VisualEncoder<Visual>>,
    val addMasks: IntArray, // Order of these is important
    val entityMask: Int,
    tasks: EngineTasks
) : ParallelEngineTask(tasks, 2) {

    private val logger = InlineLogger()

    val addFlag = addMasks.sum()
    val name = entities::class.java.simpleName

    override fun run() {
        entities.forEach { entity ->
            defers.add(update(entity.visuals))
        }
        val took = measureTimeMillis {
            super.run()
        }
        if (took > 0) {
            logger.info { "$name visual encoding took ${took}ms" }
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
        updateVisuals(visuals)
        encodeUpdate(visuals)
        if (addMasks.any { mask -> visuals.flagged(mask) }) {
            encodeAddition(visuals)
        }
        visuals.flag = 0
    }

    /**
     * Updates [Visuals.encoded] cache
     */
    fun updateVisuals(visuals: Visuals) {
        encoders.forEach { encoder ->
            val mask = encoder.mask
            val needsUpdate = visuals.flagged(mask)
            val isBlank = addMasks.contains(mask) && !visuals.encoded.containsKey(mask)

            if (needsUpdate || isBlank) {
                val visual = visuals.aspects[mask] ?: return@forEach

                val writer = BufferWriter()
                encoder.encode(writer, visual)
                visuals.encoded[mask] = writer.toArray()
            }
        }
    }

    /**
     * Encodes all flagged visuals into one reusable [Visuals.update]
     */
    fun encodeUpdate(visuals: Visuals) {
        val writer = BufferWriter()
        writeFlag(writer, visuals.flag, entityMask)
        encoders.forEach { encoder ->
            if (!visuals.flagged(encoder.mask)) {
                return@forEach
            }
            val data = visuals.encoded[encoder.mask] ?: return@forEach
            writer.writeBytes(data)
        }
        visuals.update = writer.toArray()
    }

    /**
     * Encodes [addMasks] visuals into one reusable [Visuals.addition]
     */
    fun encodeAddition(visuals: Visuals) {
        val writer = BufferWriter()
        writeFlag(writer, addFlag, entityMask)
        addMasks.forEach { mask ->
            val encoded = visuals.encoded[mask] ?: return@forEach
            writer.writeBytes(encoded)
        }
        visuals.addition = writer.toArray()
    }

    fun writeFlag(writer: Writer, dataFlag: Int, mask: Int) {
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or 0x80
        }
        if (flag >= 0x10000) {
            flag = flag or mask
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