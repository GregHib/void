package content.area.kandarin.tree_gnome_stronghold

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectOperate("Open", "tree_gnome_door_east_closed", "tree_gnome_door_west_closed") {
    if (player.tile.x !in 2465..2466) {
        player.walkToDelay(player.tile.copy(x = player.tile.x.coerceIn(2465..2466)))
        delay()
        player.face(target)
    }
    target.replace(target.id.replace("_closed", "_opened"), ticks = 4)
    player.walkOverDelay(player.tile.addY(if (player.tile.y < target.tile.y) 2 else -2))
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