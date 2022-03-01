package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.*

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

    open fun reset(character: Character) {
        flag = 0
    }

}