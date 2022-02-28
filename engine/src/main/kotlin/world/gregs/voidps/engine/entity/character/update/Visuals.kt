package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.*

interface Visuals {
    fun flag(mask: Int)
    fun flagged(mask: Int): Boolean
    fun reset(character: Character)

    val animation: Animation
    val primaryGraphic: Graphic
    val secondaryGraphic: Graphic
    val colourOverlay: ColourOverlay
    val forceMovement: ForceMovement
    val timeBar: TimeBar
    val watch: Watch
    val forceChat: ForceChat
    val hits: Hits

}