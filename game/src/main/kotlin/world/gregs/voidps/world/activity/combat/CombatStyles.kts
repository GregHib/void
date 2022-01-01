package world.gregs.voidps.world.activity.combat

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.Instructions
import world.gregs.voidps.cache.definition.data.Instructions.CALL_CS2
import world.gregs.voidps.cache.definition.data.Instructions.GOTO
import world.gregs.voidps.cache.definition.data.Instructions.MERGE_STRINGS
import world.gregs.voidps.cache.definition.data.Instructions.PUSH_INT
import world.gregs.voidps.cache.definition.data.Instructions.PUSH_STRING
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.item.weaponStyle
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.engine.utility.toUnderscoreCase
import java.util.concurrent.TimeUnit

val names = arrayOf("default", "staff", "axe", "sceptre", "pickaxe", "dagger", "sword", "2h", "mace", "claws", "hammer", "whip", "fun", "pie", "spear", "halberd", "bow", "crossbow", "thrown", "chinchompa", "fixed_device", "salamander", "scythe", "flail", "", "trident", "sol")
val decoder: ClientScriptDecoder by inject()

val styles = mutableMapOf<Int, Array<Triple<String, String, String>>>()
val logger = InlineLogger()

on<World, Startup> {
    val script = decoder.get(1142)
    val switchIndex = script.instructions.indexOf(Instructions.SWITCH)
    val indices = script.switchStatementIndices ?: return@on
    val ints = script.intOperands ?: return@on
    val strings = script.stringOperands ?: return@on
    val start = System.nanoTime()
    var highest = 0
    for ((key, offset) in indices[ints[switchIndex]]) {
        val index = switchIndex + offset
        this@CombatStyles.styles[key] = getStyles(index, script.instructions, strings)
        if (index > highest) {
            highest = index
        }
    }
    var default = highest
    for (index in highest + 1 until script.instructions.lastIndex) {
        if (script.instructions[index] == CALL_CS2) {
            default = index + 1
            break
        }
    }
    styles[0] = getStyles(default, script.instructions, strings)
    logger.info { "${styles.size} combat ${"style".plural(styles.size)} loaded in ${TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)}ms." }
}

fun getStyles(index: Int, instructions: IntArray, strings: Array<String?>): Array<Triple<String, String, String>> {
    val types = mutableListOf<String>()
    val styles = mutableListOf<String>()
    val combatStyle = mutableListOf<String>()
    var index: Int = index
    var last = -1
    while (index <= instructions.lastIndex) {
        val instruction = instructions[index]
        when (instruction) {
            PUSH_STRING -> strings[index]?.also { string ->
                if (last == GOTO || last == MERGE_STRINGS) {
                    types.add(string.toUnderscoreCase())
                } else if (last == PUSH_INT) {
                    styles.add(string.toUnderscoreCase())
                } else if (last == PUSH_STRING && strings[index - 1] == "<br>" && !string.endsWith("XP")) {
                    combatStyle.add(string.toUnderscoreCase())
                }
            }
            CALL_CS2 -> break
        }
        last = instruction
        index++
    }
    return types.mapIndexed { i, s -> Triple(s, styles[i], combatStyle.getOrNull(i) ?: "") }.toTypedArray()
}

on<Registered> { npc: NPC ->
    npc["combat_style"] = npc.def["style", ""]
}

on<InterfaceOpened>({ id == "combat_styles" }) { player: Player ->
    player.sendVar("attack_style")
    player.sendVar("special_attack_energy")
    player.sendVar("auto_retaliate")
    refreshStyle(player)
}

on<InterfaceRefreshed>({ id == "combat_styles" }) { player: Player ->
    player.interfaceOptions.unlockAll(id, "style1")
    player.interfaceOptions.unlockAll(id, "style2")
    player.interfaceOptions.unlockAll(id, "style3")
    player.interfaceOptions.unlockAll(id, "style4")
}

on<ItemChanged>({ index == EquipSlot.Weapon.index }) { player: Player ->
    refreshStyle(player)
}

on<InterfaceOption>({ id == "combat_styles" && component.startsWith("style") }) { player: Player ->
    val index = component.removePrefix("style").toIntOrNull() ?: return@on
    val type = getWeaponStyleType(player)
    if (index == 1) {
        player.clear("attack_style_${names[type]}")
    } else {
        player["attack_style_${names[type]}", true] = index - 1
    }
    refreshStyle(player)
}

on<InterfaceOption>({ id == "combat_styles" && component == "retaliate" }) { player: Player ->
    player.toggleVar("auto_retaliate")
}

fun refreshStyle(player: Player) {
    val type = getWeaponStyleType(player)
    val index = player["attack_style_${names[type]}", 0]
    val style = styles[type]?.getOrNull(index)
    player.setVar("attack_style", index)
    player["attack_type"] = style?.first ?: ""
    player["attack_style"] = style?.second ?: ""
    player["combat_style"] = style?.third ?: ""
}

fun getWeaponStyleType(player: Player): Int {
    val key = player.equipped(EquipSlot.Weapon).def.weaponStyle()
    return if (styles.containsKey(key)) key else 0
}

on<InterfaceOption>({ id == "combat_styles" && component == "special_attack_bar" && option == "Use" }) { player: Player ->
    player.toggleVar("special_attack")
}