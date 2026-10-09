package content.skill.prayer.bone

import content.skill.prayer.PrayerConfigs.PRAYERS
import content.skill.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.isCurses
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class PrayerAltars : Script {

    init {
        objectOperate("Pray", "prayer_altar_zaros,prayer_altar_zaros_senntisten") {
            prayAtZaros()
        }

        objectOperate("Pray-at", "prayer_altar_zaros,prayer_altar_zaros_senntisten") {
            prayAtZaros()
        }

        objectOperate("Convert", "prayer_altar_zaros,prayer_altar_zaros_senntisten") {
            prayAtZaros()
        }

        objectOperate("Pray", "prayer_altar_*") {
            pray()
        }

        objectOperate("Pray-at", "prayer_altar_*") {
            pray()
        }

        objectOperate("Check", "prayer_altar_chaos_varrock") {
            message("An altar to the evil god Zamorak.")
        }
    }

    fun Player.pray() {
        if (levels.getOffset(Skill.Prayer) >= 0) {
            message("You already have full Prayer points.")
        } else {
            levels.set(Skill.Prayer, levels.getMax(Skill.Prayer))
            anim("altar_pray")
            message("You recharge your Prayer points.")
            set("prayer_point_power_task", true)
        }
    }

    private fun Player.prayAtZaros() {
        if (levels.getOffset(Skill.Prayer) < 0) {
            levels.set(Skill.Prayer, levels.getMax(Skill.Prayer))
            set("prayer_point_power_task", true)
            message("You recharge your Prayer points.")
        }
        switchPrayerBook()
    }

    private fun Player.switchPrayerBook() {
        clear(getActivePrayerVarKey())
        this[USING_QUICK_PRAYERS] = false
        val curses = !isCurses()
        set(PRAYERS, if (curses) "curses" else "normal")
        anim("altar_pray")
        message(if (curses) "You switch to the Ancient Curses." else "You switch to the normal Prayer book.")
    }
}
