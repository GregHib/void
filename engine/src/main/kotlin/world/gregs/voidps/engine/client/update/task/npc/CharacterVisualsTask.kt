package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.task.CharacterTask
import world.gregs.voidps.engine.client.update.task.TaskIterator
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals

class CharacterVisualsTask<C : Character>(
    iterator: TaskIterator<C>,
    override val characters: CharacterList<C>,
    private val encoders: Array<VisualEncoder<Visual>>,
    addMasks: IntArray,
    private val extended: Boolean
) : CharacterTask<C>(iterator) {

    internal val addEncoders = addMasks.map { mask -> encoders.first { it.mask == mask } }

    override fun run(character: C) {
        val visuals = character.visuals
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
        val writer = BufferWriter(128)
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

    /**
     * Encodes [addEncoders] visuals into one reusable [Visuals.addition]
     */
    fun encodeAddition(visuals: Visuals) {
        val writer = BufferWriter()
        val addFlag = (if (extended) addEncoders else addEncoders.filter { visuals.flagged(it.mask) }).sumOf { it.mask }
        writeFlag(writer, addFlag)
        addEncoders.forEach { encoder ->
            val visual = visuals.aspects[encoder.mask] ?: return@forEach
            encoder.encode(writer, visual)
        }
        visuals.addition = writer.toArray()
    }

    fun writeFlag(writer: Writer, dataFlag: Int) {
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or if (extended) 0x40 else 0x10
        }
        if (extended && flag >= 0x10000) {
            flag = flag or 0x4000
        }
        writer.writeByte(flag)

        if (flag >= 0x100) {
            writer.writeByte(flag shr 8)
        }
        if (extended && flag >= 0x10000) {
            writer.writeByte(flag shr 16)
        }
    }

}