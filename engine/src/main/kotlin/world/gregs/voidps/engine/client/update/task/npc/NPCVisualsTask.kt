package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class NPCVisualsTask(
    override val entities: PooledMapList<NPC>,
    private val encoders: Array<VisualEncoder<Visual>>,
    addMasks: IntArray // Order of these is important
) : EntityTask<NPC>() {

    private val addEncoders = addMasks.map { mask -> encoders.first { it.mask == mask } }

    /**
     * Encodes [Visual] changes into an insertion and delta update
     */
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(npc: NPC) {
        val visuals = npc.visuals
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
            encoder.encode(writer, visual)
        }
        visuals.update = writer.toArray()
    }

    /**
     * Encodes [addEncoders] visuals into one reusable [Visuals.addition]
     */
    fun encodeAddition(visuals: Visuals) {
        val writer = BufferWriter()
        val addFlag = addEncoders.filter { visuals.flagged(it.mask) }.sumBy { it.mask }
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
            flag = flag or 0x10
        }

        writer.writeByte(flag)

        if (flag >= 0x100) {
            writer.writeByte(flag shr 8)
        }
    }

}