package world.gregs.voidps.engine.entity

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Approach(
    val option: String,
    val id: String = "",
)
