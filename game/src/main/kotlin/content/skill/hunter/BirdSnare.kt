package content.skill.hunter

import content.entity.effect.transform
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.chat.plural
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
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class BirdSnare : Script {
    init {
        itemOption("Lay", "bird_snare") {
            layTrap(it.item.id, null)
        }

        floorItemOperate("Lay") { (item) ->
            if (item.id == "bird_snare") {
                layTrap(item.id, item)
            }
        }

        objectOperate("Dismantle", "bird_snare,bird_snare_fail") { (target) ->
            dismantleTrap("bird_snare", target)
        }

        objectOperate("Investigate", "bird_snare") { (target) ->
            val id = Tables.npc("traps.bird_snare.npc")
            val npc = NPCs.find(target.tile, id)
            if (npc["baited", false]) {
                // TODO
                message("This trap has been set without any bait.")
            } else if (npc["smoked", false]) {
                message("The scent on this trap has been masked.")
            } else {
                message("Your scent lingers around this trap.") // TODO outfits?
            }
        }

        itemOnObjectOperate("*", "bird_snare") {
            when {
                it.item.id == "unlit_torch" -> message("I should light the torch before using it to smoke the trap.")
                it.item.id == "torch_lit" -> smoke(it.target)
                it.item.def.contains(Params.HEALS) -> message("There isn't really anywhere to put any bait on this trap.")
                else -> noInterest()
            }
        }

        huntNPC("bird_snare") { target ->
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
            var chance = creature.intRange("chance")
            if (get("baited", false)) {
                chance = (chance.first + 7)..(chance.last + 7) // 3%
            } else if (get("smoked", false)) {
                chance = (chance.first + 5)..(chance.last + 5) // 2%
            }
            val success = Level.success(player.levels.get(Skill.Hunter), chance)
            target.walkToDelay(tile)
            target.walkOverDelay(tile)
            target.face(Direction.SOUTH)
            target.anim("bird_land")
            target.delay(1)
            target.anim(if (success) "bird_catch" else "bird_fail")
            target.delay(1)
            despawn(100)
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            if (!success) {
                trap.replace("bird_snare_fail")
                // TODO is there a message for this?
                return@huntNPC
            }
            target.levels.set(Skill.Constitution, 0)
            trap.replace(Tables.obj("creatures.${target.id}.caught_obj"))
            player.message("Something has been caught in your trap!")
            areaSound("bird_caught", tile)
        }
    }

    private fun Player.smoke(target: GameObject) {
        val id = Tables.npc("traps.${target.id}.npc")
        val npc = NPCs.find(target.tile, id)
        if (npc["owner", ""] != accountName) {
            message("This is not your trap!") // TODO proper message
            return
        }
        if (npc["smoked", false]) { // TODO what if baited?
            message("This trap is already smoked.") // TODO proper message
            return
        }
        message("You use the smoke from the torch to remove your scent from the trap.", type = ChatType.Filter)
        anim("lay_trap_small")
        areaSound("hunting_smoke2", tile = target.tile, radius = 5)
        npc["smoked"] = true
    }

    private suspend fun Player.layTrap(trapId: String, floorItem: FloorItem?) {
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
        val message = trap.stringOrNull("item_message")
        val requires = trap.itemList("requires")
        for (item in requires) {
            if (!carriesItem(item)) {
                message(message ?: "You need${item.an()} ${item.toLowerSpaceCase()} to lay this trap.")
                return
            }
        }
        val items = trap.itemList("items")
        for (item in items) {
            if (!carriesItem(item) && floorItem != null && floorItem.id != item) {
                message(message ?: "You need${item.an()} ${item.toLowerSpaceCase()} to lay this trap.")
                return
            }
        }
        arriveDelay()
        message("You begin setting up ${if (max == 1) "the" else "a"} trap.", ChatType.Filter)
        anim("lay_trap")
        sound("set_noose")
        delay(3)
        for (item in items) {
            if (floorItem != null && item == floorItem.id) {
                FloorItems.remove(floorItem)
                continue
            }
            inventory.remove(item)
        }
        inc("trap_count")
        NPCs.add(trap.npc("npc"), tile, ticks = 100, owner = this)
        val obj = GameObjects.add(trap.obj("trap"), tile)
        stepAway(obj)
    }

    private suspend fun Player.dismantleTrap(trapId: String, target: GameObject) {
        val id = Tables.npc("traps.${trapId}.npc")
        val npc = NPCs.findOrNull(target.tile, id) ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val trap = Rows.get("traps.${trapId}")
        val items = trap.itemList("items")
        if (inventory.spaces < items.size) {
            val slots = inventory.spaces - items.size
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
        if (target.id.startsWith("pitfall")) {
            set(target.id, "empty")
            return
        }
        if (target.id.endsWith("_net_setup")) {
            val dir = target.direction()
            val net = GameObjects.findOrNull(target.tile.add(dir), "net")
            net?.remove()
        }
        GameObjects.remove(target)
    }

}