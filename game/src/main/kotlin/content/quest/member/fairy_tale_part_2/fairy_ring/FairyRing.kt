package content.quest.member.fairy_tale_part_2.fairy_ring

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.quest.quest
import content.quest.questCompleted
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.suspend.pauseString
import world.gregs.voidps.type.Tile

class FairyRing(val fairyRing: FairyRingCodes) : Script {

    val hub = Tile(2412, 4434)

    val Player.code: String
        get() = "${get("fairy_ring_code_1", "a")}${get("fairy_ring_code_2", "i")}${get("fairy_ring_code_3", "p")}"

    init {
        objectOperate("Use", "fairy_ring_*") { (target) ->
            if (target.id == "fairy_ring_zanaris" || target.id == "fairy_ring_zanaris_2") {
                return@objectOperate
            }
            if (!canUseFairyRing()) {
                return@objectOperate
            }
            walkOverDelay(target.tile)
            this["fairy_rings_unlocked"] = true
            Teleport.teleport(this, hub, "fairy_ring")
        }

        objectOperate("Use", "fairy_ring_zanaris_2") { (target) ->
            if (!canUseFairyRing()) {
                return@objectOperate
            }
            resetDial()
            open("fairy_ring")
            open("travel_log")
            val code = pauseString()
            closeMenu()
            delay()
            walkOverDelay(target.tile)
            delay()
            this["fairy_rings_unlocked"] = true
            val destination = fairyRing.codes[code]
            if (destination == null || destination.tile == Tile.EMPTY || (destination.quest.isNotEmpty() && !questCompleted(destination.quest))) {
                Teleport.teleport(this, hub.toCuboid(radius = 2).random(this) ?: hub, "fairy_ring")
                return@objectOperate
            }
            Teleport.teleport(this, destination.tile, "fairy_ring")
            val list: MutableList<String> = getOrPut("travel_log_locations") { mutableListOf() }
            if (!list.contains(code)) {
                list.add(code)
            }
        }

        interfaceClosed("fairy_ring") {
            open("inventory")
        }

        interfaceOption("Teleport", "fairy_ring:teleport") {
            val code = code
            (suspension as? Suspension.StringEntry)?.resume(code)
        }

        interfaceOpened("fairy_ring") {
            tab(Tab.Inventory)
        }

        interfaceOption("Rotate clockwise", "fairy_ring:clockwise_*") {
            val codeIndex = it.component.removePrefix("clockwise_").toInt()
            rotate(this, codeIndex, 1)
        }

        interfaceOption("Rotate anticlockwise", "fairy_ring:anticlockwise_*") {
            val codeIndex = it.component.removePrefix("anticlockwise_").toInt()
            rotate(this, codeIndex, -1)
        }
    }

    fun Player.canUseFairyRing(): Boolean {
        if (quest("fairy_tale_ii") == "unstarted") {
            message("You don't have permission to use that fairy ring.")
            return false
        }
        if (!questCompleted("fairy_tale_iii") && weapon.id != "dramen_staff" && weapon.id != "lunar_staff") {
            message("The fairy ring only works for those who wield fairy magic.")
            return false
        }
        return true
    }

    fun Player.resetDial() {
        for (index in 1..3) {
            val definition = VariableDefinitions.get("fairy_ring_code_$index") ?: continue
            val list = definition.values as ListValues
            this["fairy_ring_code_$index"] = list.default()
        }
    }

    fun rotate(player: Player, codeIndex: Int, amount: Int) {
        val definition = VariableDefinitions.get("fairy_ring_code_$codeIndex") ?: return
        val list = definition.values as ListValues
        val current = player["fairy_ring_code_$codeIndex", list.default()]
        val valueIndex = list.values.indexOf(current)
        val next = list.values[(valueIndex + amount) and 3]
        player["fairy_ring_code_$codeIndex"] = next
    }
}
