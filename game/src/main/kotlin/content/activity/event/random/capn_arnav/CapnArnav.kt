package content.activity.event.random.capn_arnav

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.network.login.protocol.encode.interfaceText
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Cap'n Arnav's Chest random event: the old pirate whisks the player to his shark-circled island and
 * asks for help with the fiddly combination lock on his freshly dug-up chest. The lock (interface 185)
 * has three cylinders of items; rolling each with the arrows until the middle row matches the target
 * word and clicking Unlock opens the chest for a random event gift, and the exit portal sends the
 * player back. Five wrong combinations and Arnav clobbers them with his bottle instead.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Cap'n_Arnav's_Chest
 */
class CapnArnav : Script {

    init {
        RandomEvents.register("capn_arnav") { startEvent() }

        npcOperate("Talk-to", "capn_arnav") {
            if (get<String>("random_event") != "capn_arnav") {
                npc<Neutral>("capn_arnav", "Ah, I cannot be stoppin' to chat to ye now. There be treasure out there, and I wants it.")
                return@npcOperate
            }
            arnavTalk()
        }

        objectOperate("Open", "capn_arnav_chest") {
            if (get<String>("random_event") != "capn_arnav" || get("arnav_solved", false)) {
                return@objectOperate
            }
            openLock()
        }

        interfaceOption("Up", "capn_arnav_lock:up_*") {
            roll(it.component.removePrefix("up_").toInt(), 1)
        }

        interfaceOption("Down", "capn_arnav_lock:down_*") {
            roll(it.component.removePrefix("down_").toInt(), -1)
        }

        interfaceOption("Unlock", "capn_arnav_lock:unlock") {
            if (get<String>("random_event") != "capn_arnav") {
                return@interfaceOption
            }
            unlock()
        }

        objectOperate("Enter", "capn_arnav_exit_portal") {
            if (get<String>("random_event") != "capn_arnav") {
                return@objectOperate
            }
            if (!get("arnav_solved", false)) {
                npc<Angry>("capn_arnav", "Ye're not goin' anywhere till ye've opened me chest, matey!")
                return@objectOperate
            }
            clearState()
            anim("teleport_modern")
            sound("teleport")
            gfx("teleport_modern")
            delay(3)
            RandomEvents.complete(this)
            anim("teleport_land_modern")
            gfx("teleport_land_modern")
            sound("teleport_land")
        }
    }

    private suspend fun Player.startEvent() {
        if (!contains("arnav_target")) {
            // arnav_target is only set once the lock is opened; a relog mid-event resumes
            // with tries and the solved flag intact so the reward can't be re-earned.
            set("arnav_tries", 0)
            clear("arnav_solved")
        }
        arnavHerald()
        kidnap(ISLAND)
    }

    private suspend fun Player.arnavHerald() {
        val arnav = NPCs.addRandom("capn_arnav", tile.toCuboid(1), ticks = 25, owner = this)
            ?: NPCs.add("capn_arnav", tile, ticks = 25, owner = this)
        arnav.watch(this)
        val greetings = listOf(
            "Avast there, $name!",
            "Ahoy, $name!",
            "I got treasure here, $name!",
            "Will ye help an old pirate, $name?",
        )
        arnav.say(greetings.random(random))
        delay(2)
    }

    private suspend fun Player.arnavTalk() {
        if (get("arnav_solved", false)) {
            npc<Happy>("capn_arnav", "Ah, well done matey, that's the right combination. Now be off with ye, through the portal!")
            return
        }
        npc<Happy>("capn_arnav", "Ah, hello there, ${if (male) "laddie" else "lassie"}! I've just dug up an old treasure chest of mine.")
        npc<Sad>("capn_arnav", "Problem is, these old hands o'mine aren't as useful as they used t'be, and the lock on that chest is a little bit too fiddly for ol' Cap'n Arnav. Could you help me out?")
        choice {
            option<Happy>("Yes, I'll help you unlock your chest.") {
                npc<Neutral>("capn_arnav", "There are three columns. What you need to do for me is to match up each picture with the word underneath the column, and then unlock the chest.")
                openLock()
            }
            option<Sad>("No, sorry.")
        }
    }

    private fun Player.openLock() {
        if (!contains("arnav_target")) {
            set("arnav_target", WORDS.random(random))
        }
        for (column in 1..3) {
            set("arnav_lock_$column", START)
        }
        open("capn_arnav_lock")
        // Send the access masks so the client reports clicks on the arrows and unlock button; the
        // cache marks the options enabled but doesn't send them, so the buttons don't respond otherwise.
        for (component in listOf("down_1", "up_1", "down_2", "up_2", "down_3", "up_3", "unlock")) {
            interfaceOptions.unlockAll("capn_arnav_lock", component, 0..0)
        }
        sendWord(get("arnav_target", ""))
    }

    /**
     * Interface 185's target-word children (31 and 32) are static text components the cache decoder
     * drops, so the text is sent by raw packed component id instead of through [Player.interfaces].
     */
    private fun Player.sendWord(word: String) {
        client?.interfaceText(InterfaceDefinition.pack(LOCK_ID, 31), word)
        client?.interfaceText(InterfaceDefinition.pack(LOCK_ID, 32), word)
    }

    /** The centred symbol of a column, as an index into [WORDS]. */
    private fun Player.column(column: Int) = get("arnav_lock_$column", START)

    // The cylinders are a wrapping wheel driven entirely by the arnav_lock varbits: client script
    // 2112 repositions the four models whenever a varbit changes, so the arrows just step the value.
    private fun Player.roll(column: Int, direction: Int) {
        if (get<String>("random_event") != "capn_arnav" || column !in 1..3) {
            return
        }
        set("arnav_lock_$column", (column(column) + direction + WORDS.size) % WORDS.size)
    }

    private suspend fun Player.unlock() {
        val target = get<String>("arnav_target") ?: return
        close("capn_arnav_lock")
        if ((1..3).all { WORDS[column(it)] == target }) {
            solve()
        } else {
            wrongCombination()
        }
    }

    private suspend fun Player.solve() {
        set("arnav_solved", true)
        val chest = GameObjects.getShape(CHEST, 10)
        if (chest != null && chest.id == "capn_arnav_chest") {
            GameObjects.replace(chest, "capn_arnav_chest_open", ticks = CHEST_OPEN_TICKS)
        }
        npc<Happy>("capn_arnav", "Ah, well done matey, that's the right combination. Here, have a little somethin' for helpin' me out.")
        addOrDrop("random_event_gift")
        message("You've been given a gift!")
    }

    private suspend fun Player.wrongCombination() {
        val tries = inc("arnav_tries")
        when {
            tries >= MAX_TRIES -> {
                npc<Angry>("capn_arnav", "Arrr! I'd better find someone else.")
                message("Cap'n Arnav hits you over the head with his bottle.")
                clearState()
                RandomEvents.fail(this)
            }
            tries == 1 -> {
                npc<Angry>("capn_arnav", "Arrr! That be nowhere near close! Did ye not listen the first time around?")
                npc<Neutral>("capn_arnav", "Right, right. What you need to do is click on the arrows to roll the cylinders round to the different items.")
                npc<Neutral>("capn_arnav", "When the items in the middle row all match the words written under the column then you'll have solved the lock.")
                npc<Quiz>("capn_arnav", "Is that a little clearer?")
            }
            else -> npc<Angry>("capn_arnav", "Arrr! That be nowhere near close!")
        }
    }

    private fun Player.clearState() {
        clear("arnav_target")
        clear("arnav_tries")
        clear("arnav_solved")
        for (column in 1..3) {
            clear("arnav_lock_$column")
        }
    }

    companion object {
        private const val LOCK_ID = 185
        private const val MAX_TRIES = 5
        private const val CHEST_OPEN_TICKS = 25

        // The centred symbol per varbit value (client script 2112's wheel order); coins start centred.
        private val WORDS = listOf("Coins", "Bowl", "Bar", "Ring")
        private const val START = 0

        private val ISLAND = Tile(1626, 5163)
        private val CHEST = Tile(1627, 5162)
    }
}
