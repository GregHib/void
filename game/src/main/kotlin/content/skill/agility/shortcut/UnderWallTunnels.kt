package content.skill.agility.shortcut

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

@Script
class UnderWallTunnels {

    init {
        objectOperate("Climb-into", "yanille_underwall_tunnel_hole") {
            tunnel(
                level = 15,
                start = Tile(2575, 3112),
                end = Tile(2575, 3108),
                direction = Direction.SOUTH,
            )
        }

        objectOperate("Climb-under", "yanille_underwall_tunnel_castle_wall") {
            tunnel(
                level = 15,
                start = Tile(2575, 3107),
                end = Tile(2575, 3111),
                direction = Direction.NORTH,
            )
        }

        objectOperate("Climb-into", "edgeville_underwall_tunnel") {
            tunnel(
                level = 21,
                start = Tile(3138, 3516),
                end = Tile(3143, 3514),
                direction = Direction.EAST,
            )
        }

        objectOperate("Climb-into", "grand_exchange_underwall_tunnel") {
            tunnel(
                level = 21,
                start = Tile(3144, 3514),
                end = Tile(3139, 3516),
                direction = Direction.WEST,
            )
        }

        objectOperate("Climb-into", "falador_underwall_tunnel_north") {
            tunnel(
                level = 26,
                start = Tile(2948, 3313),
                end = Tile(2948, 3310),
                direction = Direction.SOUTH,
            )
        }

        objectOperate("Climb-into", "falador_underwall_tunnel_south") {
            tunnel(
                level = 26,
                start = Tile(2948, 3309),
                end = Tile(2948, 3312),
                direction = Direction.NORTH,
            )
        }
    }

    suspend fun ObjectOption<Player>.tunnel(level: Int, start: Tile, end: Tile, direction: Direction) {
        if (!player.has(Skill.Agility, level)) {
            player.message("You need an Agility level of $level to negotiate this tunnel.")
            return
        }
        player.walkToDelay(start)
        player.clear("face_entity")
        player.face(direction)
        delay()
        player.anim("climb_into_tunnel")
        player.exactMoveDelay(start.add(direction), 50, direction)
        player.anim("tunnel_invisible")
        player.exactMoveDelay(end, 100, direction)
        delay()
        player.anim("climb_out_of_tunnel")
        player.exactMoveDelay(end.add(direction), startDelay = 15, delay = 33, direction = direction)
    }
}
