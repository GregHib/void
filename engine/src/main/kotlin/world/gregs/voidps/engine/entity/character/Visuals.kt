package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.visual.VisualMask
import world.gregs.voidps.network.login.protocol.visual.update.Hitsplat

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