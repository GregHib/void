package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.Instruction

data class Bot(val player: Player) : Entity by player {

    val botEvents = Events(this)

    var step: Instruction? = null

}