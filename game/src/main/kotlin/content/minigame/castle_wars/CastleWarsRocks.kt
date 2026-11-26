package content.minigame.castle_wars

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class CastleWarsRocks : Script {
    init {
        // TODO collapse timer?
        objectOperate("Mine", "castle_wars_blocked_tunnel_*") { (target) ->
            if (!inventory.contains("bronze_pickaxe")) {
                message("You need a pickaxe to mine this rock.") // TODO proper message
                return@objectOperate
            }
            // https://youtu.be/izRAfNAXS9s?t=286
            message("You swing your pick at the rock.", ChatType.Filter)
            delay(2)
            // TODO animation and success rate
            decrease(target)
        }

        itemOnObjectOperate("explosive_potion", "castle_wars_blocked_tunnel_*") { (target) ->
            if (inventory.remove("explosive_potion")) {
                decrease(target)
                // TODO gfx
            }
        }

        objectOperate("Collapse", "castle_wars_cave_wall_rockslide") { (target) ->
            if (!inventory.contains("bronze_pickaxe")) {
                message("You need a pickaxe to mine this rock.") // TODO proper message
                return@objectOperate
            }
            // https://youtu.be/izRAfNAXS9s?t=286
            message("You swing your pick at the rock.", ChatType.Filter)
            delay(2)
            collapse(target)
        }

        itemOnObjectOperate("explosive_potion", "castle_wars_cave_wall_rockslide") { (target) ->
            if (inventory.remove("explosive_potion")) {
                collapse(target)
            }
        }
    }

    private fun Player.collapse(target: GameObject) {
        val tile = when {
            target.x < 2394 -> Tile(2391, 9501)
            target.y > 9510 -> Tile(2400, 9512)
            target.x > 2406 -> Tile(2409, 9503)
            target.y < 9497 -> Tile(2401, 9494)
            else -> return
        }
        val obj = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return
        if (obj.id == "castle_wars_blocked_tunnel_3") {
            obj.replace("castle_wars_blocked_tunnel_1")
            // https://youtu.be/izRAfNAXS9s?t=286
            message("You've collapsed the tunnel!")
            for (x in 0 until 2) {
                for (y in 0 until 2) {
                    for (player in Players.at(target.tile.add(x, y))) {
                        player.directHit(player.levels.get(Skill.Constitution))
                    }
                }
            }
        }
    }

    private fun Player.decrease(target: GameObject) {
        // TODO success rate
        val id = target.id.substringAfterLast("_").toInt()
        // https://youtu.be/fMI0QS8HX24?t=823
        when (id) {
            1 -> message("You manage to remove some of the rocks.")
            2 -> message("You manage to clear the rest of the rocks.")
            else -> return
        }
        target.replace(target.id.replace("_${id}", "_${id + 1}"))
    }
}