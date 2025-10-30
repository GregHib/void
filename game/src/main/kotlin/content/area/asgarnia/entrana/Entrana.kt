package content.area.asgarnia.entrana

import content.entity.effect.stun
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Entrana : Script {
    init {
        objectOperate("Cross", "entrana_gangplank_exit") {
            player.walkOverDelay(Tile(2834, 3333, 1))
            player.tele(2834, 3335, 0)
        }

        objectOperate("Cross", "gangplank_entrana_enter") {
            player.walkOverDelay(Tile(2834, 3334))
            player.tele(2834, 3332, 1)
        }

        objectOperate("Steal", "candles_entrana") {
            if (!Level.success(player.levels.get(Skill.Thieving), 25..160)) { // Unknown rate
                player.stun(player, 8, 100)
                player.message("A higher power smites you.")
                player.walkTo(player.tile.add(Direction.SOUTH_WEST))
                return@objectOperate
            }
            if (!player.inventory.add("white_candle")) {
                player.inventoryFull()
                return@objectOperate
            }
            player.exp(Skill.Thieving, 20.0)
            player.sound("pick")
            player.anim("take")
            player.message("You steal a candle.", ChatType.Filter)
        }
    }
}
