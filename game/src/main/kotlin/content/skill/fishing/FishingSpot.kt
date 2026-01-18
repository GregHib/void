package content.skill.fishing

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class FishingSpot : Script {

    val water = CollisionStrategies.Blocked
    val land = CollisionStrategies.Normal

    private val minRespawnTick = 280
    private val maxRespawnTick = 530

    init {
        npcSpawn("fishing_spot*") {
            softTimers.start("fishing_spot_respawn")
            val area: Area = this["area"] ?: return@npcSpawn
            move(this, area)
        }

        npcTimerStart("fishing_spot_respawn") { random.nextInt(280, 530) }

        npcTimerTick("fishing_spot_respawn") {
            move(this)
            // https://x.com/JagexAsh/status/1604892218380021761
            random.nextInt(280, 530)
        }
    }

    fun move(npc: NPC, area: Area) {
        npc.softQueue("spot_move", random.nextInt(minRespawnTick, maxRespawnTick)) {
            area.random(npc)?.let { tile ->
                npc.tele(tile)
            }
            move(npc, area)
        }
    }

    fun move(npc: NPC) {
        val area = Areas.get(npc.tile.zone).firstOrNull { it.name.endsWith("fishing_area") } ?: return
        /*
            Find all water tiles that have two water tiles next to them and land perpendicular
               [W]    [L]    [W]
            [L][W] [W][W][W] [W][L] [W][W][W]
               [W]           [W]       [L]
         */
        val tile = area.area.toList().filter { tile ->
            check(tile, water) &&
                (
                    (check(tile.addY(1), water) && check(tile.addY(-1), water) && (check(tile.addX(-1), land) || check(tile.addX(1), land))) ||
                        (check(tile.addX(-1), water) && check(tile.addX(1), water) && (check(tile.addY(1), land) || check(tile.addY(-1), land)))
                    )
        }.randomOrNull() ?: return
        npc.tele(tile)
        npc.softTimers.start("fishing_spot_respawn")
        val fishers: MutableSet<String> = npc.remove("fishers") ?: return
        for (fisher in fishers) {
            val player = Players.find(fisher) ?: continue
            player.mode = EmptyMode
            player.queue.clearWeak()
        }
        fishers.clear()
    }

    fun check(tile: Tile, strategy: CollisionStrategy): Boolean {
        val tileFlag = Collisions[tile.x, tile.y, tile.level]
        return strategy.canMove(
            tileFlag,
            CollisionFlag.BLOCK_NORTH_AND_SOUTH_EAST or
                CollisionFlag.BLOCK_NORTH_AND_SOUTH_WEST or
                CollisionFlag.BLOCK_NPCS,
        )
    }
}
