package content.area.fremennik_province.rellekka.brine_rat_cavern

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

class BrineRatCavern : Script {
    init {
        npcOperate("Roll", "olaf_cave_boulder") { (target) ->
            if (!target.tile.equals(2691, 10124)) {
                return@npcOperate
            }
            walkToDelay(Tile(2692, 10123))
            message("You push the boulder.")
            face(Direction.NORTH)
            areaSound("olaf_roll_boulder", tile, radius = 7)
            delay(1)
            anim("olaf_boulder_push")
            delay(1)
            target.exactMove(Tile(2691, 10126), startDelay = 20, delay = 75)
            exactMoveDelay(Tile(2691, 10125), startDelay = 20, delay = 75, direction = Direction.NORTH)
            delay(3)
            clearAnim()
            face(Direction.NORTH)
            delay(5)
            tele(2691, 10125)
        }

        objTeleportTakeOff("Exit", "brine_rat_cavern_exit*") { _, _ ->
            message("You squeeze through the narrow crack in the cliff face.")
            1
        }

        objTeleportLand("Exit", "brine_rat_cavern_exit*") { _, _ ->
            message("You exit the cave. From this side you can't even make out how to get into the cave!")
            message("You'll likely need to dig to get back inside.")
        }

        itemOption("Dig", "spade") {
            if (tile !in Areas["brine_rat_cavern_entrance"]) {
                return@itemOption
            }
            message("You dig a hole...")
            open("fade_out")
            delay(4)
            tele(2697, 10120)
            delay(1)
            message("...and fall into a dark and slimy pit!")
            gfx("stun_long", delay = 20)
            open("fade_in")
            sound("olaf_fall_into_cave")
        }
    }
}
