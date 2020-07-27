package rs.dusk.engine.action

sealed class Suspension {
    data class Interface(val id: Int) : Suspension()
    object Tick : Suspension()
    object StringEntry : Suspension()
    object IntEntry : Suspension()
}