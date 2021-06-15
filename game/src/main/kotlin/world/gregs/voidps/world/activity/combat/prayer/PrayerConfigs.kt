package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
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

fun Player.getActivePrayerVarKey(): String = if (isCurses()) PrayerConfigs.ACTIVE_CURSES else PrayerConfigs.ACTIVE_PRAYERS

fun Player.isCurses(): Boolean = getVar(PrayerConfigs.PRAYERS, "") == "curses"

fun Character.getPrayerBonus(skill: Skill): Double {
    // TODO remove
    return if (this is Player) {
        1.0 + (getVar("${skill.name.toLowerCase()}_bonus", 30) - 30) / 100.0
    } else {
        get("${skill.name.toLowerCase()}_bonus", 1.0)
    }
}

fun Character.updateBonus(skill: Skill) {
    if (this is Player) {
        val name = skill.name.toLowerCase()
        setVar("${name}_bonus", 30 + getLeech(skill) - getDrain(skill))
    }
}

fun Character.getLeech(skill: Skill): Int {
    return get("leech_${skill.name.toLowerCase()}", 0)
}

fun Character.getDrain(skill: Skill): Int {
    return get("drain_${skill.name.toLowerCase()}", 0)
}

fun Character.setLeech(skill: Skill, value: Int) {
    set("leech_${skill.name.toLowerCase()}", value)
}

fun Character.setDrain(skill: Skill, value: Int) {
    set("drain_${skill.name.toLowerCase()}", value)
}