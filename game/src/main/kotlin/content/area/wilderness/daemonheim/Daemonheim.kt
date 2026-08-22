package content.area.wilderness.daemonheim

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Daemonheim : Script {
    init {
        objectOperate("Jump-down", "rand_dragonkin_ruin_collapsed_floor_01_var") {
            delay(1)
            face(Direction.NORTH)
            anim("rand_player_agility_jump_down")
            delay(1)
            exactMoveDelay(Tile(3454, 3724, 1), startDelay = 10, delay = 30)
            tele(3454, 3725, 0)
        }

        // https://youtu.be/1e4dfeuKsdg?t=167
        // You have entered the automated grouping system and are queued for floors 1- 35.
        // Party found, entering dungeon.
    }
}
