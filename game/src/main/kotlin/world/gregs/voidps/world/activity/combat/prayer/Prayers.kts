package world.gregs.voidps.world.activity.combat.prayer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.character.update.visual.player.headIcon
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.QUICK_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.TEMP_QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import world.gregs.voidps.world.interact.entity.player.display.Tab

/**
 * Prayers
 * Handles the activation of prayers and selection of quick prayers
 */
val enums: EnumDecoder by inject()
val structs: StructDecoder by inject()

val nameRegex = "<br>(.*?)<br>".toRegex()
val prayerEnumId = 2279
val curseEnumId = 863

val logger = InlineLogger()

fun loadPrayerNames(enumId: Int): List<String> {
    val list = mutableListOf<Pair<Int, String>>()
    enums.get(enumId).map?.forEach { (_, value) ->
        val strut = structs.get(value as Int).params
        val name = getPrayerName(strut)!!
        list.add(value to name)
    }
    list.sortBy { it.first }
    return list.map { it.second }
}

val prayerNames = loadPrayerNames(prayerEnumId)
BitwiseVariable(1395, Variable.Type.VARP, values = prayerNames).register(ACTIVE_PRAYERS)
BitwiseVariable(1397, Variable.Type.VARP, true, values = prayerNames).register(QUICK_PRAYERS)

val curseNames = loadPrayerNames(curseEnumId)
BitwiseVariable(1582, Variable.Type.VARP, values = curseNames).register(ACTIVE_CURSES)
BitwiseVariable(1587, Variable.Type.VARP, true, values = curseNames).register(QUICK_CURSES)

val prayerGroups = setOf(
    setOf("Steel Skin", "Piety", "Thick Skin", "Chivalry", "Rock Skin"),
    setOf("Burst of Strength", "Piety", "Chivalry", "Ultimate Strength", "Superhuman Strength"),
    setOf("Improved Reflexes", "Incredible Reflexes", "Piety", "Clarity of Thought", "Chivalry"),
    setOf("Rigour", "Sharp Eye", "Hawk Eye", "Eagle Eye"),
    setOf("Mystic Will", "Mystic Might", "Mystic Lore", "Augury"),
    setOf("Rapid Renewal", "Rapid Heal"),
    setOf("Smite", "Protect from Missiles", "Protect Item", "Protect from Melee", "Redemption", "Protect from Magic", "Retribution"),
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

on<InterfaceOption>({ name == "prayer_list" && component == "regular_prayers" }) { player: Player ->
    val prayers = player.getActiveVarKey()
    player.togglePrayer(itemIndex, prayers)
    player.updateOverhead(prayers)
}

on<InterfaceOption>({ name == "prayer_list" && component == "quick_prayers" }) { player: Player ->
    player.togglePrayer(itemIndex, player.getQuickVarKey())
}

fun Player.togglePrayer(prayerIndex: Int, listKey: String) {
    val curses = isCurses()
    val enum = if (curses) curseEnumId else prayerEnumId
    val params = getPrayerParameters(prayerIndex, enum)
    val name = getPrayerName(params)
        ?: return logger.warn { "Unable to find prayer button $prayerIndex $listKey $params" }
    val activated = hasVar(listKey, name)
    if (activated) {
        removeVar(listKey, name)
    } else {
        val requiredLevel = params?.get(737) as? Int ?: 0
        // TODO level check
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

/**
 * Quick prayers
 * Until the new quick prayer selection is confirmed old
 * quick prayers are stored in [TEMP_QUICK_PRAYERS]
 */

on<InterfaceOption>({ name == "prayer_orb" && component == "orb" && option == "Select Quick Prayers" }) { player: Player ->
    val selecting = player.toggleVar(SELECTING_QUICK_PRAYERS)
    if (selecting) {
        player.setVar("tab", Tab.PrayerList)
        player[TEMP_QUICK_PRAYERS] = player.getVar(player.getQuickVarKey(), 0)
    } else if (player.contains(TEMP_QUICK_PRAYERS)) {
        player.saveQuickPrayers()
    }
    if (selecting) {
        player.interfaceOptions.unlockAll(name, "quick_prayers", 0..29)
    } else {
        player.interfaceOptions.unlockAll(name, "regular_prayers", 0..29)
    }
}

on<InterfaceOption>({ name == "prayer_orb" && component == "orb" && option == "Turn Quick Prayers On" }) { player: Player ->
    val active = player.toggleVar(USING_QUICK_PRAYERS)
    val activePrayers = player.getActiveVarKey()
    if (active) {
        val quickPrayers: Int = player.getOrNull(TEMP_QUICK_PRAYERS) ?: player.getVar(player.getQuickVarKey(), 0)
        if (quickPrayers > 0) {
            player.setVar(activePrayers, quickPrayers)
        }
    } else {
        player.setVar(activePrayers, 0)
    }
    player.updateOverhead(activePrayers)
}

on<InterfaceOption>({ name == "prayer_list" && component == "confirm" && option == "Confirm Selection" }) { player: Player ->
    player.saveQuickPrayers()
}

on<Unregistered>({ it.contains(TEMP_QUICK_PRAYERS) }) { player: Player ->
    player.cancelQuickPrayers()
}

fun Player.saveQuickPrayers() {
    setVar(SELECTING_QUICK_PRAYERS, false)
    clear(TEMP_QUICK_PRAYERS)
}

fun Player.cancelQuickPrayers() {
    setVar(getQuickVarKey(), get(TEMP_QUICK_PRAYERS, 0))
    clear(TEMP_QUICK_PRAYERS)
}

fun getPrayerName(params: HashMap<Long, Any>?): String? {
    val description = params?.getOrDefault(734, null) as? String ?: return null
    return nameRegex.find(description)?.groupValues?.lastOrNull()
}

fun getPrayerParameters(index: Int, enumId: Int): HashMap<Long, Any>? {
    val enum = enums.get(enumId).map!!
    return structs.get(enum[index] as Int).params
}

fun Player.getActiveVarKey(): String = if (isCurses()) ACTIVE_CURSES else ACTIVE_PRAYERS

fun Player.getQuickVarKey(): String = if (isCurses()) QUICK_CURSES else QUICK_PRAYERS

fun Player.isCurses(): Boolean = getVar(PRAYERS, "") == "curses"

fun Player.updateOverhead(listKey: String) {
    //TODO update stats
    val changed = if (isCurses()) {
        setCurseIcon(listKey)
    } else {
        setPrayerIcon(listKey)
    }
    if (changed) {
        flagAppearance()
    }
}

fun Player.setCurseIcon(listKey: String): Boolean {
    var value = -1
    when {
        hasVar(listKey, "Wrath") -> value = 19
        hasVar(listKey, "Soul Split") -> value = 20
        else -> {
            if (hasVar(listKey, "Deflect Summoning")) {
                value += 4
            }

            value += when {
                hasVar(listKey, "Deflect Magic") -> if (value > -1) 3 else 2
                hasVar(listKey, "Deflect Missiles") -> if (value > -1) 2 else 3
                hasVar(listKey, "Deflect Melee") -> 1
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

fun Player.setPrayerIcon(listKey: String): Boolean {
    var value = -1
    when {
        hasVar(listKey, "Retribution") -> value = 3
        hasVar(listKey, "Redemption") -> value = 5
        hasVar(listKey, "Smite") -> value = 4
        else -> {
            if (hasVar(listKey, "Protect from Summoning")) {
                value += 8
            }

            value += when {
                hasVar(listKey, "Protect from Magic") -> 3
                hasVar(listKey, "Protect from Missiles") -> 2
                hasVar(listKey, "Protect from Melee") -> 1
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