package content.skill.hunter

import content.entity.effect.transform
import content.entity.player.inv.item.drop
import content.quest.questCompleted
import net.pearx.kasechange.toLowerSpaceCase
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.move.canTravel
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
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

/**
 * Hunter traps have an invisible npc under them which has a hunt mode which
 * selects nearby targets to walk to it and attempt to be caught.
 */
class Hunter : Script {

    init {
        playerDespawn {
            val npcs = NPCs.filter { it["owner", ""] == accountName }
            for (npc in npcs) {
                NPCs.remove(npc)
            }
        }

        // Free-standing traps

        itemOption("Lay", "box_trap,rabbit_snare") {
            layTrap(it.item.id, null, null)
        }

        floorItemOperate("Lay") { (item) ->
            layTrap(item.id, null, item)
        }

        objectOperate("Dismantle", "box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            val type = when {
                target.id.startsWith("box_trap") -> "box_trap"
                target.id.startsWith("rabbit_snare") -> "rabbit_snare"
                target.id.startsWith("boulder_trap") -> "boulder_trap"
                target.id.contains("_net") -> "${target.id.substringBefore("_net")}_net"
                else -> return@objectOperate
            }
            var tile = target.tile
            if (type.endsWith("_net")) {
                tile = tile.add(target.direction())
            }
            dismantleTrap(type, target, tile)
        }

        objectOperate("Investigate", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            val id = Tables.npc("traps.${target.id}.npc")
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

        itemOnObjectOperate("unlit_torch", "box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") {
            message("I should light the torch before using it to smoke the trap.")
        }

        itemOnObjectOperate("torch_lit", "box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            val id = Tables.npc("traps.${target.id}.npc")
            val npc = NPCs.find(target.tile, id)
            if (npc["owner", ""] != accountName) {
                message("This is not your trap!") // TODO proper message
                return@itemOnObjectOperate
            }
            if (npc["smoked", false]) { // TODO what if baited?
                message("This trap is already smoked.") // TODO proper message
                return@itemOnObjectOperate
            }
            message("You use the smoke from the torch to remove your scent from the trap.", type = ChatType.Filter)
            anim("lay_trap_small")
            areaSound("hunting_smoke2", tile = target.tile, radius = 5)
            npc["smoked"] = true
        }
        // TODO bait + smoking traps

        itemOnObjectOperate("*", "net") { (obj, item) ->
            if (item.def.contains(Params.HEALS)) {
                // TODO what is/isn't allowed as bait
                message("I don't think I'd catch much using that as bait.")
            } else {
                noInterest()
            }
        }

        objectOperate("Check", "snare_*") { (target) ->
            collectCatch(target.id.removePrefix("snare_"), target)
        }

        objectOperate("Check", "box_trap_*") { (target) ->
            collectCatch(target.id.removePrefix("box_trap_"), target)
        }

        objectOperate("Check", "*_net_caught") { (target) ->
            collectCatch(target.id, target)
        }

        // Net

        objectOperate("Set-trap", "*_net,boulder_trap") { (target) ->
            layTrap(target.id, target, null)
        }

        objectOperate("Dismantle", "net") { (target) ->
            val dir = target.direction().inverse()
            val trap = GameObjects.getLayer(target.tile.add(dir), ObjectLayer.GROUND) ?: return@objectOperate
            dismantleTrap(trap.id.removeSuffix("_setup").removeSuffix("_failed"), trap, target.tile)
        }

        objectOperate("Dismantle", "*_net_failed") { (target) ->
            val dir = target.direction()
            dismantleTrap(target.id.removeSuffix("_setup").removeSuffix("_failed"), target, target.tile.add(dir))
        }

        objectOperate("Investigate", "net") { (target) ->
            val dir = target.direction().inverse()
            val trap = GameObjects.getLayer(target.tile.add(dir), ObjectLayer.GROUND) ?: return@objectOperate
            // TODO
        }

        // Pitfall

        objectOperate("Trap", "pitfall") {
            layTrap("pitfall", it.target, null)
        }

        objectOperate("Jump", "pitfall_*") { (target) ->
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

        objectOperate("Check", "rabbit_snare_caught") { (target) ->
            collectCatch("rabbit", target)
        }

        // TODO ferret, chinchompa, rabbits

        npcSpawn("hunting_*_trap_npc") {
            transform("${id}_off")
            revert(15)
        }

        npcDespawn("hunting_*_trap_npc") {
            val trapId = Tables.stringOrNull("trap_npcs.${id}.trap") ?: return@npcDespawn
            val message = Tables.string("traps.${trapId}.collapse_message") // Can probably be looked up by type?
            val player = owner()
            if (lifecycle == 0) { // Collapse
                player?.message(message)
                player?.collapse(this, drop = true)
            } else {
                player?.collapse(this, drop = player["logged_out", false])
            }
        }

        huntNPC("hunter_trap") { target ->
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
            val trapId = creature.string("trap")
            when (trapId) { // FIXME Temp
                "swamp_net", "red_net", "orange_net", "black_net" -> {
                    target.walkToDelay(tile)
                    target.delay(2)
                    // TODO need other tile
                    val net = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
                    val trap = GameObjects.getLayer(tile.add(net.direction().inverse()), ObjectLayer.GROUND) ?: return@huntNPC
                    net.remove()
                    if (success) {
                        val caught = trap.replace("${trapId}_catching")
                        delay(2)
                        caught.replace("${trapId}_caught")
                        return@huntNPC
                    }

                    // TODO need inverse
                    val failed = trap.replace("${trapId}_failing")
                    delay(2)
                    failed.replace("${trapId}_failed")
                    delay(1)
                    // TODO collapse
                }
            }
        }
    }

    private fun NPC.owner(): Player? {
        val account: String = this["owner"] ?: return null
        return Players.findByAccount(account)
    }

    private fun Player.collapse(npc: NPC, drop: Boolean) {
        dec("trap_count")
        val tile = npc.tile // TODO tile won't be correct for object traps
        val obj = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return
        obj.remove()
        if (drop) {
            val id = when (obj.id) {
                "snare_crimson_swift", "snare_golden_warbler", "bird_snare_fail" -> "bird_snare"
                else -> obj.id
            }
            dropTrapItems(id, tile) // TODO does baited drop bait?
        }
    }

    private fun Player.dropTrapItems(id: String, tile: Tile) {
        val items = Tables.itemList("traps.$id.items")
        for (item in items) {
            if (item == "logs") {
                continue
            }
            drop(tile, item)
        }
    }

    private suspend fun Player.layTrap(trapId: String, obj: GameObject?, floorItem: FloorItem?) {
        if (trapId == "box_trap" && !questCompleted("eagles_peak")) {
            message("You need to learn how to set a box trap in the Eagle's Peak Quest.")
            return
        }
        var obj = obj
        val trap = Rows.getOrNull("traps.$trapId") ?: return
        val level = levels.get(Skill.Hunter)
        if (!has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        if (Areas.get(tile.zone).any { it.tags.contains("bank") } || GameObjects.getLayer(tile, ObjectLayer.GROUND) != null) {
            message("You can't lay a trap here.", ChatType.Filter)
            return
        }
        val max = maxTraps(level, trap.int("max"))
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
        anim(trap.anim("setup_anim"))
        sound(trap.string("setup_sound"))
        delay(3)
        for (item in items) {
            if (floorItem != null && item == floorItem.id) {
                FloorItems.remove(floorItem)
                continue
            }
            inventory.remove(item)
        }
        inc("trap_count")
        val trapId = trap.obj("trap")
        if (trapId == "pitfall_0" && obj != null) {
            set(obj.id, "spiked")
        } else if (obj != null) {
            obj.replace(trapId)
            if (trapId.endsWith("_net_setup")) {
                val dir = obj.direction()
                NPCs.add(trap.npc("npc"), obj.tile.add(dir), ticks = 100, owner = this) // TODO check times
                GameObjects.add("net", obj.tile.add(dir), rotation = dir.ordinal / 2)
            }
        } else {
            NPCs.add(trap.npc("npc"), tile, ticks = 100, owner = this) // TODO check times
            obj = GameObjects.add(trapId, tile, ObjectShape.CENTRE_PIECE_STRAIGHT, 0, ticks = 50 * 60)
        }
        if (trap.bool("step_away")) {
            stepAway(obj)
        }
    }

    private suspend fun Player.dismantleTrap(trapId: String, target: GameObject, tile: Tile) {
        val id = Tables.npc("traps.${trapId}.npc")
        val npc = NPCs.findOrNull(tile, id) ?: return
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

    private suspend fun Player.collectCatch(creatureId: String, target: GameObject) {
        val id = Tables.npc("traps.${target.id.removeSuffix("_caught")}.npc")
        val npc = NPCs.findOrNull(target.tile, id) ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val creature = Rows.get("creatures.$creatureId")
        val loot = creature.itemList("loot")
        if (inventory.spaces < loot.size) {
            val slots = inventory.spaces - loot.size
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        message("You dismantle the trap.", ChatType.Filter)
        val trap = Rows.get("traps.${creature.string("trap")}")
        anim(trap.anim("take_down_anim"))
        sound("trap_dismantle", delay = 25)
        delay(2)
        removeTrap(target, npc)
        for (item in loot) {
            inventory.add(item)
        }
        exp(Skill.Hunter, creature.int("xp") / 10.0)
        message("You've caught a ${creatureId.toLowerSpaceCase()}.", ChatType.Filter)
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

    private fun maxTraps(level: Int, max: Int) = (1 + level / 20).coerceAtMost(max)

}
