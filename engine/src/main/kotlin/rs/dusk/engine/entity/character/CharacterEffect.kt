package rs.dusk.engine.entity.character

import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player

abstract class CharacterEffect(type: String) : Effect(type) {

    override fun onStart(character: Character) {
        super.onStart(character)
        if (character is Player) {
            onPlayerStart(character)
        } else if (character is NPC) {
            onNPCStart(character)
        }
    }

    override fun onFinish(character: Character) {
        super.onFinish(character)
        if (character is Player) {
            onPlayerFinish(character)
        } else if (character is NPC) {
            onNPCFinish(character)
        }
    }

    open fun onPlayerStart(player: Player) {}

    open fun onPlayerFinish(player: Player) {}

    open fun onNPCStart(npc: NPC) {}

    open fun onNPCFinish(npc: NPC) {}

}