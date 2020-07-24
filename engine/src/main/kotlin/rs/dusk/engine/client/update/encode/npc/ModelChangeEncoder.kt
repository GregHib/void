package rs.dusk.engine.client.update.encode.npc

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.model.entity.character.update.VisualEncoder
import rs.dusk.engine.model.entity.character.update.visual.npc.MODEL_CHANGE_MASK
import rs.dusk.engine.model.entity.character.update.visual.npc.ModelChange

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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