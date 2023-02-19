package world.gregs.voidps.world.activity.combat.prayer

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set

object PrayerConfigs {
    const val PRAYERS = "prayers"

    const val SELECTING_QUICK_PRAYERS = "select_quick_prayers"
    const val USING_QUICK_PRAYERS = "using_quick_prayers"

    const val ACTIVE_PRAYERS = "activated_prayers"
    const val QUICK_PRAYERS = "quick_prayers"
    const val TEMP_QUICK_PRAYERS = "old_quick_prayers"

    const val ACTIVE_CURSES = "activated_curses"
    const val QUICK_CURSES = "quick_curses"
}

fun Character.prayerActive(name: String): Boolean {
    return if (this is Player) {
        hasVar(getActivePrayerVarKey(), name.toTitleCase())
    } else {
        false
    }
}

fun Player.getActivePrayerVarKey(): String = if (isCurses()) PrayerConfigs.ACTIVE_CURSES else PrayerConfigs.ACTIVE_PRAYERS

fun Player.isCurses(): Boolean = getVar(PrayerConfigs.PRAYERS, "") == "curses"

/**
 * Bonus' are a value between 11..42 to represent -25%..15% with 30=0%
 */
fun Character.updateBonus(skill: Skill) {
    if (this is Player) {
        val name = skill.name.lowercase()
        setVar("${name}_bonus", 30 + getLeech(skill) - getDrain(skill))
    }
}

/**
 * Leech represents a value between -19..12 which via [updateBonus] is interpolated between -25..15 % on the client side.
 */
fun Character.getLeech(skill: Skill): Int {
    return get("leech_${skill.name.lowercase()}", 0)
}

fun Character.getDrain(skill: Skill): Int {
    return get("drain_${skill.name.lowercase()}", 0)
}

fun Character.getBaseDrain(skill: Skill): Int {
    return get("base_${skill.name.lowercase()}_drain", 0)
}

fun Character.setLeech(skill: Skill, value: Int) {
    set("leech_${skill.name.lowercase()}", value)
}

fun Character.setDrain(skill: Skill, value: Int, base: Int) {
    set("drain_${skill.name.lowercase()}", value)
    set("base_${skill.name.lowercase()}_drain", base)
}