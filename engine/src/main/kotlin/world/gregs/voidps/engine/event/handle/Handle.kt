package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import world.gregs.voidps.engine.event.EventProcessor

/**
 * Generic way of handling Event's
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Handle(
    vararg val args: String
)

object HandleSchema : EventProcessor.SchemaProvider {

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>): List<EventField> {
        return List((data["args"] as List<*>).size) { EventField.ListIndex("args", it) }
    }

}