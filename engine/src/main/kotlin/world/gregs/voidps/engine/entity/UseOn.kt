package world.gregs.voidps.engine.entity

/**
 * Interaction Interface:Component [id] used [on] an entity
 * Note: [id] on any target is permitted but any [id] is not, e.g. spell on any npc
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UseOn(
    val id: String,
    val on: String = "",
)
