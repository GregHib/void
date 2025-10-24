package world.gregs.voidps.engine.timer

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Key(
    val id: String
)
