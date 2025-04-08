package content.area.kandarin.yanille

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectOperate("Climb-into", "yanille_underwall_tunnel_hole") {
    if (!player.has(Skill.Agility, 15)) {
        player.message("You need at least 15 Agility to do that.") // TODO proper message
        return@objectOperate
    }
    player.walkToDelay(Tile(2575, 3112))
    player.clear("face_entity")
    player.face(Direction.SOUTH)
    delay()
    player.anim("climb_into_tunnel")
    player.exactMoveDelay(Tile(2575, 3111), 50, Direction.SOUTH)
    player.anim("tunnel_invisible")
    player.exactMoveDelay(Tile(2575, 3108), 100, Direction.SOUTH)
    player.anim("climb_out_of_tunnel")
    player.exactMoveDelay(Tile(2575, 3107), startDelay = 15, delay = 33, direction = Direction.SOUTH)
}

objectOperate("Climb-under", "yanille_underwall_tunnel_castle_wall") {
    if (!player.has(Skill.Agility, 15)) {
        player.message("You need at least 15 Agility to do that.") // TODO proper message
        return@objectOperate
    }
    player.walkToDelay(Tile(2575, 3107))
    player.clear("face_entity")
    player.face(Direction.NORTH)
    delay()
    player.anim("climb_into_tunnel")
    player.exactMoveDelay(Tile(2575, 3108), 50, Direction.NORTH)
    player.anim("tunnel_invisible")
    player.exactMoveDelay(Tile(2575, 3111), 100, Direction.NORTH)
    player.anim("climb_out_of_tunnel")
    player.exactMoveDelay(Tile(2575, 3112), startDelay = 15, delay = 33, direction = Direction.NORTH)
}