package content.skill.hunter

import content.entity.effect.transform
import content.entity.player.bank.BankDeposit
import content.entity.player.inv.item.drop
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace

class MagicBox : Script {
    init {
        itemOption("Activate", "magic_box") {
            layTrap(null)
        }

        floorItemOperate("Lay") { (item) ->
            if (item.id == "magic_box") {
                layTrap(item)
            }
        }

        objectOperate("Deactivate", "magic_box,magic_box_fail") { (target) ->
            dismantleTrap(target, null)
        }

        objectOperate("Retrieve", "magic_box_caught") { (target) ->
            dismantleTrap(target, creature = Rows.get("creatures.imp"))
        }

        objectOperate("Investigate", "magic_box") { (target) ->
            val npc = NPCs.find(target.tile, "hunting_imptrap_npc")
            if (npc["owner", ""] == accountName) {
                message("This is your magic box, ready to catch an imp.")
            } else {
                message("This isn't your magic box.")
            }
        }

        huntNPC("magic_box") { target ->
            if (transform.endsWith("_off")) {
                return@huntNPC
            }
            val creature = Rows.getOrNull("creatures.${target.id}") ?: return@huntNPC
            val account: String = get("owner") ?: return@huntNPC
            val player = Players.findByAccount(account) ?: return@huntNPC
            if (!player.has(Skill.Hunter, creature.int("level"))) {
                return@huntNPC
            }
            if (tile.distanceTo(target.tile) > 2) {
                return@huntNPC
            }
            transform("${id}_off")
            val chance = Traps.chance(this, creature)
            val success = Level.success(player.levels.get(Skill.Hunter), chance)
            target.walkToDelay(tile)
            target.walkOverDelay(tile)
            despawn(100)
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            target.anim(if (success) creature.anim("catch_anim") else creature.anim("fail_anim"))
            target.gfx("imp")
            target.delay(1)
            if (!success) {
                trap.replace("magic_box_fail")
                return@huntNPC
            }
            target.levels.set(Skill.Constitution, 0)
            val catching = trap.replace("magic_box_catching")
            delay(1)
            catching.replace("magic_box_caught")
            player.message("Something has been caught in your trap!")
        }

        npcDespawn("hunting_imptrap_npc") {
            val player = owner ?: return@npcDespawn
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@npcDespawn
            player.dec("trap_count")
            GameObjects.remove(trap)
            val drop = if (lifecycle == 0) {
                player.message("The magic box that you activated has stopped working.")
                true
            } else {
                player["logged_out", false]
            }
            if (drop) {
                player.drop(trap.tile, "magic_box")
            }
        }

        itemOnItem("*", "imp_in_a_box_2,imp_in_a_box_1") { item, box ->
            if (item.id.startsWith("imp_in_a_box") || item.id.startsWith("magic_box")) {
                message("The imp refuses to take that to your bank.")
                return@itemOnItem
            }
            BankDeposit.deposit(this, inventory, item, 1, check = false)
            if (box.id == "imp_in_a_box_2") {
                inventory.replace("imp_in_a_box_2", "imp_in_a_box_1")
                message("The imp takes the item to your bank.")
            } else {
                inventory.replace("imp_in_a_box_1", "magic_box")
                message("The imp takes the item to your bank and escapes from the box.")
            }
        }
    }

    private suspend fun Player.layTrap(floorItem: FloorItem?) {
        val trap = Rows.getOrNull("traps.magic_box") ?: return
        val level = levels.get(Skill.Hunter)
        if (!has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        if (Areas.get(tile.zone).any { it.tags.contains("bank") } || GameObjects.getLayer(tile, ObjectLayer.GROUND) != null) {
            message("You can't lay a trap here.", ChatType.Filter)
            return
        }
        val max = Traps.max(level, trap.int("max"))
        val trapCount = get("trap_count", 0)
        if (trapCount >= max) {
            message("You may setup only $max ${"trap".plural(max)} at a time at your Hunter level.")
            return
        }
        arriveDelay()
        message("You begin setting up ${if (max == 1) "the" else "a"} trap.", ChatType.Filter)
        anim("lay_trap")
        sound("lay_box_trap")
        delay(3)
        if (floorItem != null) {
            FloorItems.remove(floorItem)
        } else {
            inventory.remove("magic_box")
        }
        inc("trap_count")
        NPCs.add("hunting_imptrap_npc", tile, ticks = 100, owner = this)
        val obj = GameObjects.add("magic_box", tile)
        stepAway(obj)
    }

    private suspend fun Player.dismantleTrap(target: GameObject, creature: RowDefinition?) {
        val npc = NPCs.findOrNull(target.tile, "hunting_imptrap_npc") ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val loot = creature?.itemList("loot") ?: emptyList()
        val items = if (loot.isEmpty()) listOf("magic_box") else emptyList()
        val size = items.size + loot.size
        if (inventory.spaces < size) {
            val slots = size - inventory.spaces
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        anim("take_trap")
        sound("trap_dismantle", delay = 25)
        delay(2)
        collapse(npc, target)
        for (item in items) {
            inventory.add(item)
        }
        if (creature != null) {
            for (item in loot) {
                inventory.add(item)
            }
            exp(Skill.Hunter, creature.int("xp") / 10.0)
            message("You've caught an ${creature.rowId.toLowerSpaceCase()}!", ChatType.Filter)
        } else {
            message("You dismantle the trap.", ChatType.Filter)
        }
    }

    private fun Player.collapse(npc: NPC, target: GameObject) {
        dec("trap_count")
        NPCs.remove(npc)
        GameObjects.remove(target)
    }
}
