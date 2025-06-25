package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.APPEARANCE_MASK
import world.gregs.voidps.network.login.protocol.visual.update.player.Appearance

class AppearanceEncoder : VisualEncoder<PlayerVisuals>(APPEARANCE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (showSkillLevel,
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
            soundDistance) = visuals.appearance
        writer.apply {
            val length = size(visuals.appearance)

            p1Alt3(length)

            var flag = 0
            if (!body.male) {
                flag = flag or 0x1
            }
//            flag = flag or 0x2// Display name
            if (showSkillLevel) {
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
                writeByte(0)
                ip2(transform)
                ip2(-1)
            } else {
                for (index in 0 until 15) {
                    if (index == 12 || index == 13) continue

                    val part = body.get(index)
                    if (part == 0) {
                        writeByte(0)
                    } else {
                        writeShort(part)
                    }
                }
            }

            writeShort(0) // TODO comp cape

            for (i in 0 until 10) {
                writeByte(body.getColour(i))
            }

            writeShort(emote)
            writeString(displayName)
            writeByte(combatLevel)

            writeByte(summoningCombatLevel)
            writeByte(-1)
            /*
            if (showSkillLevel) {
                writeShort(skillLevel)
            } else {
                writeByte(-1)
                writeByte(summoningCombatLevel)
            }

             */

            writeByte(transform != -1) // TODO actually sound range?
            if (transform != -1) {
                writeByte(soundDistance)
                writeShort(runSound)
                writeShort(walkSound)
                writeShort(crawlSound)
                writeShort(idleSound)
            }
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
        fun size(appearance: Appearance): Int {
            return 17 + 3 + 2 + appearance.displayName.length + if (appearance.transform != -1) 14 else (0 until 15).sumBy { if (appearance.body.get(it) == 0) 1 else 2 }
        }
    }
}