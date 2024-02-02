package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetPlayerContext
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class PlayerOption(
    override val character: Character,
    override val target: Player,
    val option: String
) : Interaction(), TargetPlayerContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun playerApproach(option: String, block: suspend PlayerOption.() -> Unit) {
    on<PlayerOption>({ approach && wildcardEquals(option, this.option) }) { _: Player ->
        block.invoke(this)
    }
}

fun playerOperate(option: String, block: suspend PlayerOption.() -> Unit) {
    on<PlayerOption>({ operate && wildcardEquals(option, this.option) }) { _: Player ->
        block.invoke(this)
    }
}