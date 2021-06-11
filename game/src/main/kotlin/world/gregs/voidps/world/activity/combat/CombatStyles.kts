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
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.utility.func.plural
import world.gregs.voidps.utility.inject
import java.util.concurrent.TimeUnit

IntVariable(43, Variable.Type.VARP, true, 0).register("attack_style")
NegativeBooleanVariable(172, Variable.Type.VARP, true).register("auto_retaliate")
BooleanVariable(301, Variable.Type.VARP).register("special_attack")

val decoder: ClientScriptDecoder by inject()

val styles = mutableMapOf<Int, Array<Pair<String, String>>>()
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

fun getStyles(index: Int, instructions: IntArray, strings: Array<String?>): Array<Pair<String, String>> {
    val types = mutableListOf<String>()
    val styles = mutableListOf<String>()
    var index: Int = index
    var last = -1
    while (index < instructions.lastIndex) {
        val instruction = instructions[index]
        when (instruction) {
            PUSH_STRING -> strings[index]?.also { string ->
                if (last == GOTO || last == MERGE_STRINGS) {
                    types.add(string)
                } else if (last == PUSH_INT) {
                    styles.add(string)
                }
            }
            CALL_CS2 -> break
        }
        last = instruction
        index++
    }
    return types.mapIndexed { i, s -> s to styles[i] }.toTypedArray()
}

on<InterfaceOpened>({ name == "combat_styles" }) { player: Player ->
    player.interfaceOptions.unlockAll(name, "style1")
    player.interfaceOptions.unlockAll(name, "style2")
    player.interfaceOptions.unlockAll(name, "style3")
    player.interfaceOptions.unlockAll(name, "style4")
    player.sendVar("attack_style")
    player.sendVar("auto_retaliate")
    updateStyles(player, player.getVar("attack_style"))
}

on<InterfaceOption>({ name == "combat_styles" && component.startsWith("style") }) { player: Player ->
    val index = component.replace("style", "").toIntOrNull() ?: return@on
    player.setVar("attack_style", index - 1)
    updateStyles(player, index - 1)
}

fun updateStyles(player: Player, index: Int) {
    val type = player.equipped(EquipSlot.Weapon).def.params?.get(686)
    val styles = styles[if (styles.containsKey(type)) type else 0]
    val style = styles?.getOrNull(index)
    player["attack_type"] = style?.first ?: ""
    player["attack_style"] = style?.second ?: ""
}

on<InterfaceOption>({ name == "combat_styles" && component == "special_attack_bar" && option == "Use" }) { player: Player ->
    player.toggleVar("special_attack")
}