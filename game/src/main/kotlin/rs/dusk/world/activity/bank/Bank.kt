package rs.dusk.world.activity.bank

import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.player.Player

object Bank

val Player.bank: Container
    get() = container("bank")