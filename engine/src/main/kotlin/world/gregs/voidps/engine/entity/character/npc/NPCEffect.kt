package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Effect

abstract class NPCEffect(type: String) : Effect(type) {
    override fun immune(character: Character): Boolean {
        return character !is NPC
    }

    open fun onStart(player: NPC) {
    }

    open fun onFinish(player: NPC) {
    }

    override fun onStart(character: Character) {
        super.onFinish(character)
        onStart(character as NPC)
    }

    override fun onFinish(character: Character) {
        super.onFinish(character)
        onFinish(character as NPC)
    }
}