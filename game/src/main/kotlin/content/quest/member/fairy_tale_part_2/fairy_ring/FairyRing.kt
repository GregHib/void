package content.quest.member.fairy_tale_part_2.fairy_ring

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.quest.quest
import content.quest.questCompleted
import content.skill.magic.spell.Teleport
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.type.Tile

class FairyRing : Script {

    val fairyRing: FairyRingCodes by inject()

    val variableDefinitions: VariableDefinitions by inject()

    val Player.code: String
        get() = "${get("fairy_ring_code_1", "a")}${get("fairy_ring_code_2", "j")}${get("fairy_ring_code_3", "r")}"

    init {
        objectOperate("Use", "fairy_ring_*") { (target) ->
            if (quest("fairy_tale_ii") == "unstarted") {
                message("You don't have permission to use that fairy ring.")
                return@objectOperate
            }
            if (!questCompleted("fairy_tale_iii") && weapon.id != "dramen_staff") {
                message("The fairy ring only works for those who wield fairy magic.")
                return@objectOperate
            }
            open("fairy_ring")
            open("travel_log")
            val code = StringSuspension.get(this)
            val fairyRing = fairyRing.codes[code] ?: return@objectOperate
            if (fairyRing.tile == Tile.EMPTY) {
                return@objectOperate
            }
            closeMenu()
            delay()
            walkOverDelay(target.tile)
            delay()
            Teleport.teleport(this, fairyRing.tile, "fairy_ring")
            val list: MutableList<String> = getOrPut("travel_log_locations") { mutableListOf() }
            list.add(code)
        }

        interfaceClose("fairy_ring") { player ->
            player.open("inventory")
        }

        interfaceOption("Teleport", "teleport", "fairy_ring") {
            val code = player.code
            (player.dialogueSuspension as? StringSuspension)?.resume(code)
        }

        interfaceOpen("fairy_ring") { player ->
            player.tab(Tab.Inventory)
        }

        interfaceOption("Rotate clockwise", "clockwise_*", "fairy_ring") {
            val codeIndex = component.removePrefix("clockwise_").toInt()
            rotate(player, codeIndex, 1)
        }

        interfaceOption("Rotate anticlockwise", "anticlockwise_*", "fairy_ring") {
            val codeIndex = component.removePrefix("anticlockwise_").toInt()
            rotate(player, codeIndex, -1)
        }
    }

    fun rotate(player: Player, codeIndex: Int, amount: Int) {
        val definition = variableDefinitions.get("fairy_ring_code_$codeIndex") ?: return
        val list = definition.values as ListValues
        val current = player["fairy_ring_code_$codeIndex", list.default()]
        val valueIndex = list.values.indexOf(current)
        val next = list.values[(valueIndex + amount) and 3]
        player["fairy_ring_code_$codeIndex"] = next
    }
}
