package content.skill.prayer

import content.skill.prayer.PrayerConfigs.ACTIVE_CURSES
import content.skill.prayer.PrayerConfigs.ACTIVE_PRAYERS
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.headIcon
import world.gregs.voidps.engine.entity.character.sound

class Prayers :
    Script,
    PrayerApi {

    init {
        playerSpawn {
            sendVariable("attack_bonus")
            sendVariable("strength_bonus")
            sendVariable("defence_bonus")
            sendVariable("ranged_bonus")
            sendVariable("magic_bonus")
        }

        prayerStart { prayer ->
            val curses = isCurses()
            if (curses) {
                anim("activate_$prayer")
                gfx("activate_$prayer")
            } else {
                sound("activate_$prayer")
            }
            updateOverheadIcon(curses)
            softTimers.startIfAbsent("prayer_drain")
        }

        prayerStop {
            sound("deactivate_prayer")
            val curses = isCurses()
            stopPrayerDrain(this, curses)
            updateOverheadIcon(curses)
        }
    }

    fun stopPrayerDrain(player: Player, curses: Boolean) {
        val key = if (curses) ACTIVE_CURSES else ACTIVE_PRAYERS
        val activePrayers: List<String>? = player[key]
        if (activePrayers.isNullOrEmpty()) {
            player.clear(key)
            if (player.softTimers.contains("prayer_drain")) {
                player.softTimers.stop("prayer_drain")
            }
            return
        }
    }

    fun Player.updateOverheadIcon(curses: Boolean) {
        val changed = if (curses) {
            changedCurseIcon()
        } else {
            changedPrayerIcon()
        }
        if (changed) {
            flagAppearance()
        }
    }

    fun Player.changedCurseIcon(): Boolean {
        var value = -1
        when {
            praying("wrath") -> value = 19
            praying("soul_split") -> value = 20
            else -> {
                if (praying("deflect_summoning")) {
                    value += 4
                }

                value += when {
                    praying("deflect_magic") -> if (value > -1) 3 else 2
                    praying("deflect_missiles") -> if (value > -1) 2 else 3
                    praying("deflect_melee") -> 1
                    else -> 0
                }
                if (value > -1) {
                    value += 12
                }
            }
        }
        if (headIcon != value) {
            headIcon = value
            return true
        }
        return false
    }

    fun Player.changedPrayerIcon(): Boolean {
        var value = -1
        when {
            praying("retribution") -> value = 3
            praying("redemption") -> value = 5
            praying("smite") -> value = 4
            else -> {
                if (praying("protect_from_summoning")) {
                    value += 8
                }

                value += when {
                    praying("protect_from_magic") -> 3
                    praying("protect_from_missiles") -> 2
                    praying("protect_from_melee") -> 1
                    else -> 0
                }
            }
        }

        if (headIcon != value) {
            headIcon = value
            return true
        }
        return false
    }
}
