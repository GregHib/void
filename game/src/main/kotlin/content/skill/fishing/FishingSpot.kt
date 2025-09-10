package content.skill.fishing

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.event.Script
@Script
class FishingSpot {

    val areas: AreaDefinitions by inject()
    val players: Players by inject()
    val collisions: Collisions by inject()
    
    val water = CollisionStrategies.Blocked
    val land = CollisionStrategies.Normal
    
    init {
        npcSpawn("fishing_spot_*") { npc ->
            npc.softTimers.start("fishing_spot_respawn")
        }

        npcTimerStart("fishing_spot_respawn") {
            // https://x.com/JagexAsh/status/1604892218380021761
            interval = random.nextInt(280, 530)
        }

        npcTimerTick("fishing_spot_respawn") { npc ->
            nextInterval = random.nextInt(280, 530)
            move(npc)
        }

    }

    fun move(npc: NPC) {
        val area = areas.get(npc.tile.zone).firstOrNull { it.name.endsWith("fishing_area") } ?: return
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
            val player = players.get(fisher) ?: continue
            player.mode = EmptyMode
            player.queue.clearWeak()
        }
        fishers.clear()
    }
    
    fun check(tile: Tile, strategy: CollisionStrategy): Boolean {
        val tileFlag = collisions[tile.x, tile.y, tile.level]
        return strategy.canMove(
            tileFlag,
            CollisionFlag.BLOCK_NORTH_AND_SOUTH_EAST or
                CollisionFlag.BLOCK_NORTH_AND_SOUTH_WEST or
                CollisionFlag.BLOCK_NPCS,
        )
    }
}
