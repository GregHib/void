package content.area.tirannwn.isafdar

import content.entity.combat.hit.damage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
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
            val start: Tile
            val dest: Tile
            if (site.centre.y == 3262) {
                direction = if (tile.x > site.centre.x) Direction.WEST else Direction.EAST
                start = Tile(site.centre.x - direction.delta.x * 2, site.centre.y, tile.level)
                dest = Tile(site.centre.x + direction.delta.x * 2, site.centre.y, tile.level)
            } else {
                direction = if (tile.y > site.centre.y) Direction.SOUTH else Direction.NORTH
                start = Tile(site.centre.x, site.centre.y - direction.delta.y * 2, tile.level)
                dest = Tile(site.centre.x, site.centre.y + direction.delta.y * 2, tile.level)
            }
            this["crossing_trap"] = true
            if (tile != start) {
                // Line up with the trap before jumping
                walkToDelay(start, forceWalk = true)
                if (tile != start) {
                    clear("crossing_trap")
                    return@objectOperate
                }
            }
            if (Level.success(levels.get(Skill.Agility), 128..250)) { // TODO unknown rate
                anim("leaf_trap_jump", delay = 20)
                sound("jump", delay = 20)
                exactMoveDelay(dest, delay = 79, direction = direction, startDelay = 30)
                message("You safely jump across.")
            } else {
                fall(this, site)
            }
            clear("crossing_trap")
        }

        objectOperate("Climb", "isafdar_protruding_rocks") { (target) ->
            val site = sites.firstOrNull { target.tile.within(it.pit, 2) } ?: return@objectOperate noInterest()
            anim("pit_getup")
            delay()
            message("You climb out of the pit.")
            tele(site.exit)
        }

        entered("isafdar_leaf_trap_1", ::stumble)
        entered("isafdar_leaf_trap_2", ::stumble)
        entered("isafdar_leaf_trap_3", ::stumble)
        entered("isafdar_leaf_trap_4", ::stumble)
    }

    fun stumble(player: Player, definition: AreaDefinition) {
        if (player.contains("crossing_trap")) {
            return
        }
        val site = sites.firstOrNull { player.tile.within(it.centre, 3) } ?: return
        player.strongQueue("leaf_trap_fall") {
            player.steps.clear()
            player.message("It's a trap!")
            player.anim("leaf_trap_stumble")
            player.sound("stumble_loop", repeat = 3)
            player.delay(1)
            fall(player, site)
        }
    }

    private data class Site(val centre: Tile, val pit: Tile, val exit: Tile)

    companion object {
        // TODO which pit belongs to the west and north traps is unverified
        private val sites = listOf(
            Site(centre = Tile(2209, 3203), pit = Tile(2313, 9656), exit = Tile(2209, 3201)),
            Site(centre = Tile(2267, 3203), pit = Tile(2355, 9656), exit = Tile(2267, 3201)),
            Site(centre = Tile(2274, 3174), pit = Tile(2336, 9656), exit = Tile(2274, 3172)),
            Site(centre = Tile(2277, 3262), pit = Tile(2354, 9643), exit = Tile(2275, 3262)),
        )

        private fun fall(player: Player, site: Site) {
            player.message("You fall through and onto some spikes.")
            player.tele(site.pit)
            player.anim("pit_getup")
            player.say("Ouch!")
            player.sound("fall_land")
            player.damage(random.nextInt(20, 41)) // TODO unknown damage
        }
    }
}
