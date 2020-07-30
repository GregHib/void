package rs.dusk.engine.entity.character.player.delay

sealed class Delay(val ticks: Int) {
    object Eat : Delay(3)
    object Drink : Delay(3)
    object ComboFood : Delay(3)
    object DoorSlam : Delay(10)
}