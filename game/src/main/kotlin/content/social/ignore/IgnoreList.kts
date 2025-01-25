package content.social.ignore

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ignore.ignoresAdd
import world.gregs.voidps.engine.entity.character.player.chat.ignore.ignoresDelete
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.encode.sendIgnoreList

val accounts: AccountDefinitions by inject()

val maxIgnores = 100

playerSpawn { player ->
    player.sendIgnores()
}

ignoresAdd(override = false) { player ->
    val account = accounts.get(name)
    if (account == null) {
        player.message("Unable to find player with name '$name'.")
        cancel()
        return@ignoresAdd
    }

    if (player.name == name) {
        player.message("We all get irritated with ourselves sometimes, take a break!")
        cancel()
        return@ignoresAdd
    }

    if (player.friends.contains(account.accountName)) {
        player.message("Please remove $name from your ignores list first.")
        cancel()
        return@ignoresAdd
    }

    if (player.ignores.size >= maxIgnores) {
        player.message("Your ignore list is full. Max of $maxIgnores.")
        cancel()
        return@ignoresAdd
    }

    if (player.ignores.contains(account.accountName)) {
        player.message("$name is already on your ignores list.")
        cancel()
        return@ignoresAdd
    }

    player.ignores.add(account.accountName)
    player.sendIgnore(account)
}

ignoresDelete(override = false) { player ->
    val accountName = player.ignores.firstOrNull {
        val account = accounts.getByAccount(it) ?: return@firstOrNull false
        name.equals(account.displayName, true) // This packet ignores case for some reason.
    }
    if (accountName == null) {
        player.message("Unable to find player with name '$name'.")
        cancel()
        return@ignoresDelete
    }

    val account = accounts.getByAccount(accountName)
    if (account == null || !player.ignores.contains(account.accountName)) {
        player.message("Unable to find player with name '$name'.")
        cancel()
        return@ignoresDelete
    }

    name = account.displayName
    player.ignores.remove(account.accountName)
}


fun Player.sendIgnores() {
    client?.sendIgnoreList(ignores.mapNotNull { account ->
        val (_, display, previous) = accounts.getByAccount(account) ?: return@mapNotNull null
        display to previous
    })
}

fun Player.sendIgnore(account: AccountDefinition) {
    client?.sendIgnoreList(listOf(account.displayName to account.previousName))
}