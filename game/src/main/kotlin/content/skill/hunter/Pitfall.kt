package content.skill.hunter

import content.entity.combat.Combat
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Pitfall : Script {
    init {
        npcOperate("Tease") {
            val target = it.target
            if (!has(Skill.Hunter, 99)) { // TODO
                message("You need a higher hunter level") // TODO proper message
                return@npcOperate
            }
            if (!inventory.contains("teasing_stick")) {
                message("You need a teasing stick.")// TODO proper message
                return@npcOperate
            }
            it.updateInteraction {
                Combat.combat(it.character, target)
            }
        }

        objectOperate("Trap", "pitfall") {
            layTrap(it.target)
        }

        objectOperate("Trap", "pitfall_*") {
            // Temp to avoid #1059
        }

        objectOperate("Jump", "pitfall_spiked") { (target) ->
            val dir = if (target.rotation == 1 || target.rotation == 3) {
                if (tile.x > target.tile.x) Direction.WEST else Direction.EAST
            } else {
                if (tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
            }
            anim("agility_pyramid_gap_jump")
            exactMove(tile.add(dir).add(dir).add(dir), delay = 53, direction = dir)
            areaSound("hunting_jump", target.tile)
        }

        objectOperate("Dismantle", "pitfall_*") { (target) ->
            dismantleTrap("pitfall", target, target.tile)
        }

    }

    private suspend fun Player.layTrap(obj: GameObject) {
        val trap = Rows.getOrNull("traps.pitfall") ?: return
        val level = levels.get(Skill.Hunter)
        if (!has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        if (get(obj.id, "empty") != "empty") {
            return
        }
        val max = Traps.max(level, 5)
        val trapCount = get("trap_count", 0)
        if (trapCount >= max) {
            message("You may setup only $max ${"trap".plural(max)} at a time at your Hunter level.")
            return
        }
        if (!inventory.contains("knife")) {
            message("You need a knife to lay this trap.") // TODO proper message
            return
        }
        if (!inventory.contains("logs")) {
            message("You need logs to lay this trap.") // TODO proper message
            return
        }
        // TODO collapse timer
        arriveDelay()
        anim("lay_trap_small")
        inventory.remove("logs")
        delay(1)
        sound("place_branches")
        inc("trap_count")
        set(obj.id, "spiked")
    }

    private suspend fun Player.dismantleTrap(trapId: String, target: GameObject, tile: Tile) {
        val id = Tables.npc("traps.$trapId.npc")
        val npc = NPCs.findOrNull(tile, id) ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val trap = Rows.get("traps.$trapId")
        val items = trap.itemList("items")
        if (inventory.spaces < items.size) {
            val slots = items.size - inventory.spaces
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        anim(trap.anim("take_down_anim"))
        sound("trap_dismantle", delay = 25)
        delay(2)
        removeTrap(target, npc)
        for (item in items) {
            inventory.add(item)
        }
        message("You dismantle the trap.", ChatType.Filter)
    }

    private fun Player.removeTrap(target: GameObject, npc: NPC) {
        dec("trap_count")
        NPCs.remove(npc)
        set(target.id, "empty")
    }

}