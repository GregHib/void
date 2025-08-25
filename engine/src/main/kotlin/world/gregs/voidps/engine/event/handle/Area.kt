package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventProcessor

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Area(
    vararg val ids: String,
    val tags: Array<String> = [],
)

object AreaSchema : EventProcessor.SchemaProvider {

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>): List<EventField> = when (extension) {
        "AreaEntered" -> listOf(
            EventField.StaticValue("area_enter"),
            EventField.StringList("ids"),
            EventField.StringList("tags"),
        )
        "AreaExited" -> listOf(
            EventField.StaticValue("area_exit"),
            EventField.StringList("ids"),
            EventField.StringList("tags"),
        )
        else -> emptyList()
    }

}