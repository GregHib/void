package content.skill.construction

import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random

/**
 * Games room hangman: guess the word by choosing up to five letters at a time, nine wrong letters and the mannequin is complete.
 * The gallows is a npc beside the game which is built up a stage for each wrong letter, the player can come back to
 * guess more letters, reset the game once it's over or banish the gallows.
 */
class HouseHangman : Script {
    init {
        objectOperate("Activate", "hangman_game") { (target) ->
            val gallows = gallows ?: NPCs.add(STAGES.first(), target.tile).also { set("hangman_npc", it) }
            if (!contains("hangman_word")) {
                start(gallows)
            }
            open("poh_hangman")
        }

        npcOperate("Guess-letter", STAGES.joinToString(",")) { (target) ->
            if (target != gallows) {
                message("That isn't your game.") // TODO proper message
                return@npcOperate
            }
            if (!contains("hangman_word")) {
                start(target)
            }
            open("poh_hangman")
        }

        npcOperate("Reset", STAGES.last()) { (target) ->
            if (target != gallows) {
                message("That isn't your game.") // TODO proper message
                return@npcOperate
            }
            start(target)
            open("poh_hangman")
        }

        npcOperate("Banish", STAGES.joinToString(",")) { (target) ->
            if (target != gallows) {
                message("That isn't your game.") // TODO proper message
                return@npcOperate
            }
            despawnHangman()
        }

        interfaceOpened("poh_hangman") {
            refresh("Enter up to five letters in any order then click 'guess' again.") // TODO proper message
        }

        interfaceClosed("poh_hangman") {
            // Letters which haven't been guessed yet are dropped, the rest of the game is kept to carry on with later
            set("hangman_selected", "")
        }

        interfaceOption("Select", "poh_hangman:*") {
            val letter = it.component.singleOrNull()?.uppercaseChar() ?: return@interfaceOption
            val selected = get("hangman_selected", "")
            if (letter in selected || letter in get("hangman_guessed", "")) {
                return@interfaceOption
            }
            if (selected.length >= MAX_SELECTED) {
                refresh("You can only choose five letters at a time.") // TODO proper message
                return@interfaceOption
            }
            set("hangman_selected", selected + letter)
            refresh("Enter up to five letters in any order then click 'guess' again.") // TODO proper message
        }

        interfaceOption("Guess", "poh_hangman:guess") {
            val word: String = get("hangman_word") ?: return@interfaceOption
            val selected = get("hangman_selected", "")
            if (selected.isEmpty()) {
                return@interfaceOption
            }
            val guessed = get("hangman_guessed", "") + selected
            val wrong = get("hangman_wrong", 0) + selected.count { it !in word }
            set("hangman_guessed", guessed)
            set("hangman_selected", "")
            set("hangman_wrong", wrong)
            gallows?.transform(STAGES[wrong.coerceAtMost(MAX_WRONG)])
            when {
                word.all { it in guessed } -> finish("Well done, you guessed the word!") // TODO proper message
                wrong >= MAX_WRONG -> finish("You've been hanged! The word was ${word.lowercase()}.") // TODO proper message
                else -> refresh("${MAX_WRONG - wrong} wrong guesses left.") // TODO proper message
            }
        }
    }

    private suspend fun Player.finish(text: String) {
        refresh(text, reveal = true)
        message(text)
        clear("hangman_word")
        delay(4)
        close("poh_hangman")
    }

    private fun Player.start(gallows: NPC) {
        set("hangman_word", WORDS[random.nextInt(WORDS.size)])
        set("hangman_guessed", "")
        set("hangman_selected", "")
        set("hangman_wrong", 0)
        if (gallows.transformId != STAGES.first()) {
            gallows.transform(STAGES.first())
        }
    }

    private fun Player.refresh(text: String, reveal: Boolean = false) {
        val word: String = get("hangman_word") ?: return
        val guessed = get("hangman_guessed", "")
        interfaces.sendText("poh_hangman", "word", word.map { if (reveal || it in guessed) it else '_' }.joinToString(" "))
        interfaces.sendText("poh_hangman", "selected", get("hangman_selected", "").toList().joinToString(" "))
        interfaces.sendText("poh_hangman", "message", text)
    }

    companion object {
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
            clear("hangman_word")
            clear("hangman_guessed")
            clear("hangman_selected")
            clear("hangman_wrong")
        }

        private val WORDS = listOf(
            "RUNESCAPE", "WIZARD", "DRAGON", "CASTLE", "GOBLIN", "VARROCK", "KNIGHT", "SWORD", "ADVENTURE", "FALADOR",
            "TREASURE", "DUNGEON", "PICKAXE", "LUMBRIDGE", "MONSTER", "ARCHER", "CAMELOT", "PRAYER", "SCIMITAR", "HITPOINTS",
        )
    }
}
