package world.gregs.voidps.engine.action

sealed class Suspension {
    data class Interface(val id: String) : Suspension()
    object Movement : Suspension()
    object Tick : Suspension()
    object Infinite : Suspension()
    object External : Suspension()
}