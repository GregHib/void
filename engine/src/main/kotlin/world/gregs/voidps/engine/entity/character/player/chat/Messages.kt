package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import java.util.*

fun Character.cantReach() = message("I can't reach that.", ChatType.Engine)

fun Character.inventoryFull() = notEnough("inventory space")

fun Character.noInterest() = message("Nothing interesting happens.", ChatType.Engine)

fun Character.notEnough(thing: String) = message("You don't have enough ${thing}.")

private class FixedSizeQueue<E>(private val capacity: Int) : LinkedList<E>() {
    override fun add(element: E): Boolean {
        if (size >= capacity) {
            removeFirst()
        }
        return super.add(element)
    }
}

val Player.messages: MutableList<String>
    get() = getOrPut("messages") { FixedSizeQueue(100) }