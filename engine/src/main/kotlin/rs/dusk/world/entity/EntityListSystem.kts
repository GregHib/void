import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.Deregistered
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Direction.Companion.cardinal
import rs.dusk.engine.model.entity.Direction.Companion.ordinal
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.index.update.visual.player.face
import rs.dusk.engine.model.entity.index.update.visual.player.movementType
import rs.dusk.engine.model.entity.index.update.visual.player.temporaryMoveType
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.entity.item.FloorItems
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.entity.proj.Projectile
import rs.dusk.engine.model.entity.proj.Projectiles
import rs.dusk.engine.model.world.map.collision.*
import rs.dusk.engine.model.world.map.collision.CollisionFlag.LAND
import rs.dusk.engine.model.world.map.collision.CollisionFlag.NORTH_OR_EAST
import rs.dusk.engine.model.world.map.collision.CollisionFlag.NORTH_OR_WEST
import rs.dusk.engine.model.world.map.collision.CollisionFlag.SEA
import rs.dusk.engine.model.world.map.collision.CollisionFlag.SKY
import rs.dusk.engine.model.world.map.collision.CollisionFlag.SOUTH_OR_EAST
import rs.dusk.engine.model.world.map.collision.CollisionFlag.SOUTH_OR_WEST
import rs.dusk.utility.inject

val players: Players by inject()
val npcs: NPCs by inject()
val objects: Objects by inject()
val items: FloorItems by inject()
val projectiles: Projectiles by inject()
val collisions: Collisions by inject()

Registered priority 9 then {
    when (entity) {
        is Player -> {
            players.add(entity)
            entity.viewport.players.add(entity)
            entity.temporaryMoveType = PlayerMoveType.Walk
            entity.movementType = PlayerMoveType.None
            entity.face()
        }
        is NPC -> npcs.add(entity)
        is Location -> {
            objects[entity.tile] = entity
            modifyCollision(entity, ADD_MASK)
        }
        is FloorItem -> items[entity.tile] = entity
        is Projectile -> projectiles[entity.tile] = entity
    }
}

Deregistered priority 9 then {
    when (entity) {
        is Location -> {
            objects.remove(entity.tile, entity)
            modifyCollision(entity, REMOVE_MASK)
        }
    }
}

fun modifyCollision(location: Location, changeType: Int) {
    when (location.type) {
        in 0..3 -> {
            modifyWall(location, 0, changeType)
            if (location.def.projectileClipped) {
                modifyWall(location, 1, changeType)
            }
            if (!location.def.swimmable) {
                modifyWall(location, 2, changeType)
            }
        }
        in 9..21 -> {
            var mask = LAND

            if (location.def.projectileClipped) {
                mask = mask or SKY
            }

            if (!location.def.swimmable) {
                mask = mask or SEA
            }

            var width = location.size.width
            var height = location.size.height

            if (location.rotation and 0x1 == 1) {
                width = location.size.height
                height = location.size.width
            }

            for (offsetX in 0 until width) {
                for (offsetY in 0 until height) {
                    modifyMask(
                        location.tile.x + offsetX,
                        location.tile.y + offsetY,
                        location.tile.plane,
                        mask,
                        changeType
                    )
                }
            }
        }
        22 -> {
            if (location.def.solid == 1) {
                modifyMask(location.tile.x, location.tile.y, location.tile.plane, CollisionFlag.FLOOR_DECO, changeType)
            }
        }
    }
}

fun modifyWall(location: Location, motion: Int, changeType: Int) {
    if (location.type == 2) {
        val direction = ordinal[location.rotation and 0x3]
        val or = when (direction) {
            Direction.NORTH_WEST -> NORTH_OR_WEST
            Direction.NORTH_EAST -> NORTH_OR_EAST
            Direction.SOUTH_EAST -> SOUTH_OR_EAST
            Direction.SOUTH_WEST -> SOUTH_OR_WEST
            else -> 0
        }
        modifyMask(location.tile.x, location.tile.y, location.tile.plane, or, changeType)
        modifyMask(
            location.tile.x + direction.delta.x,
            location.tile.y,
            location.tile.plane,
            direction.horizontal().flag(),
            changeType
        )
        modifyMask(
            location.tile.x,
            location.tile.y + direction.delta.y,
            location.tile.plane,
            direction.vertical().flag(),
            changeType
        )
    } else {
        val direction = when (location.type) {
            0 -> cardinal[(location.rotation + 3) and 0x3]
            1, 3 -> ordinal[location.rotation and 0x3]
            else -> return
        }
        modifyMask(location.tile.x, location.tile.y, location.tile.plane, direction.flag(motion), changeType)
        modifyMask(
            location.tile.x + direction.delta.x,
            location.tile.y + direction.delta.y,
            location.tile.plane,
            direction.inverse().flag(motion),
            changeType
        )
    }
}

val ADD_MASK = 0
val REMOVE_MASK = 1
val SET_MASK = 2

fun modifyMask(x: Int, y: Int, plane: Int, mask: Int, changeType: Any) {
    when (changeType) {
        ADD_MASK -> collisions.add(x, y, plane, mask)
        REMOVE_MASK -> collisions.remove(x, y, plane, mask)
        SET_MASK -> collisions[x, y, plane] = mask
    }
}

fun applyMotion(mask: Int, motion: Int): Int {
    return when (motion) {
        1 -> mask shl 9
        2 -> mask shl 22
        else -> mask
    }
}

fun Direction.flag(motion: Int) = applyMotion(flag(), motion)