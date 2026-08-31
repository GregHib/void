package content.skill.hunter

import content.entity.effect.transform
import content.entity.player.inv.item.drop
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class DeadfallTrap : Script {
    init {
        objectOperate("Set-trap", "boulder_trap") { (target) ->
            setTrap(target)
        }

        objectOperate("Dismantle", "boulder_trap_setup") { (target) ->
            dismantleTrap(target, null)
        }

        objectOperate("Check", "boulder_trap_wild_kebbit,boulder_trap_barb_tailed_kebbit,boulder_trap_prickly_kebbit,boulder_trap_sabre_toothed_kebbit") { (target) ->
            dismantleTrap(target, creature = Rows.get("creatures.${target.id.removePrefix("boulder_trap_")}"))
        }

        objectOperate("Investigate", "boulder_trap_setup") { (target) ->
            val npc = NPCs.find(target.tile, "hunting_deadfall_trap_npc")
            Traps.investigate(this, npc)
        }

        itemOnObjectOperate("*", "boulder_trap_setup") {
            when {
                it.item.id == "unlit_torch" -> message("I should light the torch before using it to smoke the trap.")
                it.item.id == "torch_lit" -> Traps.smoke(this, "boulder_trap", it.target.tile)
                it.item.def.contains(Params.HEALS) -> message("I don't think I'd catch much using that as bait.")
                else -> noInterest()
            }
        }

        huntNPC("deadfall") { target ->
            if (transform.endsWith("_off")) {
                return@huntNPC
            }
            val creature = Rows.getOrNull("creatures.${target.id}") ?: return@huntNPC
            val account: String = get("owner") ?: return@huntNPC
            val player = Players.findByAccount(account) ?: return@huntNPC
            if (!player.has(Skill.Hunter, creature.int("level"))) {
                return@huntNPC
            }
            if (tile.distanceTo(target.tile) > 3) {
                return@huntNPC
            }
            transform("${id}_off")
            val chance = Traps.chance(this, creature)
            val success = Level.success(player.levels.get(Skill.Hunter), chance)
            target.walkToDelay(tile)
            despawn(100)
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            areaSound("deadfall_catch", tile)
            if (!success) {
                target.anim(creature.anim("fail_anim"))
                val failing = trap.replace("boulder_trap_fail")
                target.delay(2)
                collapse(player, this, failing)
                player.message("Your deadfall trap has collapsed.")
                return@huntNPC
            }
            target.anim(creature.anim("catch_anim"))
            val catching = trap.replace("${Tables.obj("creatures.${target.id}.caught_obj")}_catching")
            target.delay(2)
            target.levels.set(Skill.Constitution, 0)
            catching.replace(Tables.obj("creatures.${target.id}.caught_obj"))
            player.message("Something has been caught in your trap!")
        }

        npcDespawn("hunting_deadfall_trap_npc") {
            val player = owner ?: return@npcDespawn
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@npcDespawn
            if (trap.id == "boulder_trap") {
                return@npcDespawn
            }
            player.dec("trap_count")
            player.dec("deadfall_count")
            GameObjects.remove(trap)
            val drop = if (lifecycle == 0) {
                player.message("The deadfall trap that you constructed has collapsed.")
                true
            } else {
                player["logged_out", false]
            }
            if (drop) {
                player.drop(trap.tile, "logs")
            }
        }
    }

    private suspend fun Player.setTrap(target: GameObject) {
        val trap = Rows.getOrNull("traps.boulder_trap") ?: return
        val level = levels.get(Skill.Hunter)
        if (!has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        for (item in trap.itemList("requires")) {
            if (!inventory.contains(item)) {
                message("You need a ${item.toLowerSpaceCase()} in order to set a deadfall trap.")
                return
            }
        }
        if (!inventory.contains("logs")) {
            message("You need some logs in order to set a deadfall trap.")
            return
        }
        if (get("deadfall_count", 0) >= trap.int("max")) {
            message("You can only set up one deadfall trap at a time.")
            return
        }
        val max = Traps.max(level, 5)
        val trapCount = get("trap_count", 0)
        if (trapCount >= max) {
            message("You may setup only $max ${"trap".plural(max)} at a time at your Hunter level.")
            return
        }
        arriveDelay()
        message("You begin setting up the trap.", ChatType.Filter)
        anim("lay_trap")
        sound("set_deadfall")
        delay(3)
        inventory.remove("logs")
        inc("trap_count")
        inc("deadfall_count")
        NPCs.add("hunting_deadfall_trap_npc", target.tile, ticks = 100, owner = this)
        target.replace("boulder_trap_setup")
        stepAway(target)
    }

    private suspend fun Player.dismantleTrap(target: GameObject, creature: RowDefinition?) {
        val npc = NPCs.findOrNull(target.tile, "hunting_deadfall_trap_npc") ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val loot = creature?.itemList("loot") ?: emptyList()
        val items = if (loot.isEmpty()) listOf("logs") else emptyList()
        val size = items.size + loot.size
        if (inventory.spaces < size) {
            val slots = size - inventory.spaces
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        anim("take_trap")
        sound("take_branches", delay = 25)
        delay(2)
        collapse(this, npc, target)
        for (item in items) {
            inventory.add(item)
        }
        message("You dismantle the trap.", ChatType.Filter)
        if (creature != null) {
            for (item in loot) {
                inventory.add(item)
            }
            exp(Skill.Hunter, creature.int("xp") / 10.0)
            message("You've caught a ${creature.rowId.toLowerSpaceCase()}!", ChatType.Filter)
        }
    }

    private fun collapse(player: Player, npc: NPC, target: GameObject) {
        player.dec("trap_count")
        player.dec("deadfall_count")
        NPCs.remove(npc)
        GameObjects.remove(target)
    }
}
