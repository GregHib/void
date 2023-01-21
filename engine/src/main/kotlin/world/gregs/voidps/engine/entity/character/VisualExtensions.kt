package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.move.move
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.turn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.face
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.visual.VisualMask
import world.gregs.voidps.network.visual.Visuals
import world.gregs.voidps.network.visual.update.Hit

fun Character.flagAnimation() = visuals.flag(if (this is Player) VisualMask.PLAYER_ANIMATION_MASK else VisualMask.NPC_ANIMATION_MASK)

fun Character.flagColourOverlay() = visuals.flag(if (this is Player) VisualMask.PLAYER_COLOUR_OVERLAY_MASK else VisualMask.NPC_COLOUR_OVERLAY_MASK)

fun Character.flagForceChat() = visuals.flag(if (this is Player) VisualMask.PLAYER_FORCE_CHAT_MASK else VisualMask.NPC_FORCE_CHAT_MASK)

fun Character.flagHits() = visuals.flag(if (this is Player) VisualMask.PLAYER_HITS_MASK else VisualMask.NPC_HITS_MASK)

fun Character.flagForceMovement() = visuals.flag(if (this is Player) VisualMask.PLAYER_FORCE_MOVEMENT_MASK else VisualMask.NPC_FORCE_MOVEMENT_MASK)

fun Character.flagTimeBar() = visuals.flag(if (this is Player) VisualMask.PLAYER_TIME_BAR_MASK else VisualMask.NPC_TIME_BAR_MASK)

fun Character.setAnimation(id: String, delay: Int? = null, override: Boolean = false): Int {
    val definition = get<AnimationDefinitions>().getOrNull(id) ?: return -1
    val anim = visuals.animation
    if (!override && hasEffect("animation_delay") && definition.priority < anim.priority) {
        return -1
    }
    start("animation_delay", 1)
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
    start("colour_overlay", (delay + duration) / 30)
}

var Character.forceChat: String
    get() = visuals.forceChat.text
    set(value) {
        visuals.forceChat.text = value
        flagForceChat()
    }

private fun getPlayerMask(index: Int) = when (index) {
    1 -> VisualMask.PLAYER_GRAPHIC_2_MASK
    else -> VisualMask.PLAYER_GRAPHIC_1_MASK
}

private fun getNPCMask(index: Int) = when (index) {
    1 -> VisualMask.NPC_GRAPHIC_2_MASK
    else -> VisualMask.NPC_GRAPHIC_1_MASK
}

private fun index(character: Character) = if (character is Player) character.visuals.getIndex(::getPlayerMask) else character.visuals.getIndex(::getNPCMask)

fun Character.flagGraphic(index: Int) = visuals.flag(if (this is Player) getPlayerMask(index) else getNPCMask(index))

private fun Visuals.getIndex(indexer: (Int) -> Int): Int {
    for (i in 0 until 2) {
        if (!flagged(indexer(i))) {
            return i
        }
    }
    return -1
}

fun Character.setGraphic(id: String, delay: Int? = null) {
    val definition = get<GraphicDefinitions>().getOrNull(id) ?: return
    val index = index(this)
    val graphic = if (index == 0) visuals.primaryGraphic else visuals.secondaryGraphic
    graphic.id = definition.id
    graphic.delay = delay ?: definition["delay", 0]
    val characterHeight = (this as? NPC)?.def?.get("height", 0) ?: 40
    graphic.height = (characterHeight + definition["height", -1000]).coerceAtLeast(0)
    graphic.rotation = definition["rotation", 0]
    graphic.forceRefresh = definition["force_refresh", false]
    flagGraphic(index)
}

fun Character.clearGraphic() {
    val index = index(this)
    val graphic = if (index == 0) visuals.primaryGraphic else visuals.secondaryGraphic
    graphic.reset()
    flagGraphic(index)
}

fun Character.hit(source: Character, amount: Int, mark: Hit.Mark, delay: Int = 0, critical: Boolean = false, soak: Int = -1) {
    val after = (levels.get(Skill.Constitution) - amount).coerceAtLeast(0)
    val percentage = levels.getPercent(Skill.Constitution, after, 255.0).toInt()
    visuals.hits.hits.add(Hit(amount, mark, percentage, delay, critical, if (source is NPC) -source.index else source.index, soak))
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

fun Character.watch(character: Character?) {
    val mask = if (this is Player) VisualMask.PLAYER_WATCH_MASK else if (this is NPC) VisualMask.NPC_WATCH_MASK else return
    visuals.watch.index = if (character is Player) character.index or 0x8000 else if (character is NPC) character.index else -1
    visuals.flag(mask)
}

/**
 * @param endDelta The delta position to move towards
 * @param endDelay Number of client ticks to take moving
 * @param startDelta The delta position to start at
 * @param startDelay Client ticks until starting the movement
 * @param direction The cardinal direction to face during movement
 */
fun Character.setForceMovement(
    endDelta: Delta = Delta.EMPTY,
    endDelay: Int = 0,
    startDelta: Delta = Delta.EMPTY,
    startDelay: Int = 0,
    direction: Direction = Direction.NONE
) {
    val move = visuals.forceMovement
    check(endDelay > startDelay) { "End delay ($endDelay) must be after start delay ($startDelay)." }
    move.startX = startDelta.x
    move.startY = startDelta.y
    move.startDelay = startDelay
    move.endX = endDelta.x
    move.endY = endDelta.y
    move.endDelay = endDelay
    move.direction = direction.ordinal
    flagForceMovement()
}

fun Character.forceWalk(delta: Delta, delay: Int = 0, direction: Direction = Direction.NONE, block: () -> Unit = {}) {
    setForceMovement(delta, delay, direction = direction)
    this["force_walk"] = block
    delay(delay / 30) {
        move(delta)
        clearAnimation()
    }
}

fun Character.forceWalk(target: Tile, delay: Int = tile.distanceTo(target) * 30, direction: Direction = Direction.NONE, block: () -> Unit = {}) {
    forceWalk(target.delta(tile), delay, direction, block)
}

fun Character.face(entity: Entity, update: Boolean = true) {
    if (this is NPC) {
        turn(entity, update)
        return
    }
    this as Player
    val tile = faceTile(entity)
    var delta = tile.delta(this.tile)
    if (delta != Delta.EMPTY) {
        face(delta.x, delta.y, update)
    } else if (entity is GameObject) {
        when (entity.type) {
            0, 4, 5, 6, 7, 8, 9 -> face(Direction.cardinal[(entity.rotation + 3) and 0x3], update)
            1, 2, 3 -> face(Direction.ordinal[entity.rotation], update)
            else -> {
                delta = tile.add(size).delta(entity.tile.add(entity.size))
                if (delta != Delta.EMPTY) {
                    face(delta.x, delta.y, update)
                }
            }
        }
    }
}

fun Character.faceTile(entity: Entity): Tile {
    return when (entity) {
        is GameObject -> Distance.getNearest(entity.tile, entity.size, this.tile)
        is NPC -> Distance.getNearest(entity.tile, entity.size, this.tile)
        is Player -> Distance.getNearest(entity.tile, entity.size, this.tile)
        else -> entity.tile
    }
}

fun Character.face(direction: Direction, update: Boolean = true) {
    if (this is NPC) {
        turn(direction, update)
    } else if (this is Player) {
        face(direction.delta.x, direction.delta.y, update)
    }
}