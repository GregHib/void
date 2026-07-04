package content.skill.hunter

import content.entity.player.inv.item.drop
import content.quest.questCompleted
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
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
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.name
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
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Hunter : Script {

    private val traps: MutableMap<Int, String> = Int2ObjectOpenHashMap()
    private val baited: MutableMap<Int, Boolean> = Int2BooleanOpenHashMap()
    private val catching: MutableSet<Int> = IntOpenHashSet()

    private fun removeTrap(id: Int) {
        traps.remove(id)
        baited.remove(id)
        catching.remove(id)
    }

    init {
        playerDespawn {
            val name = name
            val ids = traps.filter { it.value == name }
            for (id in ids.keys) {
                collapse(id)
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
            val bait = baited[target.tile.id]
            if (bait == null) {
                message("Your scent lingers around this trap.") // TODO outfits?
            } else if (bait) {
                // TODO
            } else {
                message("The scent on this trap has been masked.")
            }
        }

        itemOnObjectOperate("unlit_torch", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") {
            message("I should light the torch before using it to smoke the trap.")
        }

        itemOnObjectOperate("torch_lit", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            if (traps[target.tile.id] != name) {
                message("This is not your trap!") // TODO proper message
                return@itemOnObjectOperate
            }
            if (baited.contains(target.tile.id)) {
                message("This trap is already smoked.") // TODO proper message
                return@itemOnObjectOperate
            }
            message("You use the smoke from the torch to remove your scent from the trap.", type = ChatType.Filter)
            anim("lay_trap_small")
            areaSound("hunting_smoke2", tile = target.tile, radius = 5)
            baited[target.tile.id] = false
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

        // TODO can birds fly over water?

        huntObject("hunter_trap") { trap ->
            // TODO unknown chance at attempt
            if (random.nextInt(4) != 0) {
                return@huntObject
            }
            val name = traps[trap.tile.id] ?: return@huntObject
            val owner = Players.find(name) ?: return@huntObject
            val creature = Rows.getOrNull("creatures.$id") ?: return@huntObject
            if (creature.string("trap") != "bird_snare") {
                return@huntObject
            }
            if (!owner.has(Skill.Hunter, creature.int("level"))) {
                return@huntObject
            }
            if (!catching.add(trap.tile.id)) {
                return@huntObject
            }
            walkOverDelay(trap.tile)
            anim("bird_land")
            face(Direction.SOUTH)
            delay(1)
            var chance = creature.intRange("chance")
            val bait = baited[tile.id]
            if (bait == true) {
                chance = (chance.first + 7)..(chance.last + 7) // 3%
            } else if (bait == false) { // smoked
                chance = (chance.first + 5)..(chance.last + 5) // 2%
            }
            val success = Level.success(owner.levels.get(Skill.Hunter), chance)
            val catchAnim = creature.animOrNull(if (success) "catch_anim" else "fail_anim")
            if (catchAnim != null) {
                anim(catchAnim)
            }
            delay(1)
            val index = get("trap_tile_${trap.tile.id}", 0)
            softTimers.stop("trap_collapse_$index")
            softTimers.start("trap_collapse_$index")
            if (success) {
                levels.set(Skill.Constitution, 0)
                trap.replace(Tables.obj("creatures.$id.caught_obj"))
                owner.message("Something has been caught in your trap!")
                areaSound("bird_caught", trap.tile)
                return@huntObject
            }
            val failObj = Tables.objOrNull("traps.${creature.string("trap")}.fail")
            if (failObj != null) {
                trap.replace(failObj, ticks = 0) // TODO collapse time
            } else {
                owner.dropTrapItems(creature.string("trap"), trap.tile)
                trap.remove()
                removeTrap(trap.tile.id)
            }
            val failAnim = creature.animOrNull("fail_anim")
            if (failAnim != null) {
                anim(failAnim)
            }
            // TODO is there a message for this?
//            owner.message(Tables.stringOrNull("traps.${creature.string("trap")}.collapse_message") ?: return@huntObject)
        }


        timerStart("trap_collapse_0") { get("trap_ticks_0", 0) }
        timerStart("trap_collapse_1") { get("trap_ticks_1", 0) }
        timerStart("trap_collapse_2") { get("trap_ticks_2", 0) }
        timerStart("trap_collapse_3") { get("trap_ticks_3", 0) }
        timerStart("trap_collapse_4") { get("trap_ticks_4", 0) }

        timerTick("trap_collapse_0") { collapseTrap(0); Timer.CANCEL }
        timerTick("trap_collapse_1") { collapseTrap(1); Timer.CANCEL }
        timerTick("trap_collapse_2") { collapseTrap(2); Timer.CANCEL }
        timerTick("trap_collapse_3") { collapseTrap(3); Timer.CANCEL }
        timerTick("trap_collapse_4") { collapseTrap(4); Timer.CANCEL }
    }

    private fun Player.collapseTimer(index: Int, tile: Tile, ticks: Int, message: String) {
        set("trap_tile_${tile.id}", index)
        set("trap_$index", tile.id)
        set("trap_ticks_$index", ticks)
        set("trap_message_$index", message)
        softTimers.start("trap_collapse_$index")
    }

    private fun Player.collapseTrap(index: Int) {
        val tile = remove<Int>("trap_$index") ?: return
        clear("trap_tile_$tile")
        clear("trap_ticks_$index")
        collapse(tile)
        message(remove("trap_message_$index") ?: return)
    }


    private fun Player.collapse(id: Int) {
        removeTrap(id)
        val tile = Tile(id) // TODO tile won't be correct for object traps
        val obj = GameObjects.getLayer(tile, 2) ?: return
        obj.remove()
        val id = when (obj.id) {
            "snare_crimson_swift", "snare_golden_warbler", "bird_snare_fail" -> "bird_snare"
            else -> obj.id
        }
        dropTrapItems(id, tile) // TODO does baited drop bait?
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
        val trapCount = traps.count { it.value == name }
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
        val trapId = trap.obj("trap")
        if (trapId == "pitfall_0" && obj != null) {
            traps[obj.tile.id] = name
            set(obj.id, "spiked")
        } else if (obj != null) {
            traps[obj.tile.id] = name
            obj.replace(trapId)
            if (trapId.endsWith("_net_setup")) {
                val dir = direction(obj.rotation)
                GameObjects.add("net", obj.tile.add(dir), rotation = dir.ordinal / 2)
            }
        } else {
            traps[tile.id] = name
            obj = GameObjects.add(trapId, tile, ObjectShape.CENTRE_PIECE_STRAIGHT, 0, ticks = 50 * 60)
        }
        collapseTimer(trapCount, obj.tile, 100, trap.string("collapse_message")) // TODO check times
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
        if (traps[target.tile.id] != name) {
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
        if (traps[target.tile.id] != name) {
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
        removeTrap(target.tile.id)
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
