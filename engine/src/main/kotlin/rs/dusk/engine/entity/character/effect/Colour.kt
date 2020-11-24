package rs.dusk.engine.entity.character.effect

import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.CharacterEffect
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.colourOverlay
import rs.dusk.engine.entity.character.update.visual.flagColourOverlay

data class Colour(val colour: Int, val delay: Int, val duration: Int) : CharacterEffect("colour") {

    override fun onPlayerStart(player: Player) {
        set(player, colour, delay, duration)
        player.flagColourOverlay()
    }

    override fun onNPCStart(npc: NPC) {
        set(npc, colour, delay, duration)
        npc.flagColourOverlay()
    }

    override fun onStart(character: Character) {
        super.onStart(character)
        val ticks = (delay + duration) / 30L
        removeSelf(character, ticks)
    }

    override fun onPlayerFinish(player: Player) {
        set(player, 0, 0, 0)
        player.flagColourOverlay()
    }

    override fun onNPCFinish(npc: NPC) {
        set(npc, 0, 0, 0)
        npc.flagColourOverlay()
    }

    private fun set(player: Player, colour: Int, delay: Int, duration: Int) = player.colourOverlay.apply {
        this.delay = delay
        this.duration = duration
        this.colour = colour
    }

    private fun set(npc: NPC, colour: Int, delay: Int, duration: Int) = npc.colourOverlay.apply {
        this.delay = delay
        this.duration = duration
        this.colour = colour
    }
}