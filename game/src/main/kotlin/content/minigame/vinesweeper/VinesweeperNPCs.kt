package content.minigame.vinesweeper

import content.minigame.vinesweeper.Vinesweeper.Companion.addPoints
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Gnome farmers and rabbits racing to flags and dug up seeds.
 */
object VinesweeperNPCs {

    const val RACE_QUEUE = "vinesweeper_race"

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

    private const val CLEAR_RADIUS = 3
    private const val SHARE_RADIUS = 5
}
