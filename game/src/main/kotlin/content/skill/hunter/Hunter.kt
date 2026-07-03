package content.skill.hunter

import content.quest.questCompleted
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.pearx.kasechange.toLowerSpaceCase
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.move.canTravel
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Direction

class Hunter : Script {

    private val traps: MutableMap<Int, String> = Int2ObjectOpenHashMap()

    init {
        // TODO remove pitfall traps on logout
        // message("You release the salamander and it darts away.", ChatType.Filter)

        // Free-standing traps

        itemOption("Lay", "bird_snare,box_trap,rabbit_snare") {
            if (it.item.id == "box_trap" && !questCompleted("eagles_peak")) {
                message("You need to learn how to set a box trap in the Eagle's Peak Quest.")
                return@itemOption
            }
            layTrap(it.item.id, null)
        }

        objectOperate("Dismantle", "bird_snare,bird_snare_fail,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            dismantleTrap(target.id, target)
        }

        objectOperate("Investigate", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            // TODO baited + wearing outfits
            message("Your scent lingers around this trap.")
            message("The scent on this trap has been masked.")
        }

        itemOnObjectOperate("unlit_torch", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") {
            message("I should light the torch before using it to smoke the trap.")
        }

        itemOnObjectOperate("torch_lit", "bird_snare,box_trap,rabbit_snare,*_net_setup,boulder_trap_setup") { (target) ->
            message("You use the smoke from the torch to remove your scent from the trap.", type = ChatType.Filter)
            anim("lay_trap_small")
            areaSound("hunting_smoke2", tile = target.tile, radius = 5)
        }

        itemOnObjectOperate("cooked_meat", "bird_snare") {
            message("There isn't really anywhere to put any bait on this trap.")
        }

        objectOperate("Check", "snare_*") { (target) ->
            collectCatch(target.id.removePrefix("snare_"), target)
        }

        objectOperate("Check", "box_trap_*") { (target) ->
            collectCatch(target.id.removePrefix("box_trap_"), target)
        }
        // TODO bait + smoking traps

        // Net

        objectOperate("Set-trap", "*_net,boulder_trap") {
            layTrap(it.target.id, it.target)
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
            layTrap("pitfall", it.target)
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

        huntObject("hunter_trap") { trapObj ->
            println("Hunt $trapObj")
            walkTo(trapObj.tile)
            // TODO chance at attempt
        }

        npcMoved("crimson_swift") {
            val name = traps[tile.id] ?: return@npcMoved
            val trapObj = GameObjects.getLayer(tile, ObjectShape.CENTRE_PIECE_STRAIGHT) ?: return@npcMoved
            val owner = Players.find(name) ?: return@npcMoved
            val creature = Rows.getOrNull("creatures.$id") ?: return@npcMoved
            exactMove(trapObj.tile, direction = Direction.SOUTH)
            val npc = this
            // TODO allow delays in move
            queue("land") {
                anim("bird_land")
                npc.face(Direction.SOUTH)
                queue("catch", 4) {
                    val catchAnim = creature.animOrNull("catch_anim")
                    if (catchAnim != null) {
                        anim(catchAnim)
                    }
                    queue("caught", 2) {
                        // TODO 2/3% chance improvement if smoke/baited
                        val success = true // Level.success(owner.levels.get(Skill.Hunter), 1..1)
                        if (success) { // TODO proper chances
                            trapObj.replace(Tables.obj("creatures.$id.caught_obj"))
                            owner.message("Something has been caught in your trap!")
                            return@queue
                        }
                        npc.hide = true
                        npc.levels.set(Skill.Constitution, 0)
                        val failObj = Tables.objOrNull("traps.${creature.string("trap")}.fail")
                        if (failObj != null) {
                            trapObj.replace(failObj, ticks = 0) // TODO collapse time
                        } else {
                            // TODO drop floor item
                            trapObj.remove()
                            traps.remove(trapObj.tile.id)
                        }
                        val failAnim = creature.animOrNull("fail_anim")
                        if (failAnim != null) {
                            anim(failAnim)
                        }
                        owner.message("Your trap has collapsed.")
                    }
                }
            }
        }
    }

    private suspend fun Player.layTrap(trapId: String, obj: GameObject?) {
        var obj = obj
        val trap = Rows.getOrNull("traps.$trapId") ?: return
        val level = levels.get(Skill.Hunter)
        if (!has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        if (Areas.get(tile.zone).any { it.tags.contains("bank") }) {
            message("You can't lay a trap here.", ChatType.Filter)
            return
        }
        val max = maxTraps(level, trap.int("max"))
        if (traps.count { it.value == name } >= max) {
            message("You cannot place more than $max ${"trap".plural(max)} at once.")
            return
        }
        val message = trap.stringOrNull("item_message")
        val requires = trap.itemList("requires")
        for (item in requires) {
            if (!carriesItem(item)) {
                message(message ?: "You need ${item.toLowerSpaceCase()} to lay this trap.")
                return
            }
        }
        val items = trap.itemList("items")
        for (item in items) {
            if (!carriesItem(item)) {
                message(message ?: "You need ${item.toLowerSpaceCase()} to lay this trap.")
                return
            }
        }

        arriveDelay()
        anim(trap.anim("setup_anim"))
        delay(3)

        for (item in items) {
            inventory.remove(item)
        }
        val trapId = Tables.obj("traps.${trapId.removeSuffix("_setup")}.trap")
        if (trapId == "pitfall_0" && obj != null) {
            traps[obj.tile.id] = name
            set(obj.id, "spiked")
            // TODO collapse timer
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
        message("You begin setting up ${if (max == 1) "the" else "a"} trap.", ChatType.Filter)
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
        val trap = Rows.get("traps.${creature.string("trap")}")
        anim(trap.anim("take_down_anim"))
        delay(2)
        removeTrap(target)
        val loot = creature.itemList("loot")
        if (inventory.spaces < loot.size) {
            val slots = inventory.spaces - loot.size
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        for (item in loot) {
            inventory.add(item)
        }
        exp(Skill.Hunter, creature.int("xp") / 10.0)
        message("You've caught a ${creatureId.toLowerSpaceCase()}!")
    }

    private fun Player.removeTrap(target: GameObject) {
        traps.remove(target.tile.id)
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

    private fun catchChance(hunterLevel: Int, lureLevel: Int) = ((hunterLevel.toDouble() / lureLevel) * 50).toInt().coerceIn(10, 90)
}
