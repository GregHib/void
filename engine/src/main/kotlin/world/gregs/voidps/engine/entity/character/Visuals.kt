package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.network.login.protocol.visual.VisualMask
import world.gregs.voidps.network.login.protocol.visual.update.Hitsplat
import world.gregs.voidps.network.login.protocol.visual.update.Turn
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile

fun Character.flagAnimation() = visuals.flag(if (this is Player) VisualMask.PLAYER_ANIMATION_MASK else VisualMask.NPC_ANIMATION_MASK)

fun Character.flagColourOverlay() = visuals.flag(if (this is Player) VisualMask.PLAYER_COLOUR_OVERLAY_MASK else VisualMask.NPC_COLOUR_OVERLAY_MASK)

fun Character.flagSay() = visuals.flag(if (this is Player) VisualMask.PLAYER_SAY_MASK else VisualMask.NPC_SAY_MASK)

fun Character.flagHits() = visuals.flag(if (this is Player) VisualMask.PLAYER_HITS_MASK else VisualMask.NPC_HITS_MASK)

fun Character.flagExactMovement() = visuals.flag(if (this is Player) VisualMask.PLAYER_EXACT_MOVEMENT_MASK else VisualMask.NPC_EXACT_MOVEMENT_MASK)

fun Character.flagTurn() = visuals.flag(if (this is Player) VisualMask.PLAYER_TURN_MASK else VisualMask.NPC_TURN_MASK)

fun Character.flagTimeBar() = visuals.flag(if (this is Player) VisualMask.PLAYER_TIME_BAR_MASK else VisualMask.NPC_TIME_BAR_MASK)

fun Character.flagWatch() = visuals.flag(if (this is Player) VisualMask.PLAYER_WATCH_MASK else VisualMask.NPC_WATCH_MASK)

fun Character.setAnimation(id: String, delay: Int? = null, override: Boolean = false): Int {
    val definition = get<AnimationDefinitions>().getOrNull(id) ?: return -1
    val anim = visuals.animation
    if (!override && definition.priority < anim.priority) {
        return -1
    }
    val stand = definition["stand", true]
    if (stand) {
        anim.stand = definition.id
    }
    val force = definition["force", true]
    if (force) {
        anim.force = definition.id
    }
    val walk = definition["walk", true]
    if (walk) {
        anim.walk = definition.id
    }
    val run = definition["run", true]
    if (run) {
        anim.run = definition.id
    }
    anim.infinite = definition["infinite", false]
    if (stand || force || walk || run) {
        anim.delay = delay ?: definition["delay", 0]
        anim.priority = definition.priority
    }
    flagAnimation()
    return definition["ticks", 0]
}

context(SuspendableContext<*>) suspend fun Character.animDelay(id: String, override: Boolean = false) {
    val ticks = setAnimation(id, override = override)
    delay(ticks)
}

fun Character.clearAnimation() {
    visuals.animation.reset()
    flagAnimation()
}

fun Character.colourOverlay(colour: Int, delay: Int, duration: Int) {
    val overlay = visuals.colourOverlay
    overlay.colour = colour
    overlay.delay = delay
    overlay.duration = duration
    flagColourOverlay()
    softTimers.start("colour_overlay")
}

private fun primaryGfxFlagged(character: Character) = character.visuals.flagged(if (character is Player) VisualMask.PLAYER_GRAPHIC_1_MASK else VisualMask.NPC_GRAPHIC_1_MASK)

fun Character.flagPrimaryGraphic() = visuals.flag(if (this is Player) VisualMask.PLAYER_GRAPHIC_1_MASK else VisualMask.NPC_GRAPHIC_1_MASK)

fun Character.flagSecondaryGraphic() = visuals.flag(if (this is Player) VisualMask.PLAYER_GRAPHIC_2_MASK else VisualMask.NPC_GRAPHIC_2_MASK)


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
    visuals.turn.clear()
    flagWatch()
}

fun Character.watching(character: Character) = visuals.watch.index == watchIndex(character)

fun Character.clearWatch() {
    visuals.watch.index = -1
    flagWatch()
}

private fun watchIndex(character: Character) = if (character is Player) character.index or 0x8000 else character.index

val Character.turn: Delta
    get() = Tile(visuals.turn.targetX, visuals.turn.targetY, tile.level).delta(tile)

fun Character.turn(delta: Delta, update: Boolean = true): Boolean {
    if (delta == Delta.EMPTY) {
        clearTurn()
        return false
    }
    turn(delta.x, delta.y, update)
    return true
}

fun Character.clearTurn(): Boolean {
    visuals.turn.reset()
    return true
}

fun Character.turn(deltaX: Int = 0, deltaY: Int = -1, update: Boolean = true) {
    val turn = visuals.turn
    turn.targetX = tile.x + deltaX
    turn.targetY = tile.y + deltaY
    turn.direction = Turn.getFaceDirection(deltaX, deltaY)
    if (update) {
        flagTurn()
    }
}

val Character.facing: Direction
    get() = turn.toDirection()

fun Character.face(direction: Direction, update: Boolean = true) = turn(direction.delta.x, direction.delta.y, update)

fun Character.face(tile: Tile, update: Boolean = true) = turn(tile.delta(this.tile), update)

fun Character.facing(tile: Tile) = turn == tile.delta(this.tile)

fun Character.face(entity: Entity, update: Boolean = true) {
    val tile = nearestTile(entity)
    if (!face(tile, update) && entity is GameObject) {
        when {
            ObjectShape.isWall(entity.shape) -> face(Direction.cardinal[(entity.rotation + 3) and 0x3], update)
            ObjectShape.isCorner(entity.shape) -> face(Direction.ordinal[entity.rotation], update)
            else -> {
                val delta = tile.add(entity.width, entity.height).delta(entity.tile.add(entity.width, entity.height))
                turn(delta, update)
            }
        }
    }
}

fun Character.facing(entity: Entity) = turn == nearestTile(entity).delta(tile)

fun Character.nearestTile(entity: Entity): Tile {
    return when (entity) {
        is GameObject -> Distance.getNearest(entity.tile, entity.width, entity.height, this.tile)
        is NPC -> Distance.getNearest(entity.tile, entity.def.size, entity.def.size, this.tile)
        is Player -> Distance.getNearest(entity.tile, entity.appearance.size, entity.appearance.size, this.tile)
        else -> entity.tile
    }
}