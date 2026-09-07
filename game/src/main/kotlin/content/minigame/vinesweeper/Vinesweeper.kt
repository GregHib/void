package content.minigame.vinesweeper

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Vinesweeper: a farming take on Minesweeper at Winkin's Farm.
 * Dig holes with a spade to reveal how many seeds are hidden in the surrounding holes,
 * then flag the holes you believe hide seeds for the gnome farmers to check.
 * https://runescape.wiki/w/Vinesweeper?oldid=3978225
 */
class Vinesweeper : Script {

    init {
        worldSpawn {
            VinesweeperField.reset()
            VinesweeperField.populate()
        }

        entered("vinesweeper_field") {
            open("vinesweeper_points")
        }

        exited("vinesweeper_field") {
            close("vinesweeper_points")
            refundOgleroots()
        }

        objectOperate("Dig", "vinesweeper_hole*") { (target) ->
            dig(target)
        }

        itemOnObjectOperate("spade", "vinesweeper_hole*") { (target) ->
            dig(target)
        }

        objectOperate("Inspect", "vinesweeper_hole*") { (target) ->
            inspect(target)
        }

        objectOperate("Flag", "vinesweeper_hole*") { (target) ->
            plantFlag(target)
        }

        itemOnObjectOperate("flag", "vinesweeper_hole*") { (target) ->
            plantFlag(target)
        }

        objectOperate("Enter", "vinesweeper_portal") {
            tele(Tile(get("vinesweeper_return_tile", RETURN_TILE.id)))
        }

        objectOperate("Read", "vinesweeper_sign_*") { (target) ->
            open("vinesweeper_info_${target.id.removePrefix("vinesweeper_sign_")}")
        }

        interfaceOption("Close", "vinesweeper_info_*:close") {
            close(it.id)
        }

        interfaceOption("Close", "vinesweeper_instructions:close") {
            close("vinesweeper_instructions")
        }

        npcOperate("Feed", "rabbit_minesweeper*") { (target) ->
            feedRabbit(target)
        }

        itemOnNPCOperate("ogleroot", "rabbit_minesweeper*") { (target) ->
            feedRabbit(target)
        }
    }

    private suspend fun Player.dig(hole: GameObject) {
        if (!inventory.contains("spade")) {
            message("You need a spade to dig here.")
            return
        }
        face(hole)
        anim("vinesweeper_dig")
        gfx("vinesweeper_dirt")
        delay(2)
        val tile = hole.tile
        if (VinesweeperField.hole(tile) == null) {
            return
        }
        if (VinesweeperField.isSeed(tile)) {
            VinesweeperField.remove(tile)
            hole.replace("vinesweeper_dead_plant")
            addPoints(-SEED_PENALTY)
            message("Oh dear! It looks like you dug up a potato seed by mistake.")
            VinesweeperNPCs.dispatchFarmer(tile)
            return
        }
        val count = uncover(hole)
        if (random.nextInt(OGLEROOT_CHANCE) == 0 && inventory.add("ogleroot")) {
            message("You find an ogleroot buried in the hole.")
        }
        if (count != 0) {
            return
        }
        for (x in -REVEAL_RADIUS..REVEAL_RADIUS) {
            for (y in -REVEAL_RADIUS..REVEAL_RADIUS) {
                val nearby = tile.add(x, y)
                if (VinesweeperField.isSeed(nearby)) {
                    continue
                }
                val other = VinesweeperField.hole(nearby) ?: continue
                uncover(other)
            }
        }
    }

    /**
     * Turn [hole] into the number of seeds hidden around it, rewarding a point for the discovery.
     */
    private fun Player.uncover(hole: GameObject): Int {
        val count = VinesweeperField.adjacentSeeds(hole.tile)
        hole.replace("vinesweeper_number_$count")
        addPoints(1)
        return count
    }

    private suspend fun Player.inspect(hole: GameObject) {
        face(hole)
        anim("vinesweeper_inspect")
        delay(5)
        val message = when (random.nextInt(7)) {
            0 -> "You inspect the hole but can't tell whether anything is planted here."
            1 -> "The hole looks much the same as every other hole in this field."
            2 -> "You poke around in the dirt but learn nothing."
            3 -> "It's a hole. Some dirt has been dug out of it."
            else -> if (VinesweeperField.isSeed(hole.tile)) {
                "You notice a seed hidden in the dirt."
            } else {
                "You are certain there is no seed planted here."
            }
        }
        message(message)
    }

    private suspend fun Player.plantFlag(hole: GameObject) {
        if (!inventory.contains("flag")) {
            message("You need a flag to mark this hole.")
            return
        }
        val tile = hole.tile
        if (VinesweeperField.hole(tile) == null) {
            return
        }
        face(hole)
        anim("vinesweeper_plant_flag")
        gfx("flag")
        delay(2)
        if (VinesweeperField.hole(tile) == null || !inventory.remove("flag")) {
            return
        }
        hole.replace("vinesweeper_flag")
        VinesweeperField.flag(tile, accountName)
        VinesweeperNPCs.dispatchFlag(tile)
    }

    private suspend fun Player.feedRabbit(rabbit: NPC) {
        if (rabbit.hide) {
            return
        }
        if (!inventory.remove("ogleroot")) {
            message("You need an ogleroot to feed the rabbit.")
            return
        }
        face(rabbit)
        rabbit.face(this)
        rabbit.say("Squeak!")
        rabbit.anim("vinesweeper_rabbit_eat_ogleroot")
        exp(Skill.Hunter, OGLEROOT_XP)
        message("The rabbit gobbles the ogleroot and shrinks away into the ground.")
        delay(3)
        rabbit.queue.clear(VinesweeperNPCs.RACE_QUEUE)
        rabbit.respawn(RABBIT_RESPAWN_TICKS)
    }

    private fun Player.refundOgleroots() {
        val count = inventory.count("ogleroot")
        if (count == 0) {
            return
        }
        inventory.transaction {
            remove("ogleroot", count)
            add("coins", count * OGLEROOT_PRICE)
        }
        if (inventory.transaction.error == TransactionError.None) {
            message("Farmer Blinkin refunds you for your spare ogleroots.")
        }
    }

    companion object {
        const val OGLEROOT_PRICE = 10
        const val OGLEROOT_XP = 30.0
        const val MAX_FLAGS = 10
        const val FLAG_PRICE = 500
        const val SPADE_PRICE = 5
        val ARRIVAL_TILE = Tile(1637, 4709)
        private val RETURN_TILE = Tile(3052, 3304)
        private const val SEED_PENALTY = 10
        private const val REVEAL_RADIUS = 2
        private const val OGLEROOT_CHANCE = 20
        private const val RABBIT_RESPAWN_TICKS = 50
        private const val MAX_POINTS = (1 shl 26) - 1

        fun Player.addPoints(amount: Int) {
            val points = (get("vinesweeper_points", 0) + amount).coerceIn(0, MAX_POINTS)
            set("vinesweeper_points", points)
        }
    }
}
