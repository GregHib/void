package content.social.friend

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.nameEntry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.login.protocol.encode.Friend
import java.util.concurrent.TimeUnit

class NameChange : Script {

    init {
        modCommand("rename", desc = "Change your display name (login stays the same)", handler = ::rename)
    }

    fun rename(player: Player, args: List<String>) {
        val remaining = player.remaining("rename_delay", epochSeconds()).toLong()
        if (remaining > 0 && !player.isAdmin()) {
            player.message("You've already changed your name this month.")
            val days = TimeUnit.SECONDS.toDays(remaining)
            val hours = TimeUnit.SECONDS.toHours(remaining).rem(24)
            player.message("You can change your name again in $days ${"day".plural(days)} and $hours ${"hour".plural(hours)}.")
            return
        }
        player.strongQueue("rename") {
            val toName = player.nameEntry("Enter a new name")
            if (!DisplayNames.valid(toName)) {
                player.message("Invalid name, a username must be 1-12 letters, numbers or spaces.")
                return@strongQueue
            }
            if (player.nameTaken(toName)) {
                player.message("That name is already taken, please try another.")
                return@strongQueue
            }
            player.choice("Change your name to '$toName'?") {
                option("Yes, call me $toName") {
                    player.rename(toName)
                    player.message("Your name has been successfully changed to '$toName'.")
                    player.message("You can change your name again in 30 days.")
                    player.start("rename_delay", TimeUnit.DAYS.toSeconds(30).toInt(), epochSeconds())
                }
                option("No, I like my current name")
            }
        }
    }
}

/**
 * Whether [name] is already in use as another account's display name
 */
fun Player.nameTaken(name: String): Boolean {
    val definitions: AccountDefinitions = get()
    val existing = definitions.get(name) ?: return false
    return existing.accountName != accountName
}

/**
 * Changes the display name and notifies online friends of the new name
 */
fun Player.rename(toName: String) {
    val previous = name
    name = toName
    Players
        .filter { it.friend(this) }
        .forEach { friend ->
            friend.updateFriend(Friend(toName, previous, renamed = true, world = Settings.world, worldName = Settings.worldName))
        }
}
