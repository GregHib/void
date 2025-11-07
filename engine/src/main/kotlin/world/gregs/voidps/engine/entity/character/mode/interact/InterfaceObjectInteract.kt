package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

data class InterfaceObjectInteract(
    override val target: GameObject,
    val id: String,
    val index: Int,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.onObject.containsKey("$id:*") || Operation.onObject.containsKey("$id:${target.def(player).stringId}") || Operation.onObject.containsKey("*:${target.def(player).stringId}")

    override fun hasApproach() = Approachable.onObject.containsKey("$id:*") || Approachable.onObject.containsKey("$id:${target.def(player).stringId}") || Approachable.onObject.containsKey("*:${target.def(player).stringId}")

    override fun operate() {
        invoke(Operation.onObject)
    }

    override fun approach() {
        invoke(Approachable.onObject)
    }

    private fun invoke(map: Map<String, List<suspend Player.(InterfaceObjectInteract) -> Unit>>) {
        Script.launch {
            for (block in map["$id:${target.def(player).stringId}"] ?: map["$id:*"] ?: map["*:${target.def(player).stringId}"] ?: return@launch) {
                block(player, this@InterfaceObjectInteract)
            }
        }
    }
}