package world.gregs.voidps.network.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.APPEARANCE_MASK

class AppearanceEncoder : VisualEncoder<PlayerVisuals>(APPEARANCE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (male,
            showSkillLevel,
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
            soundDistance) = visuals.appearance
        writer.apply {
            val length = 17 + displayName.length + if (transform != -1) 14 else (0 until 12).sumBy { if (body.get(it) == 0) 1 else 2 }
            writeByte(length)
            if (transform != -1) {
                writeByte(soundDistance)
                writeShortLittle(runSound)
                writeShortLittle(walkSound)
                writeShortLittle(crawlSound)
                writeShortLittle(idleSound)
            }
            writeByte(transform != -1)

            if (showSkillLevel) {
                writeShort(skillLevel)
            } else {
                writeByte(-1)
                writeByte(summoningCombatLevel)
            }
            writeByte(combatLevel)
            writeStringLittle(displayName)
            writeShortLittle(emote)
            for (i in colours.lastIndex downTo 0) {
                writeByte(colours[i])
            }
            if (transform != -1) {
                writeByte(0)
                writeShortLittle(transform)
                writeShortLittle(-1)
            } else {
                for (index in 11 downTo 0) {
                    val part = body.get(index)
                    if (part == 0) {
                        writeByte(0)
                    } else {
                        writeShortLittle(part)
                    }
                }
            }
            writeByte(hidden)
            writeByte(headIcon)
            writeByte(skull)
            writeByte(title)
            var flag = 0
            if (!male) {
                flag = flag or 0x1
            }
//            flag = flag or 0x2// Display name
            if (showSkillLevel) {
                flag = flag or 0x4// Display skill level rather than combat
            }
//            flag = flag or (size shl 3 and 0x7)
//            flag = flag and ((1 and 0xf2) shr 6)// Title enum id
            writeByte(flag)
        }
    }

    private fun Writer.writeStringLittle(value: String?) {
        writeByte(0)
        if (value != null) {
            for (i in value.lastIndex downTo 0) {
                writeByte(value[i].code)
            }
        }
    }
}