package world.gregs.voidps.engine.client.variable

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Variable(
    val key: String,
    val id: String = "",
)
