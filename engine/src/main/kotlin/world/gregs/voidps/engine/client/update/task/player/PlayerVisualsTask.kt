package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.task.VisualsTask
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.player.Appearance

class PlayerVisualsTask(
    characters: CharacterList<Player>,
    encoders: Array<VisualEncoder<Visual>>,
    addMasks: IntArray
) : VisualsTask<Player>(
    characters,
    encoders,
    addMasks
) {

    private val addFlag = addMasks.sum()

    /**
     * Encodes all flagged visuals into one reusable [Visuals.update]
     */
    override fun encodeUpdate(visuals: Visuals) {
        val writer = BufferWriter(128)
        writeFlag(writer, visuals.flag)
        encoders.forEach { encoder ->
            if (!visuals.flagged(encoder.mask)) {
                return@forEach
            }
            val visual = visuals.aspects[encoder.mask] ?: return@forEach
            if (visual !is Appearance) {
                encoder.encodeVisual(writer, visual)
            } else {
                visuals.appearance = encodeAppearance(writer, encoder, visual)
            }
        }
        visuals.update = writer.toArray()
    }

    /**
     * Encodes [addEncoders] visuals into one reusable [Visuals.addition]
     */
    override fun encodeAddition(visuals: Visuals) {
        val writer = BufferWriter()
        writeFlag(writer, addFlag)
        addEncoders.forEach { encoder ->
            val visual = visuals.aspects[encoder.mask] ?: return@forEach
            if (visual !is Appearance) {
                encoder.encodeVisual(writer, visual)
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
    private fun encodeAppearance(writer: BufferWriter, encoder: VisualEncoder<out Visual>, appearance: Appearance): ByteArray {
        val start = writer.position()
        encoder.encodeVisual(writer, appearance)
        val size = writer.position() - start
        val data = ByteArray(size)
        System.arraycopy(writer.array(), start, data, 0, size)
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