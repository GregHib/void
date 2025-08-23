package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.EventProcessor

/**
 * Timer base events
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Timer(
    vararg val ids: String,
)

object TimerSchema : EventProcessor.SchemaProvider {
    private val entities = setOf(
        Player::class.simpleName, NPC::class.simpleName, Character::class.simpleName, FloorItem::class.simpleName, GameObject::class.simpleName, World::class.simpleName,
    )

    override fun param(param: ClassName): String {
        if (entities.contains(param.simpleName)) {
            return "it"
        }
        return super.param(param)
    }

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>) = when (extension) {
        "TimerStart" -> listOf(
            params.key("timer_start"),
            EventField.StringList("ids"),
            // Npc ids aren't needed as timer ids should be unique to npcs anyway
        )
        "TimerTick" -> listOf(
            params.key("timer_tick"),
            EventField.StringList("ids"),
        )
        "TimerStop" -> listOf(
            params.key("timer_stop"),
            EventField.StringList("ids"),
        )
        else -> emptyList()
    }
}
