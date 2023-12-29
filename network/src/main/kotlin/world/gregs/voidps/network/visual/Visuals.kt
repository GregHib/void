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
    val turn = Turn()
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
        turn.clear()
        watch.clear()
        forceChat.clear()
        timeBar.clear()
        secondaryGraphic.clear()
    }
}