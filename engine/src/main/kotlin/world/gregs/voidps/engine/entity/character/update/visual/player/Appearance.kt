package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.BodyParts
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.item.BodyPart
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class Appearance(
    var male: Boolean = true,
    var skillLevel: Int = -1,
    var size: Int = 1,
    var trimTitle: Boolean = false,
    var title: Int = -1,
    var prefix: String = "",
    var skull: Int = -1,
    var headIcon: Int = -1,
    var hidden: Boolean = false,
    var transform: Int = -1,
    val body: BodyParts,
    val colours: IntArray = IntArray(5),
    var emote: Int = 1426,
    var displayName: String = "",
    var combatLevel: Int = 3,
    var summoningCombatLevel: Int = 0,
    var idleSound: Int = -1,
    var crawlSound: Int = -1,
    var walkSound: Int = -1,
    var runSound: Int = -1,
    var soundDistance: Int = 0
) : Visual {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Appearance

        if (male != other.male) return false
        if (skillLevel != other.skillLevel) return false
        if (size != other.size) return false
        if (trimTitle != other.trimTitle) return false
        if (title != other.title) return false
        if (prefix != other.prefix) return false
        if (skull != other.skull) return false
        if (headIcon != other.headIcon) return false
        if (hidden != other.hidden) return false
        if (transform != other.transform) return false
        if (body != other.body) return false
        if (!colours.contentEquals(other.colours)) return false
        if (emote != other.emote) return false
        if (displayName != other.displayName) return false
        if (combatLevel != other.combatLevel) return false
        if (summoningCombatLevel != other.summoningCombatLevel) return false
        if (idleSound != other.idleSound) return false
        if (crawlSound != other.crawlSound) return false
        if (walkSound != other.walkSound) return false
        if (runSound != other.runSound) return false
        if (soundDistance != other.soundDistance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = male.hashCode()
        result = 31 * result + skillLevel
        result = 31 * result + size
        result = 31 * result + trimTitle.hashCode()
        result = 31 * result + title
        result = 31 * result + prefix.hashCode()
        result = 31 * result + skull
        result = 31 * result + headIcon
        result = 31 * result + hidden.hashCode()
        result = 31 * result + transform
        result = 31 * result + body.hashCode()
        result = 31 * result + colours.contentHashCode()
        result = 31 * result + emote
        result = 31 * result + displayName.hashCode()
        result = 31 * result + combatLevel
        result = 31 * result + summoningCombatLevel
        result = 31 * result + idleSound
        result = 31 * result + crawlSound
        result = 31 * result + walkSound
        result = 31 * result + runSound
        result = 31 * result + soundDistance
        return result
    }
}

const val APPEARANCE_MASK = 0x10

fun Player.flagAppearance() = visuals.flag(APPEARANCE_MASK)

val Player.appearance: Appearance
    get() = visuals.getOrPut(APPEARANCE_MASK) {
        val bodyParts = BodyParts(equipment, get(), intArrayOf(3, 14, 18, 26, 34, 38, 42))
        BodyPart.all.forEach {
            bodyParts.updateConnected(it)
        }
        updateAppearanceOnEquipmentChanges(bodyParts)
        Appearance(body = bodyParts)
    }

private fun Player.updateAppearanceOnEquipmentChanges(parts: BodyParts) {
    equipment.listeners.add { list ->
        var changed = false
        for ((index, _, _, _, _) in list) {
            val slot = EquipSlot.by(index)
            val part = BodyPart.by(slot) ?: continue
            if (parts.updateConnected(part)) {
                changed = true
            }
        }
        if (changed) {
            flagAppearance()
        }
    }
}

private fun Player.flag(action: Appearance.() -> Unit) {
    action(appearance)
    flagAppearance()
}

var Player.male: Boolean
    get() = appearance.male
    set(value) = flag {
        male = value
    }

fun Player.setSkillLevel(level: Int = -1) = flag {
    skillLevel = level
}

fun Player.setTrimTitle(trim: Boolean = false) = flag {
    this.trimTitle = trim
}

var Player.title: Int
    get() = appearance.title
    set(value) = flag {
        title = value
    }

var Player.prefix: String
    get() = appearance.prefix
    set(value) = flag {
        prefix = value
    }

var Player.headIcon: Int
    get() = appearance.headIcon
    set(value) = flag {
        headIcon = value
    }

fun Player.setLook(index: Int, look: Int) = flag {
    val part = BodyPart.by(index) ?: return@flag
    body.looks[index] = look
    body.updateConnected(part)
}

val Player.looks: IntArray
    get() = appearance.body.looks

fun Player.setColour(index: Int, colour: Int) = flag {
    this.colours[index] = colour
}

val Player.colours: IntArray
    get() = appearance.colours

var Player.emote: Int
    get() = appearance.emote
    set(value) = flag {
        emote = value
    }

var Player.name: String
    get() = appearance.displayName
    set(value) = flag {
        displayName = value
    }

var Player.combatLevel: Int
    get() = appearance.combatLevel
    set(value) = flag {
        combatLevel = value
    }

var Player.summoningCombatLevel: Int
    get() = appearance.summoningCombatLevel
    set(value) = flag {
        summoningCombatLevel = value
    }