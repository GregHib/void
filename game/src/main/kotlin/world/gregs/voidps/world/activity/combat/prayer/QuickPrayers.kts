package world.gregs.voidps.world.activity.combat.prayer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.event.Death
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Level.hasMax
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.EnumDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.QUICK_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.TEMP_QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import world.gregs.voidps.world.interact.entity.player.display.Tab
import world.gregs.voidps.world.interact.entity.sound.playSound

/**
 * Prayers
 * Handles the activation of prayers and selection of quick prayers
 */
val enums: EnumDefinitions by inject()
val nameRegex = "<br>(.*?)<br>".toRegex()

val logger = InlineLogger()

val prayerGroups = setOf(
    setOf("Steel Skin", "Piety", "Thick Skin", "Chivalry", "Rock Skin", "Augury", "Rigour"),
    setOf("Burst of Strength", "Piety", "Chivalry", "Ultimate Strength", "Superhuman Strength"),
    setOf("Improved Reflexes", "Incredible Reflexes", "Piety", "Clarity of Thought", "Chivalry"),
    setOf("Rigour", "Sharp Eye", "Hawk Eye", "Eagle Eye"),
    setOf("Mystic Will", "Mystic Might", "Mystic Lore", "Augury"),
    setOf("Rapid Renewal", "Rapid Heal"),
    setOf("Smite", "Protect from Missiles", "Protect from Melee", "Redemption", "Protect from Magic", "Retribution"),
    setOf("Redemption", "Retribution", "Smite", "Protect from Summoning")
)

val cursesGroups = setOf(
    setOf("Wrath", "Soul Split"),
    setOf("Soul Split", "Deflect Summoning", "Wrath"),
    setOf("Leech Strength", "Turmoil"),
    setOf("Leech Attack", "Turmoil", "Sap Warrior"),
    setOf("Soul Split", "Deflect Missiles", "Wrath", "Deflect Melee", "Deflect Magic"),
    setOf("Turmoil", "Sap Mage", "Leech Magic"),
    setOf("Turmoil", "Sap Ranger", "Leech Ranged"),
    setOf("Turmoil", "Leech Defence"),
    setOf("Sap Spirit", "Leech Special Attack", "Turmoil")
)

on<InterfaceOption>({ id == "prayer_list" && component == "regular_prayers" }) { player: Player ->
    val prayers = player.getActivePrayerVarKey()
    player.togglePrayer(itemSlot, prayers, false)
}

on<InterfaceOption>({ id == "prayer_list" && component == "quick_prayers" }) { player: Player ->
    player.togglePrayer(itemSlot, player.getQuickVarKey(), true)
}

fun Player.togglePrayer(index: Int, listKey: String, quick: Boolean) {
    val curses = isCurses()
    val enum = if (curses) "curses" else "prayers"
    val description = enums.getStruct(enum, index, "description", "")
    val name = getPrayerName(description) ?: return logger.warn { "Unable to find prayer button $index $listKey $description" }
    val activated = hasVar(listKey, name)
    if (activated) {
        removeVar(listKey, name)
    } else {
        if (!quick && !has(Skill.Prayer, 1)) {
            message("You need to recharge your Prayer at an altar.")
            return
        }
        val requiredLevel = enums.getStruct(enum, index, "required_level", 0)
        if (!hasMax(Skill.Prayer, requiredLevel)) {
            val message = enums.getStruct(enum, index, "message", "You need a prayer level of $requiredLevel to use $name.")
            message(message)
            return
        }
        for (group in if (curses) cursesGroups else prayerGroups) {
            if (group.contains(name)) {
                group.forEach {
                    removeVar(listKey, it, refresh = false)
                }
            }
        }
        addVar(listKey, name)
    }
}

/**
 * Quick prayers
 * Until the new quick prayer selection is confirmed old
 * quick prayers are stored in [TEMP_QUICK_PRAYERS]
 */
on<InterfaceOption>({ id == "prayer_orb" && component == "orb" && option == "Select Quick Prayers" }) { player: Player ->
    val selecting = player.toggleVar(SELECTING_QUICK_PRAYERS)
    if (selecting) {
        player.setVar("tab", Tab.PrayerList.name)
        player.sendVar(player.getQuickVarKey())
        player[TEMP_QUICK_PRAYERS] = player.getVar(player.getQuickVarKey(), 0)
    } else if (player.contains(TEMP_QUICK_PRAYERS)) {
        player.saveQuickPrayers()
    }
    if (selecting) {
        player.interfaceOptions.unlockAll("prayer_list", "quick_prayers", 0..29)
    } else {
        player.interfaceOptions.unlockAll("prayer_list", "regular_prayers", 0..29)
    }
}

on<InterfaceOption>({ id == "prayer_orb" && component == "orb" && option == "Turn Quick Prayers On" }) { player: Player ->
    if (player.levels.get(Skill.Prayer) == 0) {
        player.message("You've run out of prayer points.")
        player.setVar(USING_QUICK_PRAYERS, false)
        return@on
    }
    val active = player.toggleVar(USING_QUICK_PRAYERS)
    val activePrayers = player.getActivePrayerVarKey()
    if (active) {
        val quickPrayers: Int = player.getOrNull(TEMP_QUICK_PRAYERS) ?: player.getVar(player.getQuickVarKey(), 0)
        if (quickPrayers > 0) {
            player.setVar(activePrayers, quickPrayers)
        } else {
            player.message("You haven't selected any quick-prayers.")
            player.setVar(USING_QUICK_PRAYERS, false)
            return@on
        }
    } else {
        player.playSound("deactivate_prayer")
        player.clearVar(activePrayers)
    }
}

on<InterfaceOption>({ id == "prayer_list" && component == "confirm" && option == "Confirm Selection" }) { player: Player ->
    player.saveQuickPrayers()
}

on<Unregistered>({ it.contains(TEMP_QUICK_PRAYERS) }) { player: Player ->
    player.cancelQuickPrayers()
}

on<Death> { player: Player ->
    player.setVar(USING_QUICK_PRAYERS, false)
}

fun Player.saveQuickPrayers() {
    setVar(SELECTING_QUICK_PRAYERS, false)
    clear(TEMP_QUICK_PRAYERS)
}

fun Player.cancelQuickPrayers() {
    setVar(getQuickVarKey(), get(TEMP_QUICK_PRAYERS, 0))
    clear(TEMP_QUICK_PRAYERS)
}

fun getPrayerName(description: String): String? {
    return nameRegex.find(description)?.groupValues?.lastOrNull()
}

fun Player.getQuickVarKey(): String = if (isCurses()) QUICK_CURSES else QUICK_PRAYERS