package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetObjectContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.suspend.arriveDelay

data class ObjectOption(
    override val character: Character,
    override val target: GameObject,
    val def: ObjectDefinition,
    val option: String
) : Interaction(), TargetObjectContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun objectApproach(option: String, vararg objects: String = arrayOf("*"), block: suspend ObjectOption.() -> Unit) {
    for (id in objects) {
        on<ObjectOption>({ approach && wildcardEquals(id, target.id) && wildcardEquals(option, this.option) }) { _: Player ->
            block.invoke(this)
        }
    }
}

fun objectOperate(option: String, vararg objects: String = arrayOf("*"), arrive: Boolean = true, priority: Priority = Priority.MEDIUM, block: suspend ObjectOption.() -> Unit) {
    for (id in objects) {
        on<ObjectOption>({ operate && wildcardEquals(id, target.id) && wildcardEquals(option, this.option) }, priority) { _: Player ->
            if (arrive) {
                arriveDelay()
            }
            block.invoke(this)
        }
    }
}