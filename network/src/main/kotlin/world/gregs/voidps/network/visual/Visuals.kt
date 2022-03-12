package world.gregs.voidps.network.visual

import world.gregs.voidps.network.visual.update.*

abstract class Visuals {

    var flag: Int = 0
        private set

    val animation = Animation()
    val primaryGraphic = Graphic()
    val secondaryGraphic = Graphic()
    val colourOverlay = ColourOverlay()
    val forceMovement = ForceMovement()
    val timeBar = TimeBar()
    val watch = Watch()
    val forceChat = ForceChat()
    val hits = Hits()

    fun flag(mask: Int) {
        flag = flag or mask
    }

    fun flagged(mask: Int): Boolean {
        return flag and mask != 0
    }

    open fun reset() {
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

}