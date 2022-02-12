package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.task.TaskIterator
import world.gregs.voidps.engine.client.update.task.VisualsTask
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals

class NPCVisualsTask(
    iterator: TaskIterator<NPC>,
    characters: CharacterList<NPC>,
    encoders: Array<VisualEncoder<Visual>>,
    addMasks: IntArray
) : VisualsTask<NPC>(iterator, characters, encoders, addMasks) {

    /**
     * Encodes all flagged visuals into one reusable [Visuals.update]
     */
    override fun encodeUpdate(visuals: Visuals) {
        val writer = BufferWriter()
        writeFlag(writer, visuals.flag)
        encoders.forEach { encoder ->
            if (!visuals.flagged(encoder.mask)) {
                return@forEach
            }
            val visual = visuals.aspects[encoder.mask] ?: return@forEach
            encoder.encodeVisual(writer, visual)
        }
        visuals.update = writer.toArray()
    }

    /**
     * Encodes [addEncoders] visuals into one reusable [Visuals.addition]
     */
    override fun encodeAddition(visuals: Visuals) {
        val writer = BufferWriter()
        val addFlag = addEncoders.filter { visuals.flagged(it.mask) }.sumOf { it.mask }
        writeFlag(writer, addFlag)
        addEncoders.forEach { encoder ->
            val visual = visuals.aspects[encoder.mask] ?: return@forEach
            encoder.encodeVisual(writer, visual)
        }
        visuals.addition = writer.toArray()
    }

    fun writeFlag(writer: Writer, dataFlag: Int) {
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or 0x10
        }

        writer.writeByte(flag)

        if (flag >= 0x100) {
            writer.writeByte(flag shr 8)
        }
    }

}