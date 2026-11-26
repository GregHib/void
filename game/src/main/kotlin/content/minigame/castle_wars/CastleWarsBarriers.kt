package content.minigame.castle_wars

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class CastleWarsBarriers : Script {
    init {
        objectOperate("Pass", "castle_wars_zamorak_spawn_door") { (target) ->
            if (equipped(EquipSlot.Cape).id != "zamorak_team_cape") {
                message("Only those who follow zamorak can pass through this barrier.")// TODO proper message
                return@objectOperate
            }
            pass(target)
        }

        objectOperate("Pass", "castle_wars_saradomin_spawn_door") { (target) ->
            if (equipped(EquipSlot.Cape).id != "saradomin_team_cape") {
                message("Only those who follow saradomin can pass through this barrier.")// TODO proper message
                return@objectOperate
            }
            pass(target)
        }
    }

    private suspend fun Player.pass(target: GameObject) {
        if (target.rotation == 2) {
            val x = if (tile.x <= target.tile.x) target.tile.x + 1 else target.tile.x
            val y = tile.y.coerceIn(target.tile.y, target.tile.y + 1)
            walkOverDelay(tile.copy(y = y))
            walkOverDelay(Tile(x, y))
        } else if (target.rotation == 3) {
            val x = tile.x.coerceIn(target.tile.x, target.tile.x + 1)
            val y = if (tile.y >= target.tile.y) target.tile.y - 1 else target.tile.y
            walkOverDelay(tile.copy(x = x))
            walkOverDelay(Tile(x, y))
        }
    }
}