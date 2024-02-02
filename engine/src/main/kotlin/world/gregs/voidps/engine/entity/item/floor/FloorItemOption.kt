package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetFloorItemContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class FloorItemOption(
    override val character: Character,
    override val target: FloorItem,
    val option: String
) : Interaction(), TargetFloorItemContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun objectApproach(option: String, item: String, block: suspend FloorItemOption.() -> Unit) {
    on<FloorItemOption>({ approach && wildcardEquals(item, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
        block.invoke(this)
    }
}

fun objectOperate(option: String, item: String, block: suspend FloorItemOption.() -> Unit) {
    on<FloorItemOption>({ operate && wildcardEquals(item, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
        block.invoke(this)
    }
}