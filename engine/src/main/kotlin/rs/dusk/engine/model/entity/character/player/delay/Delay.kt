package rs.dusk.engine.model.entity.character.player.delay

sealed class Delay(val ticks: Int) {
    object Eat : Delay(3)
    object Drink : Delay(3)
    object ComboFood : Delay(3)
    object Doors : Delay(3)
}