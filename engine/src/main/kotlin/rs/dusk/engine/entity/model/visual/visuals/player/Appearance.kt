package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
    val look: IntArray = intArrayOf(0, 10, 18, 26, 33, 36, 42),
    val colours: IntArray = IntArray(10),
    var equipment: IntArray = intArrayOf(
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1
    ),// TODO replace with container
    var emote: Int = 1426,
    var displayName: String = "",
    var combatLevel: Int = 3,
    var summoningCombatLevel: Int = 0
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
        if (!look.contentEquals(other.look)) return false
        if (!colours.contentEquals(other.colours)) return false
        if (emote != other.emote) return false
        if (displayName != other.displayName) return false
        if (combatLevel != other.combatLevel) return false
        if (summoningCombatLevel != other.summoningCombatLevel) return false

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
        result = 31 * result + look.contentHashCode()
        result = 31 * result + colours.contentHashCode()
        result = 31 * result + emote
        result = 31 * result + displayName.hashCode()
        result = 31 * result + combatLevel
        result = 31 * result + summoningCombatLevel
        return result
    }
}

const val APPEARANCE_MASK = 0x8

fun Player.flagAppearance() = visuals.flag(APPEARANCE_MASK)

fun Player.getAppearance() = visuals.getOrPut(APPEARANCE_MASK) { Appearance() }

private fun Player.flag(action: Appearance.() -> Unit) {
    val appearance = getAppearance()
    action(appearance)
    flagAppearance()
}

fun Player.setGender(male: Boolean = false) = flag {
    this.male = male
}

fun Player.setSkillLevel(level: Int = -1) = flag {
    skillLevel = level
}

fun Player.setSize(size: Int = 1) = flag {
    this.size = size
}

fun Player.setTrimTitle(trim: Boolean = false) = flag {
    this.trimTitle = trim
}

fun Player.setTitle(title: Int = -1) = flag {
    this.title = title
}

fun Player.setPrefix(prefix: String = "") = flag {
    this.prefix = prefix
}

fun Player.setSkull(skull: Int = -1) = flag {
    this.skull = skull
}

fun Player.skull() = setSkull(1)

fun Player.unskull() = setSkull(-1)

fun Player.setHeadIcon(icon: Int = -1) = flag {
    this.headIcon = icon
}

fun Player.setHidden(hidden: Boolean = false) = flag {
    this.hidden = hidden
}

fun Player.hide() = setHidden(true)

fun Player.show() = setHidden(false)

fun Player.transform(transform: Int = -1) = flag {
    this.transform = transform
}

fun Player.setLook(index: Int, look: Int) = flag {
    this.look[index] = look
}

fun Player.setColour(index: Int, colour: Int) = flag {
    this.colours[index] = colour
}

fun Player.setEquipment(equipment: IntArray) = flag {
    this.equipment = equipment
}

fun Player.setEmote(emote: Int = 1426) = flag {
    this.emote = emote
}

fun Player.setName(displayName: String) = flag {
    this.displayName = displayName
}

fun Player.setCombatLevel(level: Int = 3) = flag {
    this.combatLevel = level
}

fun Player.setSummoningCombatLevel(level: Int = 0) = flag {
    this.summoningCombatLevel = level
}