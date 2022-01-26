package world.gregs.voidps.engine.event

/**
 * The order of execution, the highest first, the lowest last.
 */
enum class Priority {
    LOWEST,
    LOWER,
    LOW,
    LOWISH,
    MEDIUM,
    HIGHISH,
    HIGH,
    HIGHER,
    HIGHEST
}