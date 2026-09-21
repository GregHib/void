package content.entity.player.modal

import content.social.friend.nameTaken
import content.social.friend.rename
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.pauseString
import world.gregs.voidps.network.login.AccountNames
import world.gregs.voidps.network.login.registration.RegistrationValidator

/**
 * The "Character Name" panel at the end of character creation, where accounts registered with an email address pick their display name.
 * The client keeps the typed name; submitting runs a client script which sends it back as a string entry.
 */
class CharacterName : Script {

    init {
        interfaceOption(id = "character_creation:continue") {
            submit(this, 0)
        }

        interfaceOption(id = "character_creation:suggestion_*") {
            val index = it.component.removePrefix("suggestion_").toInt()
            submit(this, index + 1)
        }

        interfaceOption(id = "character_creation:more_suggestions") {
            suggest(this, 1)
        }

        interfaceOption(id = "character_creation:previous_suggestions") {
            suggest(this, -1)
        }
    }

    private fun submit(player: Player, index: Int) {
        if (!player.contains("choose_name")) {
            return
        }
        player.strongQueue("character_name") {
            player.sendScript("character_name_submit", index, 1, 1)
            val entered = player.pauseString()
            player.chooseCharacterName(entered)
        }
    }

    companion object {
        private const val SUGGESTIONS = 6

        /**
         * Switches the appearance panels for the name panel
         */
        fun Player.openCharacterName() {
            sendScript("character_name_open")
            // Registered accounts log in with an email; show the name derived from it rather than the login
            val base = if (AccountNames.isEmail(accountName)) DisplayNames.sanitise(RegistrationValidator.localPart(accountName)) else name
            interfaces.sendText("character_creation", "name_message", "'$base' is not available.")
            interfaces.sendVisibility("character_creation", "name_rules", false)
            interfaces.sendVisibility("character_creation", "name_suggestions", true)
            this["character_name_page"] = -1
            this["character_name_pages"] = mutableListOf<List<String>>()
            this["character_name_base"] = base
            suggest(this, 1)
        }

        fun Player.chooseCharacterName(entered: String) {
            val chosen = normalise(entered)
            if (!DisplayNames.valid(chosen)) {
                reject(this, "Please enter a valid character name.")
                return
            }
            if (nameTaken(chosen)) {
                reject(this, "'$chosen' is not available.")
                this["character_name_base"] = chosen
                this["character_name_pages"] = mutableListOf<List<String>>()
                this["character_name_page"] = -1
                suggest(this, 1)
                return
            }
            if (chosen != name) {
                rename(chosen)
            }
            clear("choose_name")
            open(interfaces.gameFrame)
        }

        private fun reject(player: Player, message: String) {
            player.interfaces.sendText("character_creation", "name_message", message)
            player["character_name_locked"] = 0
            player.sendVariable("character_name_locked")
        }

        /**
         * Shows the next or previous page of name suggestions, generating a new page when needed
         */
        private fun suggest(player: Player, direction: Int) {
            val pages: MutableList<List<String>> = player.getOrPut("character_name_pages") { mutableListOf() }
            var page = player["character_name_page", -1] + direction
            if (page < 0) {
                return
            }
            if (page >= pages.size) {
                val base: String = player["character_name_base", player.name]
                pages.add(DisplayNames.suggestions(base, SUGGESTIONS, taken = { player.nameTaken(it) }))
                page = pages.lastIndex
            }
            player["character_name_page"] = page
            player.interfaces.sendVisibility("character_creation", "previous_suggestions", page > 0)
            val names = pages[page]
            for (index in 0 until SUGGESTIONS) {
                player["character_name_suggestion_$index"] = names.getOrNull(index) ?: ""
            }
            // The client queues client string changes until after it has read all packets but runs scripts immediately,
            // so rendering in the same tick would show the previous strings
            World.queue("character_name_suggestions_${player.accountName}", 1) {
                player.sendScript("character_name_suggestions", 1)
            }
        }

        /**
         * The client allows underscores and dashes which display names show as spaces
         */
        private fun normalise(entered: String): String = entered.replace('_', ' ').replace('-', ' ').replace(Regex(" +"), " ").trim()
    }
}
