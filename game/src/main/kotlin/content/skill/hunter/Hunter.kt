package content.skill.hunter

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Hunter : Script {

    private val traps: MutableMap<Int, String> = Int2ObjectOpenHashMap()

    init {
        itemOption("Lay", "bird_snare,box_trap,rabbit_snare") {
            layTrap(it.item.id)
        }

        itemOption("Set-trap", "young_tree") {
            layTrap(it.item.id)
        }

        objectOperate("Dismantle", "snare_trap,box_trap,rabbit_snare") { (target) ->
            dismantleTrap(target.id, target)
        }

        objectOperate("Investigate", "snare_trap,box_trap,rabbit_snare") { (target) ->
            // TODO
        }

        // TODO bait + smoking traps

        objectOperate("Check", "snare_*") { (target) ->
            collectCatch(target.id.removePrefix("snare_"), target)
        }

        objectOperate("Check", "box_trap_*") { (target) ->
            collectCatch(target.id.removePrefix("box_trap_"), target)
        }

//        objectOperate("Check", "rabbit_snare_*") { (target) ->
//            collectCatch(target.id.removePrefix("rabbit_snare_"), target)
//        }

        // TODO ferret, chinchompa, rabbits

        huntObject("hunter_trap") { trapObj ->
            // TODO chance at attempt
            val owner = Players.find(traps[trapObj.tile.id] ?: "") ?: return@huntObject
            val creature = Rows.getOrNull("creatures.${id}") ?: return@huntObject
            val catchAnim = creature.animOrNull("catch_anim")
            if (catchAnim != null) {
                anim(catchAnim)
            }
            if (Level.success(owner.levels.get(Skill.Hunter), 1..1)) { // TODO proper chances
                GameObjects.replace(trapObj, Tables.obj("creatures.$id.caught_obj"))
                owner.message("Something has been caught in your trap!")
                return@huntObject
            }
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
            owner.message("Your trap has been disturbed.")
        }

    }

    private suspend fun Player.layTrap(trapId: String) {
        val trap = Tables.rowOrNull("traps.${trapId}") ?: return
        val items = trap.itemList("items")
        val level = levels.get(Skill.Hunter)

        if (!has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        if (traps.count { it.value == name } >= maxTraps(level)) {
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
        traps[tile.id] = name
        message("You set up the ${trapId.toLowerSpaceCase()}.")
    }

    private suspend fun Player.dismantleTrap(trapId: String, target: GameObject) {
        if (traps[target.tile.id] != name) {
            message("This is not your trap.")
            return
        }
        val trap = Tables.get("traps").rows().first { it.rowId == trapId }
        anim(trap.anim("take_down_anim"))
        delay(2)
        GameObjects.remove(target)
        traps.remove(target.tile.id)
        for (item in trap.itemList("items")) {
            inventory.add(item)
        }
        message("You dismantle the trap and retrieve your equipment.")
    }

    private suspend fun Player.collectCatch(creatureId: String, target: GameObject) {
        if (traps[target.tile.id] != name) {
            message("This is not your trap.")
            return
        }
        val creature = Tables.get("creatures").rows().first { it.rowId == creatureId }
        val trap = Tables.get("traps").rows().first { it.rowId == creature.string("trap") }
        anim(trap.anim("take_down_anim"))
        delay(2)
        GameObjects.remove(target)
        traps.remove(target.tile.id)
        for (item in trap.itemList("items")) {
            inventory.add(item)
        }
        for (item in creature.itemList("loot")) {
            inventory.add(item)
        }
        exp(Skill.Hunter, creature.int("xp") / 10.0)
        message("You've caught a ${creatureId.toLowerSpaceCase()}!")
    }

    private fun maxTraps(level: Int) = (1 + level / 20).coerceAtMost(5)

    private fun catchChance(hunterLevel: Int, lureLevel: Int) =
        ((hunterLevel.toDouble() / lureLevel) * 50).toInt().coerceIn(10, 90)
}
