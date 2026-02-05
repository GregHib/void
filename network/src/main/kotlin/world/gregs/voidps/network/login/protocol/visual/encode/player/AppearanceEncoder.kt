package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.APPEARANCE_MASK
import world.gregs.voidps.network.login.protocol.visual.update.player.Appearance

class AppearanceEncoder : VisualEncoder<PlayerVisuals>(APPEARANCE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (
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
            emote,
            displayName,
            combatLevel,
            summoningCombatLevel,
            idleSound,
            crawlSound,
            walkSound,
            runSound,
            soundDistance,
        ) = visuals.appearance
        writer.apply {
            val length = size(visuals.appearance)
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
            for (i in 4 downTo 0) {
                writeByte(body.getColour(i))
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
            if (!body.male) {
                flag = flag or 0x1
            }
//            flag = flag or 0x2// Display name
            if (showSkillLevel) {
                flag = flag or 0x4 // Display skill level rather than combat
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

    companion object {
        fun size(appearance: Appearance): Int = 17 + appearance.displayName.length + if (appearance.transform != -1) 14 else (0 until 12).sumBy { if (appearance.body.get(it) == 0) 1 else 2 }
    }
}
