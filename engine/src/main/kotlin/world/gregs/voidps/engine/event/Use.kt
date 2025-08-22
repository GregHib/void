package world.gregs.voidps.engine.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Use(
    val id: String = "",
    val ids: Array<String> = [],
    val on: Array<String> = [],
    val option: String = "",
    val component: String = "",
    val approach: Boolean = false,
    val arrive: Boolean = true,
)