package content.social.friend

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.nameEntry
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.login.protocol.encode.Friend
import java.util.concurrent.TimeUnit

@Script
class NameChange {

    val players: Players by inject()

    init {
        modCommand("rename", desc = "rename your accounts display name (login stays the same)", handler = ::rename)
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
            val toName = nameEntry("Enter a new name")
            if (toName.length !in 1..12) {
                player.message("Name too long, a username must be less than 12 characters.")
                return@strongQueue
            }
            choice("Change your name to '$toName'?") {
                option("Yes, call me $toName") {
                    val previous = player.name
                    player.name = toName
                    players
                        .filter { it.friend(player) }
                        .forEach { friend ->
                            friend.updateFriend(Friend(toName, previous, renamed = true, world = Settings.world, worldName = Settings.worldName))
                        }
                    player.message("Your name has been successfully changed to '$toName'.")
                    player.message("You can change your name again in 30 days.")
                    player.start("rename_delay", TimeUnit.DAYS.toSeconds(30).toInt(), epochSeconds())
                }
                option("No, I like my current name")
            }
        }
    }
}
