package world.gregs.voidps.engine.entity.character.player.req

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

fun Player.hasRequest(target: Player, type: String): Boolean = getSet(type).contains(target.name)

fun Player.request(target: Player, type: String, request: (requester: Player, acceptor: Player) -> Unit): Boolean {
    val set = getSet(type)
    set.add(target.name)

    val targetSet = target.getSet(type)
    if (targetSet.contains(name)) {
        request.invoke(target, this)
        set.remove(target.name)
        targetSet.remove(name)
        return false
    }
    return true
}

fun Player.removeRequest(target: Player, type: String): Boolean = getSet(type).remove(target.name)

private fun Player.getSet(type: String) = getOrPut<MutableSet<String>>("${type}_map") { mutableSetOf() }
