package world.gregs.voidps.engine.timer

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Timer(
    val id: String
) {
    companion object {
        const val CANCEL = -1
        const val CONTINUE = -2
    }
}

