package rs.dusk.world.activity.bank

import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player

object Bank {
    fun sendLastDeposit(player: Player) {
        val deposit = player["last_deposit", 0]
        if(deposit != 0) {
            player.interfaceOptions.set("bank_side", "container", 3, "Deposit-${deposit}")
            player.interfaceOptions.send("bank_side", "container")
        }
    }
}

val Player.bank: Container
    get() = container("bank")