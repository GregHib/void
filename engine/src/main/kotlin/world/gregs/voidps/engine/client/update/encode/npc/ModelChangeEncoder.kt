package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.npc.MODEL_CHANGE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.ModelChange

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class ModelChangeEncoder : VisualEncoder<ModelChange>(MODEL_CHANGE_MASK) {

    override fun encode(writer: Writer, visual: ModelChange) {
        val (models, colours, textures) = visual
        var hash = 0
        //Reset
        if (models == null && colours == null && textures == null) {
            hash = 1
        } else {
            if (models != null) {
                hash = hash or 0x2
            }

            if (colours != null) {
                hash = hash or 0x4
            }

            if (textures != null) {
                hash = hash or 0x8
            }
        }
        writer.apply {
            writeByte(hash, Modifier.SUBTRACT)
            models?.forEach {
                writeShort(it)
            }
            colours?.forEach {
                writeShort(it)
            }
            textures?.forEach {
                writeShort(it, Modifier.ADD, Endian.LITTLE)
            }
        }
    }

}