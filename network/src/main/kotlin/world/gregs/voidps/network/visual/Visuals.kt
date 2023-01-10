package world.gregs.voidps.network.visual

import world.gregs.voidps.network.visual.update.*

abstract class Visuals(index: Int) {

    var flag: Int = 0
        private set

    var walkStep: Int = -1
    var runStep: Int = -1
    var moved: Boolean = false

    val animation = Animation()
    val primaryGraphic = Graphic()
    val secondaryGraphic = Graphic()
    val colourOverlay = ColourOverlay()
    val forceMovement = ForceMovement()
    val timeBar = TimeBar()
    val watch = Watch()
    val forceChat = ForceChat()
    val hits = Hits(self = index)

    fun flag(mask: Int) {
        flag = flag or mask
    }

    fun flagged(mask: Int): Boolean {
        return flag and mask != 0
    }

    open fun reset() {
        walkStep = -1
        runStep = -1
        moved = false
        flag = 0
        animation.clear()
        primaryGraphic.clear()
        forceMovement.clear()
        colourOverlay.clear()
        hits.clear()
        watch.clear()
        forceChat.clear()
        timeBar.clear()
        secondaryGraphic.clear()
    }
/*
    private fun clockwise(step: Direction) = when (step) {
        Direction.NORTH -> 0
        Direction.NORTH_EAST -> 1
        Direction.EAST -> 2
        Direction.SOUTH_EAST -> 3
        Direction.SOUTH -> 4
        Direction.SOUTH_WEST -> 5
        Direction.WEST -> 6
        Direction.NORTH_WEST -> 7
        else -> -1
    }
 */
}