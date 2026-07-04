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

        itemOption("Lay", "bird_snare,box_trap,rabbit_snare") {
            if (it.item.id == "box_trap" && !questCompleted("eagles_peak")) {
                message("You need to learn how to set a box trap in the Eagle's Peak Quest.")
                return@itemOption
            }
            layTrap(it.item.id, null, null)
        }

        floorItemOperate("Lay") { (item) ->
            if (item.id == "box_trap" && !questCompleted("eagles_peak")) {
                message("You need to learn how to set a box trap in the Eagle's Peak Quest.")
                return@floorItemOperate
            }
            layTrap(item.id, null, item)
        }

        objectOperate("Dismantle", "bird_snare,bird_snare_fail,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            dismantleTrap(target.id, target)
        }

        objectOperate("Investigate", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            val npc = NPCs.find(target.tile, "hunting_ojibway_trap_npc")
            if (npc["baited", false]) {
                // TODO
            } else if (npc["smoked", false]) {
                message("The scent on this trap has been masked.")
            } else {
                message("Your scent lingers around this trap.") // TODO outfits?
            }
        }

        itemOnObjectOperate("unlit_torch", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") {
            message("I should light the torch before using it to smoke the trap.")
        }

        itemOnObjectOperate("torch_lit", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            val npc = NPCs.find(target.tile, "hunting_ojibway_trap_npc")
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

        itemOnObjectOperate("*", "bird_snare") { (obj, item) ->
            if (item.def.contains(Params.HEALS)) {
                message("There isn't really anywhere to put any bait on this trap.")
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

        // Net

        objectOperate("Set-trap", "*_net,boulder_trap") {
            layTrap(it.target.id, it.target, null)
        }

        objectOperate("Dismantle", "net") { (target) ->
            val dir = direction(target.rotation).inverse()
            val trap = GameObjects.findOrNull(target.tile.add(dir)) { it.id.endsWith("_net_setup") } ?: return@objectOperate
            dismantleTrap(trap.id, trap)
        }

        objectOperate("Investigate", "net") { (target) ->
            val dir = direction(target.rotation).inverse()
            val trap = GameObjects.findOrNull(target.tile.add(dir)) { it.id.endsWith("_net_setup") } ?: return@objectOperate
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
            dismantleTrap("pitfall", target)
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
            if (creature.string("trap") != "bird_snare") { // FIXME Temp
                return@huntNPC
            }
            val account: String = get("owner") ?: return@huntNPC
            val player = Players.findByAccount(account) ?: return@huntNPC
            if (!player.has(Skill.Hunter, creature.int("level"))) {
                return@huntNPC
            }
            if (tile.distanceTo(target.tile) > 2) {
                return@huntNPC
            }
            transform("${id}_off")
            target.walkToDelay(tile)
            target.walkOverDelay(tile)
            target.face(Direction.SOUTH)
            target.anim("bird_land")
            target.delay(1)
            var chance = creature.intRange("chance")
            if (get("baited", false)) {
                chance = (chance.first + 7)..(chance.last + 7) // 3%
            } else if (get("smoked", false)) {
                chance = (chance.first + 5)..(chance.last + 5) // 2%
            }
            val success = Level.success(player.levels.get(Skill.Hunter), chance)
            val catchAnim = creature.animOrNull(if (success) "catch_anim" else "fail_anim")
            if (catchAnim != null) {
                target.anim(catchAnim)
            }
            target.delay(1)
            despawn(100) // TODO timings
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            if (success) {
                target.levels.set(Skill.Constitution, 0)
                trap.replace(Tables.obj("creatures.${target.id}.caught_obj"))
                player.message("Something has been caught in your trap!")
                areaSound("bird_caught", tile)
                return@huntNPC
            }
            val failObj = Tables.objOrNull("traps.${creature.string("trap")}.fail")
            if (failObj != null) {
                trap.replace(failObj) // TODO collapse time
            }
            val failAnim = creature.animOrNull("fail_anim")
            if (failAnim != null) {
                target.anim(failAnim)
            }
            // TODO is there a message for this?
//            owner.message(Tables.stringOrNull("traps.${creature.string("trap")}.collapse_message") ?: return@huntNPC)
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
        NPCs.add("hunting_ojibway_trap_npc", tile, ticks = 100, owner = this) // TODO check times
        val trapId = trap.obj("trap")
        if (trapId == "pitfall_0" && obj != null) {
            set(obj.id, "spiked")
        } else if (obj != null) {
            obj.replace(trapId)
            if (trapId.endsWith("_net_setup")) {
                val dir = direction(obj.rotation)
                GameObjects.add("net", obj.tile.add(dir), rotation = dir.ordinal / 2)
            }
        } else {
            obj = GameObjects.add(trapId, tile, ObjectShape.CENTRE_PIECE_STRAIGHT, 0, ticks = 50 * 60)
        }
        if (trap.bool("step_away")) {
            stepAway(obj)
        }
    }

    private val directions = listOf(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH)

    private fun Player.stepAway(obj: GameObject?) {
        val steps: StepValidator = get()
        for (dir in directions) {
            if (steps.canTravel(this, dir.delta.x, dir.delta.y)) {
                walkTo(tile.add(dir), noCollision = true)
                break
            }
        }
        if (obj != null) {
            set("face_entity", obj)
        }
    }

    private suspend fun Player.dismantleTrap(trapId: String, target: GameObject) {
        val npc = NPCs.findOrNull(target.tile, "hunting_ojibway_trap_npc") ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val trap = Rows.get("traps.${trapId.removeSuffix("_setup").removeSuffix("_fail")}")
        anim(trap.anim("take_down_anim"))
        sound("trap_dismantle", delay = 25)
        delay(2)
        removeTrap(target)
        for (item in trap.itemList("items")) {
            inventory.add(item)
        }
        message("You dismantle the trap.", ChatType.Filter)
    }

    private suspend fun Player.collectCatch(creatureId: String, target: GameObject) {
        val npc = NPCs.findOrNull(target.tile, "hunting_ojibway_trap_npc") ?: return
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
        removeTrap(target)
        for (item in loot) {
            inventory.add(item)
        }
        exp(Skill.Hunter, creature.int("xp") / 10.0)
        message("You've caught a ${creatureId.toLowerSpaceCase()}!", ChatType.Filter)
    }

    private fun Player.removeTrap(target: GameObject) {
        dec("trap_count")
        val npc = NPCs.find(target.tile, "hunting_ojibway_trap_npc")
        NPCs.remove(npc)
        if (target.id.startsWith("pitfall")) {
            set(target.id, "empty")
            return
        }
        if (target.id.endsWith("_net_setup")) {
            val dir = direction(target.rotation)
            val net = GameObjects.findOrNull(target.tile.add(dir), "net")
            net?.remove()
        }
        GameObjects.remove(target)
    }

    private fun direction(rotation: Int): Direction = when (rotation) {
        0 -> Direction.NORTH
        1 -> Direction.EAST
        2 -> Direction.SOUTH
        else -> Direction.WEST
    }

    private fun maxTraps(level: Int, max: Int) = (1 + level / 20).coerceAtMost(max)

}
