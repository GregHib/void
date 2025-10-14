package world.gregs.voidps.engine.entity

/**
 * Don't call arriveDelay before an object or floor item interaction
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class NoDelay
