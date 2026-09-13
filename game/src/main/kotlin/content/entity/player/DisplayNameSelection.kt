package content.entity.player

import content.entity.player.dialogue.type.nameEntry
import content.entity.player.dialogue.type.statement
import content.social.friend.nameTaken
import content.social.friend.rename
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

/**
 * Accounts registered with an email address are given a placeholder display name until the player picks one on first login
 */
suspend fun Player.chooseDisplayName() {
    while (true) {
        val chosen = nameEntry("Choose a display name")
        if (!DisplayNames.valid(chosen)) {
            statement("Display names must be 1-12 characters long and may only contain letters, numbers and single spaces.")
            continue
        }
        if (nameTaken(chosen)) {
            statement("The name '$chosen' is already taken. Please choose another.")
            continue
        }
        if (chosen != name) {
            rename(chosen)
        }
        clear("choose_name")
        return
    }
}
