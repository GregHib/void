package content.skill.agility.shortcut

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class UnderWallTunnels : Script {

    init {
        objectOperate("Climb-into", "yanille_underwall_tunnel_hole") { (target) ->
            tunnel(
                target,
                level = 15,
                start = Tile(2575, 3112),
                end = Tile(2575, 3108),
                direction = Direction.SOUTH,
            )
        }

        objectOperate("Climb-under", "yanille_underwall_tunnel_castle_wall") { (target) ->
            tunnel(
                target,
                level = 15,
                start = Tile(2575, 3107),
                end = Tile(2575, 3111),
                direction = Direction.NORTH,
            )
        }

        objectOperate("Climb-into", "edgeville_underwall_tunnel") { (target) ->
            tunnel(
                target,
                level = 21,
                start = Tile(3138, 3516),
                end = Tile(3143, 3514),
                direction = Direction.EAST,
            )
        }

        objectOperate("Climb-into", "grand_exchange_underwall_tunnel") { (target) ->
            tunnel(
                target,
                level = 21,
                start = Tile(3144, 3514),
                end = Tile(3139, 3516),
                direction = Direction.WEST,
            )
        }

        objectOperate("Climb-into", "falador_underwall_tunnel_north") { (target) ->
            tunnel(
                target,
                level = 26,
                start = Tile(2948, 3313),
                end = Tile(2948, 3310),
                direction = Direction.SOUTH,
            )
        }

        objectOperate("Climb-into", "falador_underwall_tunnel_south") { (target) ->
            tunnel(
                target,
                level = 26,
                start = Tile(2948, 3309),
                end = Tile(2948, 3312),
                direction = Direction.NORTH,
            )
        }
    }

    suspend fun Player.tunnel(target: GameObject, level: Int, start: Tile, end: Tile, direction: Direction) {
        if (!has(Skill.Agility, level)) {
            message("You need an Agility level of $level to negotiate this tunnel.")
            return
        }
        walkToDelay(start)
        clear("face_entity")
        face(direction)
        delay()
        anim("climb_into_tunnel")
        exactMoveDelay(start.add(direction), 50, direction)
        anim("tunnel_invisible")
        exactMoveDelay(end, 100, direction)
        delay()
        anim("climb_out_of_tunnel")
        exactMoveDelay(end.add(direction), startDelay = 15, delay = 33, direction = direction)
    }
}
