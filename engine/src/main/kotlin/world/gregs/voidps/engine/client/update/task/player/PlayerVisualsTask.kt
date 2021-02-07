package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.player.Appearance
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class PlayerVisualsTask(
    override val entities: PooledMapList<Player>,
    private val encoders: Array<VisualEncoder<Visual>>,
    addMasks: IntArray // Order of these is important
) : EntityTask<Player>() {

    private val addFlag = addMasks.sum()
    private val addEncoders = addMasks.map { mask -> encoders.first { it.mask == mask } }

    /**
     * Encodes [Visual] changes into an insertion and delta update
     */
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(player: Player) {
        val visuals = player.visuals
        if (visuals.flag == 0) {
            visuals.update = null
            return
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
        val reversed = data.reversedArray()
        writer.position(start)
        writer.writeBytes(reversed)
        return reversed
    }

    fun writeFlag(writer: Writer, dataFlag: Int) {
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or 0x40
        }
        if (flag >= 0x10000) {
            flag = flag or 0x4000
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