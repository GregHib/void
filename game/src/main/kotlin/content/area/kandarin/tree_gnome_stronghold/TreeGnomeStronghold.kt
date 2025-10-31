package content.area.kandarin.tree_gnome_stronghold

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class TreeGnomeStronghold : Script {

    val objects: GameObjects by inject()

    init {
        objectOperate("Open", "tree_gnome_door_east_closed,tree_gnome_door_west_closed") { (target) ->
            if (tile.x !in 2465..2466) {
                walkToDelay(tile.copy(x = tile.x.coerceIn(2465..2466)))
                delay()
                face(target)
            }
            target.replace(target.id.replace("_closed", "_opened"), ticks = 4)
            walkOverDelay(tile.addY(if (tile.y < target.tile.y) 2 else -2))
        }

        objectOperate("Open", "tree_gnome_gate_closed") { (target) ->
            if (tile.x !in 2461..2461) {
                walkToDelay(tile.copy(x = tile.x.coerceIn(2461..2461)))
                delay()
                face(target)
            }
            target.replace(target.id.replace("_closed", "_west_opened"), ticks = 5, collision = false)
            objects.add("tree_gnome_gate_east_opened", Tile(2462, 3383), ObjectShape.CENTRE_PIECE_STRAIGHT, 0, 5, false)
            walkOverDelay(tile.copy(y = if (tile.y <= 3382) 3385 else 3382))
        }

        objectOperate("Climb", "gnome_stronghold_shortcut_rock_top") {
            if (!has(Skill.Agility, 37)) {
                message("You need an Agility level of 37 to negotiate these rocks.")
                return@objectOperate
            }
            walkToDelay(Tile(2486, 3515))
            walkToDelay(Tile(2487, 3515))
            anim("human_climbing_down")
            exactMoveDelay(Tile(2488, 3516), startDelay = 20, delay = 80, direction = Direction.SOUTH)
            delay()
            walkToDelay(Tile(2489, 3517))
            anim("human_climbing_down")
            exactMoveDelay(Tile(2489, 3521), delay = 120, direction = Direction.SOUTH)
        }

        objectOperate("Climb", "gnome_stronghold_shortcut_rock_bottom") {
            if (!has(Skill.Agility, 37)) {
                message("You need an Agility level of 37 to negotiate these rocks.")
                return@objectOperate
            }
            walkOverDelay(Tile(2489, 3521))
            face(Direction.SOUTH)
            renderEmote("climbing")
            walkOverDelay(Tile(2489, 3519))
            walkOverDelay(Tile(2489, 3517))
            walkOverDelay(Tile(2488, 3516))
            walkOverDelay(Tile(2487, 3515))
            clearRenderEmote()
            walkToDelay(Tile(2486, 3515))
        }
    }
}
