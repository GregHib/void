import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.AddIgnore
import world.gregs.voidps.engine.entity.character.player.chat.DeleteIgnore
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.character.update.visual.player.previousName
import world.gregs.voidps.engine.entity.definition.AccountDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.capitalise
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.sendIgnoreList
import world.gregs.voidps.world.community.friend.friend
import world.gregs.voidps.world.community.friend.status

val players: Players by inject()
val accounts: AccountDefinitions by inject()

val maxIgnores = 100

on<Registered> { player: Player ->
    player.sendIgnores()
}

on<AddIgnore> { player: Player ->
    if (player.friend(name)) {
        return@on
    }

    if (player.ignores.size >= maxIgnores) {
        player.message("Your ignore list is full. Max of $maxIgnores.")
        return@on
    }

    if (player.ignores.contains(name)) {
        return@on
    }

    player.ignores.add(name)
    notifyIgnores(player)
    player.sendIgnore(name)
}

on<DeleteIgnore> { player: Player ->
    val name = name.capitalise()
    if (!player.ignores.contains(name)) {
        player.message("Unable to find player with name '$name'.")
        return@on
    }
    player.ignores.remove(name)
    notifyIgnores(player)
}

fun notifyIgnores(player: Player) {
    players.forEach {
        if (it.friend(player) && player.status != 2) {
            it.sendIgnore(player)
        }
    }
}

fun Player.sendIgnores() {
    client?.sendIgnoreList(ignores.mapNotNull { account ->
        val (display, previous) = accounts.get(account) ?: return@mapNotNull null
        display to previous
    })
}

fun Player.sendIgnore(name: String) {
    val account = accounts.get(name)
    if (account == null) {
        message("Unable to find player with name '$name'.")
        return
    }
    val (display, previous) = account
    client?.sendIgnoreList(listOf(display to previous))
}

fun Player.sendIgnore(player: Player) {
    client?.sendIgnoreList(listOf(player.name to player.previousName))
}