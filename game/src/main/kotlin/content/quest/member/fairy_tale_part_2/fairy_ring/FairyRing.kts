package content.quest.member.fairy_tale_part_2.fairy_ring

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.quest.quest
import content.quest.questCompleted
import content.skill.magic.spell.Teleport
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile

val fairyRing: FairyRingCodes by inject()

objectOperate("Use", "fairy_ring_*") {
    if (player.quest("fairy_tale_ii") == "unstarted") {
        player.message("You don't have permission to use that fairy ring.")
        return@objectOperate
    }
    if (!player.questCompleted("fairy_tale_iii") && player.weapon.id != "dramen_staff") {
        player.message("The fairy ring only works for those who wield fairy magic.")
        return@objectOperate
    }
    player.open("fairy_ring")
    player.open("travel_log")
}

interfaceClose("fairy_ring") { player ->
    player.open("inventory")
}

interfaceOption("Teleport", "teleport", "fairy_ring") {
    val code = player.code
    val fairyRing = fairyRing.codes[code] ?: return@interfaceOption
    if (fairyRing.tile == Tile.EMPTY) {
        return@interfaceOption
    }
    Teleport.teleport(player, fairyRing.tile, "fairy_ring")
    val list: MutableList<String> = player["travel_log_locations"] ?: return@interfaceOption
    list.add(code)
}

interfaceOpen("fairy_ring") { player ->
    player.tab(Tab.Inventory)
}

val variableDefinitions: VariableDefinitions by inject()

interfaceOption("Rotate clockwise", "clockwise_*", "fairy_ring") {
    val codeIndex = component.removePrefix("clockwise_").toInt()
    rotate(player, codeIndex, 1)
}

interfaceOption("Rotate anticlockwise", "anticlockwise_*", "fairy_ring") {
    val codeIndex = component.removePrefix("anticlockwise_").toInt()
    rotate(player, codeIndex, -1)
}

fun rotate(player: Player, codeIndex: Int, amount: Int) {
    val definition = variableDefinitions.get("fairy_ring_code_${codeIndex}") ?: return
    val list = definition.values as ListValues
    val current = player["fairy_ring_code_${codeIndex}", list.default()]
    val valueIndex = list.values.indexOf(current)
    val next = list.values[(valueIndex + amount) and 3]
    player["fairy_ring_code_${codeIndex}"] = next
}

val Player.code: String
    get() = "${get("fairy_ring_code_1", "a")}${get("fairy_ring_code_2", "j")}${get("fairy_ring_code_3", "r")}"
