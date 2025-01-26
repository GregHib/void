package content.skill.prayer.list

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.inject
import content.entity.death.playerDeath
import content.skill.prayer.PrayerConfigs.QUICK_CURSES
import content.skill.prayer.PrayerConfigs.QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.TEMP_QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import content.entity.player.modal.Tab
import content.entity.sound.playSound
import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.isCurses

/**
 * Prayers
 * Handles the activation of prayers and selection of quick prayers
 */
val enums: EnumDefinitions by inject()
val definitions: PrayerDefinitions by inject()

interfaceOption(component = "regular_prayers", id = "prayer_list") {
    val prayers = player.getActivePrayerVarKey()
    player.togglePrayer(itemSlot, prayers, false)
}

interfaceOption(component = "quick_prayers", id = "prayer_list") {
    player.togglePrayer(itemSlot, player.getQuickVarKey(), true)
}

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
            for (key in definitions.getGroup(group) ?: continue) {
                removeVarbit(listKey, key, refresh = false)
            }
        }
        addVarbit(listKey, name, refresh = false)
        sendVariable(listKey)
    }
}

/**
 * Quick prayers
 * Until the new quick prayer selection is confirmed old
 * quick prayers are stored in [TEMP_QUICK_PRAYERS]
 */
interfaceOption("Select Quick Prayers", "orb", "prayer_orb") {
    val selecting = player.toggle(SELECTING_QUICK_PRAYERS)
    if (selecting) {
        player["tab"] = Tab.PrayerList.name
        player.sendVariable(player.getQuickVarKey())
        player[TEMP_QUICK_PRAYERS] = player[player.getQuickVarKey(), 0]
    } else if (player.contains(TEMP_QUICK_PRAYERS)) {
        player.saveQuickPrayers()
    }
    if (selecting) {
        player.interfaceOptions.unlockAll("prayer_list", "quick_prayers", 0..29)
    } else {
        player.interfaceOptions.unlockAll("prayer_list", "regular_prayers", 0..29)
    }
}

interfaceOption("Turn Quick Prayers On", "orb", "prayer_orb") {
    if (player.levels.get(Skill.Prayer) == 0) {
        player.message("You've run out of prayer points.")
        player[USING_QUICK_PRAYERS] = false
        return@interfaceOption
    }
    val active = player.toggle(USING_QUICK_PRAYERS)
    val activePrayers = player.getActivePrayerVarKey()
    if (active) {
        val quickPrayers: List<Any> = player[TEMP_QUICK_PRAYERS] ?: player[player.getQuickVarKey(), emptyList()]
        if (quickPrayers.isNotEmpty()) {
            player[activePrayers] = quickPrayers
        } else {
            player.message("You haven't selected any quick-prayers.")
            player[USING_QUICK_PRAYERS] = false
            return@interfaceOption
        }
    } else {
        player.playSound("deactivate_prayer")
        player.clear(activePrayers)
    }
}

interfaceOption("Confirm Selection", "confirm", "prayer_list") {
    player.saveQuickPrayers()
}

playerDespawn { player ->
    if (player.contains(TEMP_QUICK_PRAYERS)) {
        player.cancelQuickPrayers()
    }
}

playerDeath { player ->
    player[USING_QUICK_PRAYERS] = false
}

fun Player.saveQuickPrayers() {
    set(SELECTING_QUICK_PRAYERS, false)
    clear(TEMP_QUICK_PRAYERS)
}

fun Player.cancelQuickPrayers() {
    set(getQuickVarKey(), get(TEMP_QUICK_PRAYERS, 0))
    clear(TEMP_QUICK_PRAYERS)
}

fun Player.getQuickVarKey(): String = if (isCurses()) QUICK_CURSES else QUICK_PRAYERS