package world.gregs.voidps.engine.event.handle

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class VarSet(
    vararg val ids: String,
    val npc: String = "*",
    val fromInt: Int = -1,
    val fromStr: String = "",
    val fromNull: Boolean = false,
    val toInt: Int = -1,
    val toStr: String = "",
    val toNull: Boolean = false,
)