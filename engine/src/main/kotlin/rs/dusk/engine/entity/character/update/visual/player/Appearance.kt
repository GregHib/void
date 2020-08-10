package rs.dusk.engine.entity.character.update.visual.player

import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.Visual

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
    var equipment: Container,
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

fun Player.getAppearance() =
    visuals.getOrPut(APPEARANCE_MASK) { Appearance(equipment = equipment) }

private fun Player.flag(action: Appearance.() -> Unit) {
    val appearance = getAppearance()
    action(appearance)
    flagAppearance()
}

var Player.male: Boolean
    get() = getAppearance().male
    set(value) = flag {
        male = value
    }

fun Player.setSkillLevel(level: Int = -1) = flag {
    skillLevel = level
}

var Player.size: Int
    get() = getAppearance().size
    set(value) = flag {
        size = value
    }

fun Player.setTrimTitle(trim: Boolean = false) = flag {
    this.trimTitle = trim
}

var Player.title: Int
    get() = getAppearance().title
    set(value) = flag {
        title = value
    }

var Player.prefix: String
    get() = getAppearance().prefix
    set(value) = flag {
        prefix = value
    }

var Player.skull: Int
    get() = getAppearance().skull
    set(value) = flag {
        skull = value
    }

fun Player.skull() {
    skull = 1
}

fun Player.unskull() {
    skull = -1
}

var Player.headIcon: Int
    get() = getAppearance().headIcon
    set(value) = flag {
        headIcon = value
    }

var Player.hidden: Boolean
    get() = getAppearance().hidden
    set(value) = flag {
        hidden = value
    }

fun Player.hide() {
    hidden = true
}

fun Player.show() {
    hidden = false
}

var Player.transform: Int
    get() = getAppearance().transform
    set(value) = flag {
        transform = value
    }

fun Player.setTransformSounds(
    idleSound: Int = -1,
    crawlSound: Int = -1,
    walkSound: Int = -1,
    runSound: Int = -1,
    soundDistance: Int = 0
) = flag {
    this.idleSound = idleSound
    this.crawlSound = crawlSound
    this.walkSound = walkSound
    this.runSound = runSound
    this.soundDistance = soundDistance
}

fun Player.setLook(index: Int, look: Int) = flag {
    this.look[index] = look
}

val Player.looks: IntArray
    get() = getAppearance().look

fun Player.setColour(index: Int, colour: Int) = flag {
    this.colours[index] = colour
}

val Player.colours: IntArray
    get() = getAppearance().colours

var Player.emote: Int
    get() = getAppearance().emote
    set(value) = flag {
        emote = value
    }

var Player.name: String
    get() = getAppearance().displayName
    set(value) = flag {
        displayName = value
    }

var Player.combatLevel: Int
    get() = getAppearance().combatLevel
    set(value) = flag {
        combatLevel = value
    }

var Player.summoningCombatLevel: Int
    get() = getAppearance().summoningCombatLevel
    set(value) = flag {
        summoningCombatLevel = value
    }