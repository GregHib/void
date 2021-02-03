package world.gregs.voidps.engine.entity.character.player

import kotlinx.serialization.Serializable
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Effect

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