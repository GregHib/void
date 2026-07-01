package content.skill.hunter

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Hunter : Script {

    init {
        for (trap in Tables.get("traps").rows()) {
            val items = trap.itemList("items")
            val primary = items.firstOrNull() ?: continue
            itemOption("Lay", primary) { // TODO no all traps use Lay - nets and boulder_traps are itemOnObject
                layTrap(trap.rowId)
            }
        }

        for (trap in Tables.get("traps").rows()) {
            val activeObj = Tables.obj("traps.${trap.rowId}.trap")
            objectOperate("Dismantle", activeObj) { (target) -> dismantleTrap(trap.rowId, target) }

            objectOperate("Investigate", activeObj) { (target) ->
                // TODO description
            }
        }

        for (creature in Tables.get("creatures").rows()) {
            val caughtObj = Tables.obj("creatures.${creature.rowId}.caught_obj")
            objectOperate("Check", caughtObj) { (target) -> collectCatch(creature.rowId, target) }
        }

        huntObject("hunter_trap") { trapObj ->
            if (get<Int>("hunter_trap_tile") != null) {
                return@huntObject
            }
            set("hunter_trap_tile", trapObj.tile.id)
            set("hunter_trap_obj", trapObj.id)
            softTimers.start("hunter_capture")
        }

        npcTimerStart("hunter_capture") { _ -> 7 }

        npcTimerTick("hunter_capture") {
            val trapTileId = get<Int>("hunter_trap_tile") ?: return@npcTimerTick Timer.CANCEL
            val trapObjId = get<String>("hunter_trap_obj") ?: return@npcTimerTick Timer.CANCEL
            val trapObj = GameObjects.findOrNull(Tile(trapTileId), trapObjId)
            val owner = if (trapObj != null) findOwner(trapTileId) else null

            if (trapObj == null || owner == null) {
                clearTrapTarget()
                return@npcTimerTick Timer.CANCEL
            }

            val creature = Tables.get("creatures").rows().firstOrNull { it.rowId == id }
                ?: return@npcTimerTick Timer.CANCEL
            val success = random.nextInt(100) < catchChance(owner.levels.get(Skill.Hunter), creature.int("level"))

            val catchAnim = creature.animOrNull("catch_anim")
            if (catchAnim != null) {
                anim(catchAnim)
            }

            if (success) {
                GameObjects.replace(trapObj, Tables.obj("creatures.$id.caught_obj"))
                owner.message("Something has been caught in your trap!")
            } else {
                val failObj = Tables.objOrNull("traps.${creature.string("trap")}.fail")
                if (failObj != null) {
                    GameObjects.replace(trapObj, failObj)
                } else {
                    GameObjects.remove(trapObj)
                    owner.clearTrapSlot(trapTileId)
                }
                val failAnim = creature.animOrNull("fail_anim")
                if (failAnim != null) {
                    anim(failAnim)
                }
                owner.message("Your trap has been disturbed.")
            }

            clearTrapTarget()
            Timer.CANCEL
        }

        npcOperate("Catch", "ruby_harvest,sapphire_glacialis,snowy_knight,black_warlock") { (target) ->
            catchButterfly(target.id)
        }

        npcOperate("Catch", "*_impling") { (target) ->
            catchImpling(target.id, withNet = false)
        }

        npcOperate("Catch with net", "*_impling") { (target) ->
            catchImpling(target.id, withNet = true)
        }
    }

    private suspend fun Player.layTrap(trapId: String) {
        val trap = Tables.get("traps").rows().first { it.rowId == trapId }
        val items = trap.itemList("items")
        val level = levels.get(Skill.Hunter)

        if (!has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        if (trapCount() >= maxTraps(level)) {
            message("You cannot place more than ${maxTraps(level)} traps at once.")
            return
        }
        for (item in items) {
            if (!carriesItem(item)) {
                message("You need ${item.toLowerSpaceCase()} to lay this trap.")
                return
            }
        }

        arriveDelay()
        anim(trap.anim("setup_anim"))
        delay(3)

        for (item in items) {
            inventory.remove(item)
        }
        GameObjects.add(Tables.obj("traps.$trapId.trap"), tile, ObjectShape.CENTRE_PIECE_STRAIGHT, 0, ticks = 50 * 60)
        setTrapSlot(tile.id)
        message("You set up the ${trapId.toLowerSpaceCase()}.")
    }

    private suspend fun Player.dismantleTrap(trapId: String, target: GameObject) {
        if (!hasTrapSlot(target.tile.id)) {
            message("This is not your trap.")
            return
        }
        val trap = Tables.get("traps").rows().first { it.rowId == trapId }
        anim(trap.anim("take_down_anim"))
        delay(2)
        GameObjects.remove(target)
        clearTrapSlot(target.tile.id)
        for (item in trap.itemList("items")) {
            inventory.add(item)
        }
        message("You dismantle the trap and retrieve your equipment.")
    }

    private suspend fun Player.collectCatch(creatureId: String, target: GameObject) {
        if (!hasTrapSlot(target.tile.id)) {
            message("This is not your trap.")
            return
        }
        val creature = Tables.get("creatures").rows().first { it.rowId == creatureId }
        val trap = Tables.get("traps").rows().first { it.rowId == creature.string("trap") }
        anim(trap.anim("take_down_anim"))
        delay(2)
        GameObjects.remove(target)
        clearTrapSlot(target.tile.id)
        for (item in trap.itemList("items")) {
            inventory.add(item)
        }
        for (item in creature.itemList("loot")) {
            inventory.add(item)
        }
        exp(Skill.Hunter, creature.int("xp") / 10.0)
        message("You've caught a ${creatureId.toLowerSpaceCase()}!")
    }

    private suspend fun Player.catchButterfly(npcId: String) {
        val row = Tables.get("butterflies").rows().firstOrNull { it.rowId == npcId } ?: return
        if (!has(Skill.Hunter, row.int("level"), message = true)) {
            return
        }
        if (!carriesItem("butterfly_net")) {
            message("You need a butterfly net to catch butterflies.")
            return
        }
        if (!carriesItem("butterfly_jar")) {
            message("You need an empty butterfly jar.")
            return
        }
        anim("butterfly_catch")
        delay(2)
        inventory.remove("butterfly_jar")
        inventory.add(row.item("jar"))
        exp(Skill.Hunter, row.int("xp") / 10.0)
        message("You catch the ${npcId.toLowerSpaceCase()} and place it in a jar.")
    }

    private suspend fun Player.catchImpling(npcId: String, withNet: Boolean) {
        val row = Tables.get("implings").rows().firstOrNull { it.rowId == npcId } ?: return
        val level = if (withNet) (row.int("level") - 10).coerceAtLeast(1) else row.int("level")
        if (!has(Skill.Hunter, level, message = true)) {
            return
        }
        if (withNet && !carriesItem("butterfly_net")) {
            message("You need a butterfly net.")
            return
        }
        if (!carriesItem("butterfly_jar")) {
            message("You need an empty jar to catch an impling.")
            return
        }
        anim("butterfly_catch")
        delay(2)
        inventory.remove("butterfly_jar")
        inventory.add(row.item("jar"))
        exp(Skill.Hunter, row.int("xp") / 10.0)
        message("You catch the ${npcId.toLowerSpaceCase()}!")
    }

    private fun findOwner(trapTileId: Int): Player? =
        Players.firstOrNull { it.hasTrapSlot(trapTileId) }

    private fun maxTraps(level: Int) = (1 + level / 20).coerceAtMost(5)

    private fun catchChance(hunterLevel: Int, lureLevel: Int) =
        ((hunterLevel.toDouble() / lureLevel) * 50).toInt().coerceIn(10, 90)
}

private val trapSlots = (1..5).map { "hunter_trap_$it" }

fun Player.trapCount(): Int = trapSlots.count { get<Int>(it, 0) != 0 }

fun Player.hasTrapSlot(tileId: Int): Boolean = trapSlots.any { get<Int>(it, 0) == tileId }

fun Player.setTrapSlot(tileId: Int) {
    val slot = trapSlots.firstOrNull { get<Int>(it, 0) == 0 } ?: return
    set(slot, tileId)
}

fun Player.clearTrapSlot(tileId: Int) {
    val slot = trapSlots.firstOrNull { get<Int>(it, 0) == tileId } ?: return
    set(slot, 0)
}

fun NPC.clearTrapTarget() {
    clear("hunter_trap_tile")
    clear("hunter_trap_obj")
}
