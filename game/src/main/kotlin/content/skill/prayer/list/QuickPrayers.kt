package content.skill.prayer.list

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.skill.prayer.PrayerConfigs.QUICK_CURSES
import content.skill.prayer.PrayerConfigs.QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.TEMP_QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.isCurses
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.character.sound

class QuickPrayers(val enums: EnumDefinitions, val definitions: PrayerDefinitions) : Script {

    init {
        interfaceOption(id = "prayer_list:regular_prayers") { (_, itemSlot) ->
            val prayers = getActivePrayerVarKey()
            togglePrayer(itemSlot, prayers, false)
        }

        interfaceOption(id = "prayer_list:quick_prayers") { (_, itemSlot) ->
            togglePrayer(itemSlot, getQuickVarKey(), true)
        }

        interfaceOption("Select Quick Prayers", "prayer_orb:orb") {
            val selecting = toggle(SELECTING_QUICK_PRAYERS)
            if (selecting) {
                tab(Tab.PrayerList)
                sendVariable(getQuickVarKey())
                set(TEMP_QUICK_PRAYERS, get(getQuickVarKey(), 0))
            } else if (contains(TEMP_QUICK_PRAYERS)) {
                saveQuickPrayers()
            }
            if (selecting) {
                interfaceOptions.unlockAll("prayer_list", "quick_prayers", 0..29)
            } else {
                interfaceOptions.unlockAll("prayer_list", "regular_prayers", 0..29)
            }
        }

        interfaceOption("Turn Quick Prayers On", "prayer_orb:orb") {
            if (levels.get(Skill.Prayer) == 0) {
                message("You've run out of prayer points.")
                set(USING_QUICK_PRAYERS, false)
                return@interfaceOption
            }
            val active = toggle(USING_QUICK_PRAYERS)
            val activePrayers = getActivePrayerVarKey()
            if (active) {
                val quickPrayers: List<Any> = get(TEMP_QUICK_PRAYERS) ?: get(getQuickVarKey(), emptyList())
                if (quickPrayers.isNotEmpty()) {
                    set(activePrayers, quickPrayers)
                } else {
                    message("You haven't selected any quick-prayers.")
                    set(USING_QUICK_PRAYERS, false)
                    return@interfaceOption
                }
            } else {
                sound("deactivate_prayer")
                clear(activePrayers)
            }
        }

        interfaceOption("Confirm Selection", "prayer_list:confirm") {
            saveQuickPrayers()
        }

        playerDespawn {
            if (contains(TEMP_QUICK_PRAYERS)) {
                cancelQuickPrayers()
            }
        }

        playerDeath {
            set(USING_QUICK_PRAYERS, false)
        }
    }

    /**
     * Prayers
     * Handles the activation of prayers and selection of quick prayers
     */

    fun Player.togglePrayer(index: Int, listKey: String, quick: Boolean) {
        val curses = isCurses()
        val definition = if (curses) definitions.getCurse(index) else definitions.getPrayer(index)
        val name = definition.stringId
        val activated = containsVarbit(listKey, name)
        if (activated) {
            removeVarbit(listKey, name)
        } else {
            if (!quick && !has(Skill.Prayer, 1)) {
                message("You need to recharge your Prayer at an altar.")
                return
            }
            val requiredLevel = definition.level
            if (!hasMax(Skill.Prayer, requiredLevel)) {
                val enum = if (curses) "curses" else "prayers"
                val message = enums.getStruct(enum, index, "prayer_requirement_text", "You need a prayer level of $requiredLevel to use $name.")
                message(message)
                return
            }
            for (group in definition.groups) {
                removeGroup(listKey, group)
            }
            for (type in definition.bonuses.keys + definition.drains.keys) {
                when (type) {
                    "magic", "ranged_attack", "ranged_strength" -> if (curses) {
                        removeGroup(listKey, 11) // Attack
                        removeGroup(listKey, 10) // Strength
                    } else {
                        removeGroup(listKey, 1) // Strength
                        removeGroup(listKey, 2) // Attack
                    }
                    "attack", "strength" -> if (curses) {
                        removeGroup(listKey, 14) // Ranged
                        removeGroup(listKey, 13) // Magic
                    } else {
                        removeGroup(listKey, 3) // Ranged
                        removeGroup(listKey, 4) // Magic
                    }
                    "defence" -> if (curses) {
                        removeGroup(listKey, 15)
                    } else {
                        removeGroup(listKey, 0)
                    }
                }
            }
            addVarbit(listKey, name, refresh = false)
            sendVariable(listKey)
        }
    }

    private fun Player.removeGroup(listKey: String, group: Int) {
        for (key in definitions.getGroup(group) ?: return) {
            removeVarbit(listKey, key, refresh = false)
        }
    }

    /**
     * Quick prayers
     * Until the new quick prayer selection is confirmed old
     * quick prayers are stored in [TEMP_QUICK_PRAYERS]
     */

    fun Player.saveQuickPrayers() {
        set(SELECTING_QUICK_PRAYERS, false)
        clear(TEMP_QUICK_PRAYERS)
    }

    fun Player.cancelQuickPrayers() {
        set(getQuickVarKey(), get(TEMP_QUICK_PRAYERS, 0))
        clear(TEMP_QUICK_PRAYERS)
    }

    fun Player.getQuickVarKey(): String = if (isCurses()) QUICK_CURSES else QUICK_PRAYERS
}
