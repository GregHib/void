package world.gregs.voidps.engine.entity

/**
 * Interaction [item] used [on] an entity
 * Note: any item used [on] is permitted but any [item] is not, e.g. use on cow or fire
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ItemOn(
    val item: String = "",
    val on: String,
)
