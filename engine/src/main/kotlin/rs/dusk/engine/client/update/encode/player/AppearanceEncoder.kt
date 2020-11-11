package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.character.update.VisualEncoder
import rs.dusk.engine.entity.character.update.visual.player.APPEARANCE_MASK
import rs.dusk.engine.entity.character.update.visual.player.Appearance

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class AppearanceEncoder : VisualEncoder<Appearance>(APPEARANCE_MASK) {

    override fun encode(writer: Writer, visual: Appearance) {
        val (male,
            skillLevel,
            size,
            trimTitle,
            title,
            prefix,
            skull,
            headIcon,
            hidden,
            transform,
            body,
            colours,
            emote,
            displayName,
            combatLevel,
            summoningCombatLevel,
            idleSound,
            crawlSound,
            walkSound,
            runSound,
            soundDistance) = visual
        writer.apply {
            val start = position()
            writeByte(0)// Save space for size later
            var flag = 0
//            flag = flag or 0x1// Gender
//            flag = flag or 0x2// Display name
            if (skillLevel != -1) {
                flag = flag or 0x4// Display skill level rather than combat
            }
//            flag = flag or (size shl 3 and 0x7)
//            flag = flag and ((1 and 0xf2) shr 6)// Title enum id
            writeByte(flag)
            writeByte(title)
            writeByte(skull)
            writeByte(headIcon)
            writeByte(hidden)

            if (transform != -1) {
                writeShort(-1)
                writeShort(transform)
                writeByte(0)
            } else {
                for(index in 0 until 13) {
                    val part = body.get(index)
                    if(part == 0) {
                        writeByte(part)
                    } else {
                        writeShort(part)
                    }
                }
                writeShort(0)
            }
            colours.forEach { colour ->
                writeByte(colour)
            }
            writeShort(emote)
            writeString(displayName)
            writeByte(combatLevel)
            if (skillLevel != -1) {
                writeShort(-1)// Skill level
            } else {
                writeByte(summoningCombatLevel)// Combat level + summoning
                writeByte(-1)// Skill level
            }

            writeByte(transform != -1)
            if (transform != -1) {
                writeShort(idleSound)
                writeShort(crawlSound)
                writeShort(walkSound)
                writeShort(runSound)
                writeByte(soundDistance)
            }
            val end = position()
            position(start)
            writeByte(end - start - 1, Modifier.SUBTRACT)
            position(end)
        }
    }
}