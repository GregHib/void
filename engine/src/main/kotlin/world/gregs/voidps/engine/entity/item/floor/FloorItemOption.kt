package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetFloorItemContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class FloorItemOption(
    override val character: Character,
    override val target: FloorItem,
    val option: String
) : Interaction(), TargetFloorItemContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

@JvmName("floorItemApproachPlayer")
fun floorItemApproach(filter: FloorItemOption.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend FloorItemOption.(Player) -> Unit) {
    on<FloorItemOption>({ approach && filter(this, it) }, priority, block)
}

@JvmName("floorItemOperatePlayer")
fun floorItemOperate(filter: FloorItemOption.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend FloorItemOption.(Player) -> Unit) {
    on<FloorItemOption>({ operate && filter(this, it) }, priority, block)
}

@JvmName("floorItemApproachNPC")
fun floorItemApproach(filter: FloorItemOption.(NPC) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend FloorItemOption.(NPC) -> Unit) {
    on<FloorItemOption>({ approach && filter(this, it) }, priority, block)
}

@JvmName("floorItemOperateNPC")
fun floorItemOperate(filter: FloorItemOption.(NPC) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend FloorItemOption.(NPC) -> Unit) {
    on<FloorItemOption>({ operate && filter(this, it) }, priority, block)
}

fun floorItemApproach(option: String, item: String, block: suspend FloorItemOption.() -> Unit) {
    on<FloorItemOption>({ approach && wildcardEquals(item, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
        block.invoke(this)
    }
}

fun floorItemOperate(option: String, item: String, block: suspend FloorItemOption.() -> Unit) {
    on<FloorItemOption>({ operate && wildcardEquals(item, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
        block.invoke(this)
    }
}