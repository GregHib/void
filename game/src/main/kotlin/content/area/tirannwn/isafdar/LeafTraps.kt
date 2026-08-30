package content.area.tirannwn.isafdar

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class LeafTraps : Script {

    init {
        objectOperate("Jump", "isafdar_leaves,isafdar_leaves_2,isafdar_leaves_3") { (target) ->
            val site = sites.firstOrNull { target.tile.within(it.centre, 3) } ?: return@objectOperate noInterest()
            // West, centre and south pits are jumped north-south; the north pit east-west
            val direction: Direction
            val dest = if (site.centre.y == 3262) {
                direction = if (tile.x > site.centre.x) Direction.WEST else Direction.EAST
                Tile(if (tile.x > site.centre.x) site.centre.x - 2 else site.centre.x + 2, site.centre.y)
            } else {
                direction = if (tile.y > site.centre.y) Direction.SOUTH else Direction.NORTH
                Tile(site.centre.x, if (tile.y > site.centre.y) site.centre.y - 2 else site.centre.y + 2)
            }
            face(direction)
            delay()
            if (Level.success(levels.get(Skill.Agility), 128..250)) { // TODO unknown rate
                anim("leaf_trap_jump")
                exactMoveDelay(dest, delay = 60, direction = direction)
                message("You safely jump across.", ChatType.Filter)
            } else {
                fall(this, site)
            }
        }

        objectOperate("Climb", "isafdar_protruding_rocks") { (target) ->
            val site = sites.firstOrNull { target.tile.within(it.pit, 2) } ?: return@objectOperate noInterest()
            tele(site.exit)
            message("You climb out of the pit.", ChatType.Filter)
        }

        entered("isafdar_leaf_trap_1", ::stumble)
        entered("isafdar_leaf_trap_2", ::stumble)
        entered("isafdar_leaf_trap_3", ::stumble)
        entered("isafdar_leaf_trap_4", ::stumble)
    }

    fun stumble(player: Player, definition: AreaDefinition) {
        val site = sites.firstOrNull { player.tile.within(it.centre, 3) } ?: return
        player.strongQueue("leaf_trap_fall", 1) {
            fall(player, site)
        }
    }

    private data class Site(val centre: Tile, val pit: Tile, val exit: Tile)

    companion object {
        // TODO which pit belongs to the west, centre and north traps is unverified
        private val sites = listOf(
            Site(centre = Tile(2209, 3203), pit = Tile(2313, 9656), exit = Tile(2209, 3201)),
            Site(centre = Tile(2267, 3203), pit = Tile(2354, 9656), exit = Tile(2267, 3201)),
            Site(centre = Tile(2274, 3174), pit = Tile(2336, 9656), exit = Tile(2274, 3172)),
            Site(centre = Tile(2277, 3262), pit = Tile(2354, 9643), exit = Tile(2275, 3262)),
        )

        private fun fall(player: Player, site: Site) {
            player.message("You fall through and onto some spikes.")
            player.tele(site.pit)
            player.directHit(random.nextInt(20, 41)) // TODO unknown damage
        }
    }
}
