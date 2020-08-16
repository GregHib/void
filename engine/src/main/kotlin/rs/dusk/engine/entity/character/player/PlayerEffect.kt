package rs.dusk.engine.entity.character.player

import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.Effect

abstract class PlayerEffect(name: String) : Effect(name) {

    override fun immune(character: Character): Boolean {
        return character !is Player
    }

    open fun onStart(player: Player) {
    }

    open fun onFinish(player: Player) {
    }

    override fun onStart(character: Character) {
        super.onStart(character)
        onStart(character as Player)
    }

    override fun onFinish(character: Character) {
        super.onFinish(character)
        onFinish(character as Player)
    }
}