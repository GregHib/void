package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.network.login.protocol.visual.VisualMask
import world.gregs.voidps.network.login.protocol.visual.update.Hitsplat
import world.gregs.voidps.network.login.protocol.visual.update.Face
import world.gregs.voidps.type.*

fun Character.flagAnimation() = visuals.flag(if (this is Player) VisualMask.PLAYER_ANIMATION_MASK else VisualMask.NPC_ANIMATION_MASK)

fun Character.flagColourOverlay() = visuals.flag(if (this is Player) VisualMask.PLAYER_COLOUR_OVERLAY_MASK else VisualMask.NPC_COLOUR_OVERLAY_MASK)

fun Character.flagSay() = visuals.flag(if (this is Player) VisualMask.PLAYER_SAY_MASK else VisualMask.NPC_SAY_MASK)

fun Character.flagHits() = visuals.flag(if (this is Player) VisualMask.PLAYER_HITS_MASK else VisualMask.NPC_HITS_MASK)

fun Character.flagExactMovement() = visuals.flag(if (this is Player) VisualMask.PLAYER_EXACT_MOVEMENT_MASK else VisualMask.NPC_EXACT_MOVEMENT_MASK)

fun Character.flagTurn() = visuals.flag(if (this is Player) VisualMask.PLAYER_FACE_MASK else VisualMask.NPC_FACE_MASK)

fun Character.flagTimeBar() = visuals.flag(if (this is Player) VisualMask.PLAYER_TIME_BAR_MASK else VisualMask.NPC_TIME_BAR_MASK)

fun Character.flagWatch() = visuals.flag(if (this is Player) VisualMask.PLAYER_WATCH_MASK else VisualMask.NPC_WATCH_MASK)

fun Character.flagPrimaryGraphic() = visuals.flag(if (this is Player) VisualMask.PLAYER_GRAPHIC_1_MASK else VisualMask.NPC_GRAPHIC_1_MASK)

fun Character.flagSecondaryGraphic() = visuals.flag(if (this is Player) VisualMask.PLAYER_GRAPHIC_2_MASK else VisualMask.NPC_GRAPHIC_2_MASK)

fun Character.colourOverlay(colour: Int, delay: Int, duration: Int) {
    val overlay = visuals.colourOverlay
    overlay.colour = colour
    overlay.delay = delay
    overlay.duration = duration
    flagColourOverlay()
    softTimers.start("colour_overlay")
}

fun Character.hit(source: Character, amount: Int, mark: Hitsplat.Mark, delay: Int = 0, critical: Boolean = false, soak: Int = -1) {
    val after = (levels.get(Skill.Constitution) - amount).coerceAtLeast(0)
    val percentage = levels.getPercent(Skill.Constitution, after, 255.0).toInt()
    visuals.hits.hits.add(Hitsplat(amount, mark, percentage, delay, critical, if (source is NPC) -source.index else source.index, soak))
    flagHits()
}

fun Character.setTimeBar(full: Boolean = false, exponentialDelay: Int = 0, delay: Int = 0, increment: Int = 0) {
    val bar = visuals.timeBar
    bar.full = full
    bar.exponentialDelay = exponentialDelay
    bar.delay = delay
    bar.increment = increment
    flagTimeBar()
}

fun Character.watch(character: Character) {
    visuals.watch.index = watchIndex(character)
    visuals.face.clear()
    flagWatch()
}

fun Character.watching(character: Character) = visuals.watch.index == watchIndex(character)

fun Character.clearWatch() {
    visuals.watch.index = -1
    flagWatch()
}

private fun watchIndex(character: Character) = if (character is Player) character.index or 0x8000 else character.index


fun Character.face(delta: Delta, update: Boolean = true): Boolean {
    if (delta == Delta.EMPTY) {
        clearFace()
        return false
    }
    face(delta.x, delta.y, update)
    return true
}

fun Character.clearFace(): Boolean {
    visuals.face.reset()
    return true
}

fun Character.face(deltaX: Int = 0, deltaY: Int = -1, update: Boolean = true) {
    val turn = visuals.face
    turn.targetX = tile.x + deltaX
    turn.targetY = tile.y + deltaY
    turn.direction = Face.getFaceDirection(deltaX, deltaY)
    if (update) {
        flagTurn()
    }
}

val Character.direction: Direction
    get() = Direction.of(visuals.face.targetX - tile.x, visuals.face.targetY - tile.y)

fun Character.face(direction: Direction, update: Boolean = true) = face(direction.delta, update)

fun Character.face(tile: Tile, update: Boolean = true) = face(tile.delta(this.tile), update)

fun Character.face(entity: Entity, update: Boolean = true) {
    val tile = nearestTile(entity)
    if (!face(tile, update) && entity is GameObject) {
        when {
            ObjectShape.isWall(entity.shape) -> face(Direction.cardinal[(entity.rotation + 3) and 0x3], update)
            ObjectShape.isCorner(entity.shape) -> face(Direction.ordinal[entity.rotation], update)
            else -> {
                val delta = tile.add(entity.width, entity.height).delta(entity.tile.add(entity.width, entity.height))
                face(delta, update)
            }
        }
    }
}

private fun Character.nearestTile(entity: Entity): Tile {
    return when (entity) {
        is GameObject -> Distance.getNearest(entity.tile, entity.width, entity.height, this.tile)
        is NPC -> Distance.getNearest(entity.tile, entity.def.size, entity.def.size, this.tile)
        is Player -> Distance.getNearest(entity.tile, entity.appearance.size, entity.appearance.size, this.tile)
        else -> entity.tile
    }
}