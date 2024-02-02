package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on

object Unregistered : Event

fun playerDespawn(block: suspend (Player) -> Unit) {
    on<Unregistered> { player: Player ->
        block.invoke(player)
    }
}

fun npcDespawn(block: suspend (NPC) -> Unit) {
    on<Unregistered> { npc: NPC ->
        block.invoke(npc)
    }
}

fun characterDespawn(block: suspend (Character) -> Unit) {
    on<Unregistered> { character: Character ->
        block.invoke(character)
    }
}