package rs.dusk.engine.action

sealed class Suspension {
    data class Interface(val id: String) : Suspension()
    object Tick : Suspension()
    object Follow : Suspension()
    object Infinite : Suspension()
}