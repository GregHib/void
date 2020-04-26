package rs.dusk.engine.entity.model.visual.visuals.player

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
    var prefix: String? = null,
    var skull: Int = -1,
    var headIcon: Int = -1,
    var hidden: Boolean = false,
    var transform: Int = -1,
    var look: IntArray = intArrayOf(0, 10, 18, 26, 33, 36, 42),
    var colours: IntArray = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    var emote: Int = 1426,
    var displayName: String,
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
        result = 31 * result + (prefix?.hashCode() ?: 0)
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