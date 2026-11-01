package content.area.tirannwn.isafdar

import content.entity.combat.hit.damage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Tables
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
            val site = Tables.get("leaf_traps").rows().firstOrNull { target.tile.within(it.tile("centre"), 3) } ?: return@objectOperate noInterest()
            // West, centre and south pits are jumped north-south; the north pit east-west
            val direction: Direction
            val start: Tile
            val dest: Tile
            // Jump from whichever of the three lanes the player is in
            val centre = site.tile("centre")
            if (centre.y == 3262) {
                direction = if (tile.x > centre.x) Direction.WEST else Direction.EAST
                val lane = tile.y.coerceIn(centre.y - 1, centre.y + 1)
                start = Tile(centre.x - direction.delta.x * 2, lane, tile.level)
                dest = Tile(centre.x + direction.delta.x * 2, lane, tile.level)
            } else {
                direction = if (tile.y > centre.y) Direction.SOUTH else Direction.NORTH
                val lane = tile.x.coerceIn(centre.x - 1, centre.x + 1)
                start = Tile(lane, centre.y - direction.delta.y * 2, tile.level)
                dest = Tile(lane, centre.y + direction.delta.y * 2, tile.level)
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
            if (Level.success(levels.get(Skill.Agility), 180..255)) {
                anim("leaf_trap_jump", delay = 20)
                sound("jump", delay = 20)
                exactMoveDelay(dest, delay = 60, direction = direction, startDelay = 30)
                message("You safely jump across.")
            } else {
                fall(this, site)
            }
            delay()
            clear("crossing_trap")
        }

        objectOperate("Climb", "isafdar_protruding_rocks") { (target) ->
            val site = Tables.get("leaf_traps").rows().firstOrNull { target.tile.within(it.tile("pit"), 2) } ?: return@objectOperate noInterest()
            anim("pit_getup")
            delay()
            message("You climb out of the pit.")
            tele(site.tile("exit"))
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
        val site = Tables.get("leaf_traps").rows().firstOrNull { player.tile.within(it.tile("centre"), 3) } ?: return
        player.strongQueue("leaf_trap_fall") {
            player.steps.clear()
            player.message("It's a trap!")
            player.anim("leaf_trap_stumble")
            player.sound("stumble_loop", repeat = 3)
            player.delay(1)
            fall(player, site)
        }
    }

    private fun fall(player: Player, site: RowDefinition) {
        player.message("You fall through and onto some spikes.")
        player.tele(site.tile("pit"))
        player.anim("pit_getup")
        player.say("Ouch!")
        player.sound("land_flatter")
        player.damage(random.nextInt(20, 181)) // TODO unknown min damage
    }
}
