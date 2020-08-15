package rs.dusk.world.activity.combat.prayer

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.cache.config.decoder.StructDecoder
import rs.dusk.cache.definition.decoder.EnumDecoder
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.*
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.player.flagAppearance
import rs.dusk.engine.entity.character.update.visual.player.headIcon
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.PRAYERS
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.QUICK_CURSES
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.QUICK_PRAYERS
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.TEMP_QUICK_PRAYERS
import rs.dusk.world.activity.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import rs.dusk.world.interact.entity.player.display.InterfaceInteraction
import rs.dusk.world.interact.entity.player.display.Tab
import rs.dusk.world.interact.entity.player.spawn.logout.Logout

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

InterfaceInteraction where { name == "prayer_list" && component == "regular_prayers" } then {
    val prayers = player.getActiveVarKey()
    player.togglePrayer(itemIndex, prayers)
    player.updateOverhead(prayers)
}

InterfaceInteraction where { name == "prayer_list" && component == "quick_prayers" } then {
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

InterfaceInteraction where { name == "prayer_orb" && component == "orb" && option == "Select Quick Prayers" } then {
    val selecting = player.toggleVar(SELECTING_QUICK_PRAYERS)
    if (selecting) {
        player.setVar("tab", Tab.PrayerList)
        player[TEMP_QUICK_PRAYERS] = player.getVar(player.getQuickVarKey(), 0)
    } else if (player.has(TEMP_QUICK_PRAYERS)) {
        player.saveQuickPrayers()
    }
    player.interfaces.sendSetting("prayer_list", if (selecting) "quick_prayers" else "regular_prayers", 0, 29, 2)
}

InterfaceInteraction where { name == "prayer_orb" && component == "orb" && option == "Turn Quick Prayers On" } then {
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

InterfaceInteraction where { name == "prayer_list" && component == "confirm" && option == "Confirm Selection" } then {
    player.saveQuickPrayers()
}

Logout where { player.has(TEMP_QUICK_PRAYERS) } then {
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