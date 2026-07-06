package content.skill.hunter

import content.entity.effect.transform
import content.entity.player.inv.item.drop
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
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
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class NetTrap : Script {
    init {
        objectOperate("Set-trap", "*_net") { (target) ->
            layTrap(target.id, target)
        }

        objectOperate("Dismantle", "net") { (target) ->
            val dir = target.direction().inverse()
            val trap = GameObjects.getLayer(target.tile.add(dir), ObjectLayer.GROUND) ?: return@objectOperate
            dismantleTrap(trap.id.removeSuffix("_setup").removeSuffix("_failed"), target.tile, null)
        }

        objectOperate("Dismantle", "*_net_failed") { (target) ->
            val dir = target.direction()
            dismantleTrap(target.id.removeSuffix("_setup").removeSuffix("_failed"), target.tile.add(dir), null)
        }

        objectOperate("Dismantle", "*_net_setup") { (target) ->
            var tile = target.tile.add(target.direction())
            dismantleTrap(target.id.removeSuffix("_setup"), tile, null)
        }

        objectOperate("Check", "*_net_caught") { (target) ->
            val id = when (target.id) {
                "swamp_net_caught" -> "swamp_lizard"
                else -> "${target.id.removeSuffix("_net_caught")}_salamander"
            }
            dismantleTrap(target.id.removeSuffix("_caught"), target.tile.add(target.direction()), Rows.get("creatures.$id"))
        }

        objectOperate("Investigate", "net") { (target) ->
            val npc = NPCs.find(target.tile, "hunting_sapling_trap_npc")
            investigate(npc)
        }

        objectOperate("Investigate", "*_net_setup") { (target) ->
            val npc = NPCs.find(target.tile.add(target.direction()), "hunting_sapling_trap_npc")
            investigate(npc)
        }

        itemOnObjectOperate("*", "net,*_net_setup") { (target, item) ->
            var trap = if (target.id == "net") {
                GameObjects.getLayer(target.tile.add(target.direction().inverse()), ObjectLayer.GROUND)!!
            } else {
                target
            }
            when {
                item.id == "unlit_torch" -> message("I should light the torch before using it to smoke the trap.")
                item.id == "torch_lit" -> Traps.smoke(this, trap.id.removeSuffix("_setup"), trap.tile.add(target.direction()))
                item.id.endsWith("_tar") -> bait(item, trap)
                item.def.contains(Params.HEALS) -> message("I don't think I'd catch much using that as bait.")
                else -> noInterest()
            }
        }

        huntNPC("net_trap") { target ->
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
            var chance = Traps.chance(this, creature)
            val success = Level.success(player.levels.get(Skill.Hunter), chance)
            val trapId = creature.string("trap")
            target.walkToDelay(tile)
            target.delay(1)
            despawn(100)
            val net = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            net.remove()
            val trap = GameObjects.getLayer(tile.add(net.direction().inverse()), ObjectLayer.GROUND) ?: return@huntNPC
            areaSound("twitch_net", tile)
            if (success) {
                target.levels.set(Skill.Constitution, 0)
                var replaced = trap.replace("${trapId}_catching")
                delay(1)
                replaced = replaced.replace("${trapId}_caught")
                player.message("Something has been caught in your trap!")
                areaSound("salamander_hit", tile)
            } else {
                var replaced = trap.replace("${trapId}_failing")
                delay(1)
                replaced = replaced.replace("${trapId}_failed")
                NPCs.remove(this)
            }
        }

        npcDespawn("hunting_sapling_trap_npc") {
            val trap = GameObjects.getLayer(tile.add(direction.inverse()), ObjectLayer.GROUND) ?: return@npcDespawn
            val player = owner ?: return@npcDespawn
            player.dec("trap_count")
            val net = GameObjects.findOrNull(trap.tile.add(trap.direction()), "net")
            net?.remove()
            GameObjects.remove(trap)
            if (lifecycle == 0 || trap.id.endsWith("_net_failed")) {
                player.message("The net trap that you set has collapsed.")
            }
            if (lifecycle == 0 || trap.id.endsWith("_net_failed") || player["logged_out", false]) {
                for (item in listOf("rope", "small_fishing_net")) {
                    player.drop(tile, item)
                }
            }
        }
    }

    private fun Player.bait(item: Item, trap: GameObject) {
        if (item.id != Tables.item("traps.${trap.id.removeSuffix("_setup")}.bait")) {
            message("This is the wrong sort of tar for these lizards.")
            return
        }
        val npc = NPCs.find(trap.tile.add(trap.direction()), "hunting_sapling_trap_npc")
        if (npc.contains("bait")) {
            message("You've already baited this trap.")
            return
        }
        if (!inventory.remove(item.id)) {
            return
        }
        anim("lay_trap_small")
        sound("drop_item", delay = 25)
        npc["bait"] = item.id
        message("You place a blob of tar on the net as bait.")
    }

    private fun Player.investigate(npc: NPC) {
        val bait: String? = npc["bait"]
        if (bait != null) {
            message("This trap has been baited with ${bait.toLowerSpaceCase()}.")
        } else {
            message("This trap has been set without any bait.")
        }
        if (npc["smoked", false]) {
            message("The scent on this trap has been masked.")
        } else {
            message("Your scent lingers around this trap.")
        }
    }

    private suspend fun Player.layTrap(trapId: String, obj: GameObject) {
        val trap = Rows.getOrNull("traps.$trapId") ?: return
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
        val items = listOf("rope", "small_fishing_net")
        for (item in items) {
            if (!inventory.contains(item)) {
                message("You need${item.an()} ${item.toLowerSpaceCase()} to lay this trap.")
                return
            }
        }
        arriveDelay()
        message("You begin setting up ${if (max == 1) "the" else "a"} trap.", ChatType.Filter)
        anim("lay_net_trap")
        sound("lay_net_trap")
        delay(1)
        for (item in items) {
            inventory.remove(item)
        }
        inc("trap_count")
        obj.replace("${trapId}_setup")
        val dir = obj.direction()
        NPCs.add("hunting_sapling_trap_npc", obj.tile.add(dir), direction = dir, ticks = 100, owner = this)
        GameObjects.add("net", obj.tile.add(dir), rotation = dir.rotation())
        stepAway(obj)
    }

    private suspend fun Player.dismantleTrap(trapId: String, tile: Tile, creature: RowDefinition?) {
        val trap = Rows.get("traps.$trapId")
        val npc = NPCs.findOrNull(tile, "hunting_sapling_trap_npc") ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val loot = creature?.itemList("loot") ?: emptyList()
        val items = trap.itemList("items").toMutableList()
        val bait: String? = npc["bait"]
        if (loot.isEmpty() && bait != null) {
            items.add(bait)
        }
        val size = items.size + loot.size
        if (inventory.spaces < size) {
            val slots = size - inventory.spaces
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        anim("take_trap")
        sound("trap_dismantle", delay = 25)
        delay(1)
        NPCs.remove(npc)
        for (item in items) {
            inventory.add(item)
        }
        message("You dismantle the trap.", ChatType.Filter)
        if (creature != null) {
            for (item in loot) {
                inventory.add(item)
            }
            exp(Skill.Hunter, creature.int("xp") / 10.0)
            message("You've caught a ${creature.rowId.toLowerSpaceCase()}.", ChatType.Filter)
        }
    }
}
