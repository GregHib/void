package world.gregs.voidps.world.interact.entity.player.combat.prayer

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.death.Death
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.QUICK_CURSES
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.QUICK_PRAYERS
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.TEMP_QUICK_PRAYERS
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS
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
    setOf("steel_skin", "piety", "thick_skin", "chivalry", "rock_skin", "augury", "rigour"),
    setOf("burst_of_strength", "piety", "chivalry", "ultimate_strength", "superhuman_strength"),
    setOf("improved_reflexes", "incredible_reflexes", "piety", "clarity_of_thought", "chivalry"),
    setOf("rigour", "sharp_eye", "hawk_eye", "eagle_eye"),
    setOf("mystic_will", "mystic_might", "mystic_lore", "augury"),
    setOf("rapid_renewal", "rapid_heal"),
    setOf("smite", "protect_from_missiles", "protect_from_melee", "redemption", "protect_from_magic", "retribution"),
    setOf("redemption", "retribution", "smite", "protect_from_summoning")
)

val cursesGroups = setOf(
    setOf("wrath", "soul_split"),
    setOf("soul_split", "deflect_summoning", "wrath"),
    setOf("leech_strength", "turmoil"),
    setOf("leech_attack", "turmoil", "sap_warrior"),
    setOf("soul_split", "deflect_missiles", "wrath", "deflect_melee", "deflect_magic"),
    setOf("turmoil", "sap_mage", "leech_magic"),
    setOf("turmoil", "sap_ranger", "leech_ranged"),
    setOf("turmoil", "leech_defence"),
    setOf("sap_spirit", "leech_special_attack", "turmoil")
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
    val description = enums.getStruct(enum, index, "prayer_description", "")
    val name = getPrayerName(description)?.toSnakeCase() ?: return logger.warn { "Unable to find prayer button $index $listKey $description" }
    val activated = containsVarbit(listKey, name)
    if (activated) {
        removeVarbit(listKey, name)
    } else {
        if (!quick && !has(Skill.Prayer, 1)) {
            message("You need to recharge your Prayer at an altar.")
            return
        }
        val requiredLevel = enums.getStruct(enum, index, "prayer_required_level", 0)
        if (!hasMax(Skill.Prayer, requiredLevel)) {
            val message = enums.getStruct(enum, index, "prayer_requirement_text", "You need a prayer level of $requiredLevel to use $name.")
            message(message)
            return
        }
        for (group in if (curses) cursesGroups else prayerGroups) {
            if (group.contains(name)) {
                group.forEach {
                    removeVarbit(listKey, it, refresh = false)
                }
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
on<InterfaceOption>({ id == "prayer_orb" && component == "orb" && option == "Select Quick Prayers" }) { player: Player ->
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

on<InterfaceOption>({ id == "prayer_orb" && component == "orb" && option == "Turn Quick Prayers On" }) { player: Player ->
    if (player.levels.get(Skill.Prayer) == 0) {
        player.message("You've run out of prayer points.")
        player[USING_QUICK_PRAYERS] = false
        return@on
    }
    val active = player.toggle(USING_QUICK_PRAYERS)
    val activePrayers = player.getActivePrayerVarKey()
    if (active) {
        val quickPrayers: List<Any> = player.get(TEMP_QUICK_PRAYERS) ?: player[player.getQuickVarKey(), emptyList()]
        if (quickPrayers.isNotEmpty()) {
            player[activePrayers] = quickPrayers
        } else {
            player.message("You haven't selected any quick-prayers.")
            player[USING_QUICK_PRAYERS] = false
            return@on
        }
    } else {
        player.playSound("deactivate_prayer")
        player.clear(activePrayers)
    }
}

on<InterfaceOption>({ id == "prayer_list" && component == "confirm" && option == "Confirm Selection" }) { player: Player ->
    player.saveQuickPrayers()
}

on<Unregistered>({ it.contains(TEMP_QUICK_PRAYERS) }) { player: Player ->
    player.cancelQuickPrayers()
}

on<Death> { player: Player ->
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

fun getPrayerName(description: String): String? {
    return nameRegex.find(description)?.groupValues?.lastOrNull()
}

fun Player.getQuickVarKey(): String = if (isCurses()) QUICK_CURSES else QUICK_PRAYERS