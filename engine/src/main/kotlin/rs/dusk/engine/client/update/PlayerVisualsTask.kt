package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.PooledMapList
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.Visual
import rs.dusk.engine.model.entity.index.update.VisualEncoder
import rs.dusk.engine.model.entity.index.update.Visuals
import rs.dusk.engine.model.entity.index.update.visual.player.Appearance
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PlayerVisualsTask(
    private val players: PooledMapList<Player>,
    private val encoders: Array<VisualEncoder<Visual>>,
    addMasks: IntArray // Order of these is important
) : ParallelEngineTask() {

    private val logger = InlineLogger()
    private val addFlag = addMasks.sum()
    private val addEncoders = addMasks.map { mask -> encoders.first { it.mask == mask } }

    override fun run() {
        players.forEach { entity ->
            defers.add(update(entity.visuals))
        }
        val took = measureTimeMillis {
            super.run()
        }
        if (took > 0) {
            logger.info { "Player visual encoding took ${took}ms" }
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
        if (addEncoders.any { encoder -> visuals.flagged(encoder.mask) }) {
            encodeAddition(visuals)
        }
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
            if (visual !is Appearance) {
                encoder.encode(writer, visual)
            } else {
                visuals.appearance = encodeAppearance(writer, encoder, visual)
            }
        }
        visuals.update = writer.toArray()
    }

    /**
     * Encodes [addEncoders] visuals into one reusable [Visuals.addition]
     */
    fun encodeAddition(visuals: Visuals) {
        val writer = BufferWriter()
        writeFlag(writer, addFlag)
        addEncoders.forEach { encoder ->
            val visual = visuals.aspects[encoder.mask] ?: return@forEach
            if (visual !is Appearance) {
                encoder.encode(writer, visual)
            } else {
                val data = visuals.appearance
                if (data != null) {
                    writer.writeBytes(data)
                } else {
                    visuals.appearance = encodeAppearance(writer, encoder, visual)
                }
            }
        }
        visuals.addition = writer.toArray()
    }

    /**
     * Returns byte array of encoded [appearance] and writes it to [writer]
     */
    fun encodeAppearance(writer: BufferWriter, encoder: VisualEncoder<Visual>, appearance: Appearance): ByteArray {
        val start = writer.buffer.writerIndex()
        encoder.encode(writer, appearance)
        val size = writer.buffer.writerIndex() - start
        val data = ByteArray(size)
        System.arraycopy(writer.buffer.array(), start, data, 0, size)
        return data
    }

    fun writeFlag(writer: Writer, dataFlag: Int) {
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or 0x80
        }
        if (flag >= 0x10000) {
            flag = flag or 0x800
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