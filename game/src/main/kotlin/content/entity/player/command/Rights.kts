package content.entity.player.command

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.get

playerSpawn { player ->
    if (player.name == Settings.getOrNull("development.admin.name") && player.rights != PlayerRights.Admin) {
        player.rights = PlayerRights.Admin
        player.message("Rights set to Admin. Please re-log to activate.")
    }
}

adminCommand("rights (player-name) (rights-name)", "set the rights for another player ${PlayerRights.entries.joinToString(",", "(", ")")}") {
    val right = content.split(" ").last()
    val rights: PlayerRights
    try {
        rights = PlayerRights.valueOf(right.toSentenceCase())
    } catch (e: IllegalArgumentException) {
        player.message("No rights found with the name: '${right.toSentenceCase()}'.")
        return@adminCommand
    }
    val username = content.removeSuffix(" $right")
    val target = get<Players>().get(username)
    if (target == null) {
        player.message("Unable to find player '$username'.")
    } else {
        target.rights = rights
        player.message("${player.name} rights set to $rights.")
        target.message("${player.name} granted you $rights rights. Please re-log to activate.")
    }
}