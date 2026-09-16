package content.minigame.vinesweeper

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.queue
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
            dispatchFarmer(tile)
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
        dispatchFlag(tile)
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
        rabbit.queue.clear(RACE_QUEUE)
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

    /**
     * Send the nearest farmer and rabbit racing to a freshly planted flag at [tile].
     */
    fun dispatchFlag(tile: Tile) {
        nearest(tile, "rabbit_minesweeper")?.queue(RACE_QUEUE) {
            walkToDelay(tile)
            rabbitArrive(this, tile)
        }
        dispatchFarmer(tile)
    }

    /**
     * Send the nearest farmer to tidy up [tile], be it a flag or a dead plant.
     */
    fun dispatchFarmer(tile: Tile) {
        nearest(tile, "farmer_minesweeper")?.queue(RACE_QUEUE) {
            walkToDelay(tile)
            farmerArrive(this, tile)
        }
    }

    private fun nearest(tile: Tile, prefix: String): NPC? {
        val candidates = NPCs.at(tile.regionLevel).filter { it.id.startsWith(prefix) && !it.hide }
        return candidates.filter { !it.queue.contains(RACE_QUEUE) }.minByOrNull { it.tile.distanceTo(tile) }
            ?: candidates.minByOrNull { it.tile.distanceTo(tile) }
    }

    private suspend fun rabbitArrive(rabbit: NPC, tile: Tile) {
        val flag = VinesweeperField.objectAt(tile, "vinesweeper_flag") ?: return
        if (!VinesweeperField.isSeed(tile)) {
            return
        }
        rabbit.anim("vinesweeper_rabbit_eat_seed")
        rabbit.delay(2)
        if (VinesweeperField.objectAt(tile, "vinesweeper_flag") == null) {
            return
        }
        VinesweeperField.remove(tile)
        VinesweeperField.unflag(tile)
        flag.replace("vinesweeper_dead_plant")
    }

    private suspend fun farmerArrive(farmer: NPC, tile: Tile) {
        val flag = VinesweeperField.objectAt(tile, "vinesweeper_flag")
        if (flag != null) {
            farmerCheckFlag(farmer, flag)
            return
        }
        val deadPlant = VinesweeperField.objectAt(tile, "vinesweeper_dead_plant") ?: return
        farmerClearDeadPlant(farmer, deadPlant)
    }

    private suspend fun farmerCheckFlag(farmer: NPC, flag: GameObject) {
        val tile = flag.tile
        farmer.say("Ah, another flag to clear...")
        farmer.anim("vinesweeper_farmer_dig_flag")
        farmer.delay(3)
        if (VinesweeperField.objectAt(tile, "vinesweeper_flag") == null) {
            return
        }
        val owner = VinesweeperField.flagOwner(tile)?.let { Players.findByAccount(it) }
        VinesweeperField.unflag(tile)
        if (VinesweeperField.isSeed(tile)) {
            VinesweeperField.remove(tile)
            farmer.say("Ah! A seed. Points for everyone near me!")
            farmer.anim("vinesweeper_farmer_hooray")
            reward(farmer, owner)
        } else {
            farmer.say("Hmm, no seeds planted here.")
            farmer.delay(2)
            farmer.say("I'll have to keep this 'ere flag. Sorry.")
        }
        farmer.delay(2)
        clear(farmer, tile)
    }

    private suspend fun farmerClearDeadPlant(farmer: NPC, deadPlant: GameObject) {
        farmer.say("Hmm. Looks like there's a plant here.")
        farmer.anim("vinesweeper_farmer_dig_seed")
        farmer.delay(3)
        farmer.say("Gracious me! This one's dead.")
        farmer.anim("vinesweeper_farmer_smack")
        farmer.delay(2)
        clear(farmer, deadPlant.tile)
    }

    private fun reward(farmer: NPC, owner: Player?) {
        if (owner != null) {
            val level = owner.levels.get(Skill.Farming)
            owner.addPoints(random.nextInt(level, level * 4 + 1))
            returnFlag(owner)
        }
        for (player in Players) {
            if (player != owner && player.tile.within(farmer.tile, SHARE_RADIUS)) {
                val level = player.levels.get(Skill.Farming)
                player.addPoints(random.nextInt(level, level * 4 + 1) / 2)
            }
        }
    }

    private fun returnFlag(owner: Player) {
        if (owner.inventory.count("flag") >= Vinesweeper.MAX_FLAGS) {
            return
        }
        if (!owner.inventory.add("flag")) {
            FloorItems.add(owner.tile, "flag", owner = owner)
        }
    }

    /**
     * Reset every dug hole, dead plant and flag within [CLEAR_RADIUS] of [centre] then replant the field.
     */
    private fun clear(farmer: NPC, centre: Tile) {
        farmer.anim("vinesweeper_farmer_clear")
        for (x in -CLEAR_RADIUS..CLEAR_RADIUS) {
            for (y in -CLEAR_RADIUS..CLEAR_RADIUS) {
                val tile = centre.add(x, y)
                val obj = VinesweeperField.objectAt(tile, "vinesweeper_number_")
                    ?: VinesweeperField.objectAt(tile, "vinesweeper_dead_plant")
                    ?: VinesweeperField.objectAt(tile, "vinesweeper_flag")
                    ?: continue
                VinesweeperField.unflag(tile)
                obj.replace("vinesweeper_hole")
            }
        }
        VinesweeperField.populate()
    }

    companion object {
        private const val RACE_QUEUE = "vinesweeper_race"
        private const val CLEAR_RADIUS = 3
        private const val SHARE_RADIUS = 5
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
