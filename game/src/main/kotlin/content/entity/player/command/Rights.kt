package content.entity.player.command

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.commandSuggestion
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject

@Script
class Rights {

    val accounts: AccountDefinitions by inject()
    init {
        playerSpawn { player ->
            if (player.name == Settings.getOrNull("development.admin.name") && player.rights != PlayerRights.Admin) {
                player.rights = PlayerRights.Admin
                player.message("Rights set to Admin. Please re-log to activate.")
            }
        }

        val rights = PlayerRights.entries.map { it.name }.toSet()
        adminCommand("rights", stringArg("player-name", autofill = accounts.displayNames.keys), stringArg("rights-name", autofill = rights), desc = "set the rights for another player", handler = ::grant)
        commandSuggestion("rights", "promote")
    }

    fun grant(player: Player, args: List<String>) {
        val right = args[1]
        val rights: PlayerRights
        try {
            rights = PlayerRights.valueOf(right.toSentenceCase())
        } catch (e: IllegalArgumentException) {
            player.message("No rights found with the name: '${right.toSentenceCase()}'.")
            return
        }
        val target = get<Players>().firstOrNull { it.name.equals(args[0], true) }
        if (target == null) {
            player.message("Unable to find player '${args[0]}' online.", ChatType.Console)
            return
        }
        target.rights = rights
        player.message("${player.name} rights set to $rights.")
        target.message("${player.name} granted you $rights rights. Please re-log to activate.")
    }
}
