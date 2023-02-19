package world.gregs.voidps.world.community.friend

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.updateFriend
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSecondRemaining
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.encode.Friend
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.stringEntry
import java.util.concurrent.TimeUnit

val players: Players by inject()

on<Command>({ prefix == "rename" }) { player: Player ->
    val seconds = player.getVar("rename_delay", 0)
    if (seconds > epochSeconds() && !player.isAdmin()) {
        player.message("You've already changed your name this month.")
        val remaining = epochSecondRemaining(seconds).toLong()
        val days = TimeUnit.SECONDS.toDays(remaining)
        val hours = TimeUnit.SECONDS.toHours(remaining).rem(24)
        player.message("You can change your name again in $days ${"day".plural(days)} and $hours ${"hour".plural(hours)}.")
        return@on
    }
    val toName = stringEntry("Enter a new name")
    if (toName.length !in 1..12) {
        player.message("Name too long, a username must be less than 12 characters.")
        return@on
    }
    val choice = choice(
        title = "Change your name to '$toName'?",
        text = """
            Yes, call me $toName
            No, I like my current name
        """
    )
    if (choice == 1) {
        val previous = player.name
        player.name = toName
        players
            .filter { it.friend(player) }
            .forEach { friend ->
                friend.updateFriend(Friend(toName, previous, renamed = true, world = World.id, worldName = World.name))
            }
        player.message("Your name has been successfully changed to '$toName'.")
        player.message("You can change your name again in 30 days.")
        player.setVar("rename_delay", epochSeconds() + TimeUnit.DAYS.toSeconds(30).toInt())
    }
}