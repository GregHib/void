package content.area.kandarin.tree_gnome_stronghold

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

@Script
class TreeGnomeStronghold {

    val objects: GameObjects by inject()

    init {
        objectOperate("Open", "tree_gnome_door_east_closed", "tree_gnome_door_west_closed") {
            if (player.tile.x !in 2465..2466) {
                player.walkToDelay(player.tile.copy(x = player.tile.x.coerceIn(2465..2466)))
                delay()
                player.face(target)
            }
            target.replace(target.id.replace("_closed", "_opened"), ticks = 4)
            player.walkOverDelay(player.tile.addY(if (player.tile.y < target.tile.y) 2 else -2))
        }

        objectOperate("Open", "tree_gnome_gate_closed") {
            if (player.tile.x !in 2461..2461) {
                player.walkToDelay(player.tile.copy(x = player.tile.x.coerceIn(2461..2461)))
                delay()
                player.face(target)
            }
            target.replace(target.id.replace("_closed", "_west_opened"), ticks = 5, collision = false)
            objects.add("tree_gnome_gate_east_opened", Tile(2462, 3383), ObjectShape.CENTRE_PIECE_STRAIGHT, 0, 5, false)
            player.walkOverDelay(player.tile.copy(y = if (player.tile.y <= 3382) 3385 else 3382))
        }

        objectOperate("Climb", "gnome_stronghold_shortcut_rock_top") {
            if (!player.has(Skill.Agility, 37)) {
                player.message("You need an Agility level of 37 to negotiate these rocks.")
                return@objectOperate
            }
            player.walkToDelay(Tile(2486, 3515))
            player.walkToDelay(Tile(2487, 3515))
            player.anim("human_climbing_down")
            player.exactMoveDelay(Tile(2488, 3516), startDelay = 20, delay = 80, direction = Direction.SOUTH)
            delay()
            player.walkToDelay(Tile(2489, 3517))
            player.anim("human_climbing_down")
            player.exactMoveDelay(Tile(2489, 3521), delay = 120, direction = Direction.SOUTH)
        }

        objectOperate("Climb", "gnome_stronghold_shortcut_rock_bottom") {
            if (!player.has(Skill.Agility, 37)) {
                player.message("You need an Agility level of 37 to negotiate these rocks.")
                return@objectOperate
            }
            player.walkOverDelay(Tile(2489, 3521))
            player.face(Direction.SOUTH)
            player.renderEmote("climbing")
            player.walkOverDelay(Tile(2489, 3519))
            player.walkOverDelay(Tile(2489, 3517))
            player.walkOverDelay(Tile(2488, 3516))
            player.walkOverDelay(Tile(2487, 3515))
            player.clearRenderEmote()
            player.walkToDelay(Tile(2486, 3515))
        }
    }
}
