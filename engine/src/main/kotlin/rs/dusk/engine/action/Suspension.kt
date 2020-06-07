package rs.dusk.engine.action

sealed class Suspension {
    object Interfaces : Suspension()
    object Tick : Suspension()
    object StringEntry : Suspension()
    object IntEntry : Suspension()
}