import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.AddIgnore
import world.gregs.voidps.engine.entity.character.player.chat.DeleteIgnore
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.character.update.visual.player.previousName
import world.gregs.voidps.engine.entity.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.definition.config.AccountDefinition
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.capitalise
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.sendIgnoreList

val players: Players by inject()
val accounts: AccountDefinitions by inject()

val maxIgnores = 100

on<Registered> { player: Player ->
    player.sendIgnores()
}

on<AddIgnore> { player: Player ->
    val account = accounts.get(name)
    if (account == null) {
        player.message("Unable to find player with name '$name'.")
        return@on
    }

    if (player.name == name) {
        player.message("We all get irritated with ourselves sometimes, take a break!")
        return@on
    }

    if (player.friends.contains(account.accountName)) {
        return@on
    }

    if (player.ignores.size >= maxIgnores) {
        player.message("Your ignore list is full. Max of $maxIgnores.")
        return@on
    }

    if (player.ignores.contains(account.accountName)) {
        return@on
    }

    player.ignores.add(account.accountName)
    name.updateIgnore(player)
    player.sendIgnore(account)
}

on<DeleteIgnore> { player: Player ->
    val account = accounts.get(name.capitalise())
    if (account == null || !player.ignores.contains(account.accountName)) {
        player.message("Unable to find player with name '$name'.")
        return@on
    }

    player.ignores.remove(account.displayName)
    name.updateIgnore(player)
}


fun Player.sendIgnores() {
    client?.sendIgnoreList(ignores.mapNotNull { account ->
        val (display, previous) = accounts.getByAccount(account) ?: return@mapNotNull null
        display to previous
    })
}

fun String.updateIgnore(player: Player) {
    players.get(this)?.sendIgnore(player)
}

fun Player.sendIgnore(account: AccountDefinition) {
    client?.sendIgnoreList(listOf(account.displayName to account.previousName))
}

fun Player.sendIgnore(player: Player) {
    client?.sendIgnoreList(listOf(player.name to player.previousName))
}