package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class ObjectOption<C : Character>(
    override val character: C,
    override val target: GameObject,
    val def: ObjectDefinition,
    val option: String,
) : TargetInteraction<C, GameObject>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${character.key}_${if (approach) "approach" else "operate"}_object"
        1 -> option
        2 -> def.stringId
        3 -> character.identifier
        else -> null
    }
}

fun objectApproach(option: String, vararg objects: String = arrayOf("*"), block: suspend ObjectOption<Player>.() -> Unit) {
    val handler: suspend ObjectOption<Player>.(Player) -> Unit = {
        block.invoke(this)
    }
    for (id in objects) {
        Events.handle("player_approach_object", option, id, "player", handler = handler)
    }
}

