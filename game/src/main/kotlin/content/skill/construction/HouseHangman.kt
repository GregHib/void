package content.skill.construction

import content.entity.effect.transform
import content.entity.player.dialogue.type.statement
import content.skill.construction.HouseGamesRoom.Companion.offerPrize
import content.skill.construction.HouseGamesRoom.Companion.winPrize
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.random

/**
 * Games room hangman: guess the word one letter at a time, nine wrong letters and the mannequin is complete.
 * The gallows is a npc standing in place of the game which is built up a stage for each wrong letter.
 * Clicking a letter guesses it straight away, the guess button switches to guessing the missing letters all at once.
 */
class HouseHangman : Script {
    init {
        objectOperate("Activate", "hangman_game") { (target) ->
            if (get("house_build_mode", false)) {
                message("You cannot activate the game while in building mode.", ChatType.Game)
                return@objectOperate
            }
            val gallows = NPCs.add(STAGES.first(), target.tile)
            gallows["hangman_object"] = target.id
            set("hangman_npc", gallows)
            begin(gallows)
            // Npcs spawn at the start of the next tick, so the game stays until then rather than leaving a gap
            delay(1)
            target.replace("invisible_seat", collision = false)
            offerPrize(GAME)
        }

        npcOperate("Guess-letter", STAGES.joinToString(",")) { (target) ->
            if (target != gallows) {
                message("That isn't your game.") // TODO proper message
                return@npcOperate
            }
            if (!contains("hangman_word")) {
                begin(target)
                offerPrize(GAME)
                return@npcOperate
            }
            open("poh_hangman")
        }

        npcOperate("Reset", STAGES.last()) { (target) ->
            if (target != gallows) {
                message("That isn't your game.") // TODO proper message
                return@npcOperate
            }
            begin(target)
            offerPrize(GAME)
        }

        npcOperate("Banish", STAGES.joinToString(",")) { (target) ->
            if (target != gallows) {
                message("That isn't your game.") // TODO proper message
                return@npcOperate
            }
            despawnHangman()
        }

        interfaceOpened("poh_hangman") { id ->
            clear("hangman_guessing")
            set("hangman_selected", "")
            interfaces.sendText(id, "word", display())
            interfaces.sendText(id, "selected", "")
            interfaces.sendVisibility(id, "guessing", false)
        }

        interfaceClosed("poh_hangman") {
            clear("hangman_guessing")
            clear("hangman_selected")
        }

        interfaceOption("Select", "poh_hangman:*") {
            val letter = it.component.singleOrNull()?.uppercaseChar() ?: return@interfaceOption
            if (get("hangman_guessing", false)) {
                select(letter)
            } else {
                guessLetter(letter)
            }
        }

        interfaceOption("Guess", "poh_hangman:guess") {
            if (!get("hangman_guessing", false)) {
                set("hangman_guessing", true)
                set("hangman_selected", "")
                interfaces.sendText("poh_hangman", "selected", "")
                interfaces.sendVisibility("poh_hangman", "guessing", true)
                return@interfaceOption
            }
            val selected = get("hangman_selected", "")
            if (selected.isNotEmpty()) {
                guessWord(selected)
            }
        }
    }

    /**
     * Starts a new game with a new word
     */
    private fun Player.begin(gallows: NPC) {
        val words = Tables.stringList("hangman_words.all.words")
        set("hangman_word", words[random.nextInt(words.size)])
        set("hangman_guessed", "")
        set("hangman_wrong", 0)
        if (gallows.transformId != STAGES.first()) {
            gallows.transform(STAGES.first())
        }
        message("Hangman word: ${display()}", ChatType.Game)
        message("You activate the hangman game.", ChatType.Filter)
    }

    /**
     * Adds a [letter] to the final guess
     */
    private suspend fun Player.select(letter: Char) {
        val selected = get("hangman_selected", "")
        if (selected.length >= MAX_SELECTED) {
            message("You can only guess five letters.", ChatType.Broadcast)
            statement("You can only guess five letters.")
            return
        }
        areaSound("poh_select", tile, radius = 5)
        set("hangman_selected", selected + letter)
        interfaces.sendText("poh_hangman", "selected", selected + letter)
    }

    private suspend fun Player.guessLetter(letter: Char) {
        val word: String = get("hangman_word") ?: return
        val gallows = gallows ?: return
        say(letter.toString())
        sound("poh_select")
        close("poh_hangman")
        delay(1)
        val wrong = get("hangman_wrong", 0) + if (letter in word) 0 else 1
        set("hangman_wrong", wrong)
        val guessed = get("hangman_guessed", "") + letter
        set("hangman_guessed", guessed)
        if (wrong >= MAX_WRONG) {
            gallows.transform(STAGES.last())
            gallows.anim("hangman_hangs")
            gallows.say("Game over")
            jingle("burthorpe_games_room_loss")
            clear("hangman_word")
            return
        }
        gallows.transform(STAGES[wrong])
        message("Hangman word: ${display()}", ChatType.Game)
        gallows.say(display())
        if (word.all { it in guessed }) {
            win(gallows, word)
        }
    }

    /**
     * Guesses the [letters] which are missing from the word, in any order
     */
    private suspend fun Player.guessWord(letters: String) {
        val word: String = get("hangman_word") ?: return
        val gallows = gallows ?: return
        say("Guess: $letters")
        close("poh_hangman")
        delay(1)
        val missing = word.filter { it !in get("hangman_guessed", "") }.toSet()
        if (letters.toSet() == missing) {
            win(gallows, word)
        } else {
            areaSound("poh_wrong", tile, radius = 5)
            gallows.say("$name guessed wrongly.")
        }
    }

    private fun Player.win(gallows: NPC, word: String) {
        val text = "$word! $name is the winner!"
        message("Hangman word: $text", ChatType.Game)
        gallows.say(text)
        jingle("burthorpe_games_room_victory")
        clear("hangman_word")
        winPrize(GAME)
    }

    /**
     * The word with the letters which haven't been guessed yet hidden
     */
    private fun Player.display(): String {
        val word: String = get("hangman_word") ?: return ""
        val guessed = get("hangman_guessed", "")
        return word.map { if (it in guessed) it else '_' }.joinToString("")
    }

    companion object {
        private const val GAME = "hangman"
        private const val MAX_SELECTED = 5
        private const val MAX_WRONG = 9

        // Gallows npcs from empty to the complete mannequin
        private val STAGES = (3944..3953).map { it.toString() }

        private val Player.gallows: NPC?
            get() = get<NPC>("hangman_npc")?.takeIf { NPCs.indexed(it.index) == it }

        /**
         * Removes the gallows and ends the game
         */
        fun Player.despawnHangman() {
            val npc: NPC = remove("hangman_npc") ?: return
            NPCs.remove(npc)
            val id: String = npc["hangman_object", "hangman_game"]
            GameObjects.findOrNull(npc.tile, "invisible_seat")?.replace(id)
            clear("hangman_word")
            clear("hangman_guessed")
            clear("hangman_selected")
            clear("hangman_guessing")
            clear("hangman_wrong")
        }
    }
}
