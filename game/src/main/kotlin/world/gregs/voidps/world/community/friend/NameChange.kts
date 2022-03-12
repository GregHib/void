import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.updateFriend
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.remaining
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.TICKS
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.network.encode.Friend
import world.gregs.voidps.world.community.friend.friend
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.stringEntry
import java.util.concurrent.TimeUnit

val players: Players by inject()

on<Command>({ prefix == "rename" }) { player: Player ->
    if (player.hasEffect("rename_delay") && !player.isAdmin()) {
        player.message("You've already changed your name this month.")
        val ticks = player.remaining("rename_delay")
        val days = TICKS.toDays(ticks)
        val hours = TICKS.toHours(ticks).rem(24)
        player.message("You can change your name again in $days ${"day".plural(days)} and $hours ${"hour".plural(hours)}.")
        return@on
    }
    player.dialogue {
        val toName = stringEntry("Enter a new name")
        if (toName.length !in 1..12) {
            player.message("Name too long, a username must be less than 12 characters.")
            return@dialogue
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
            player.start("rename_delay", TimeUnit.DAYS.toTicks(30), persist = true)
        }
    }
}