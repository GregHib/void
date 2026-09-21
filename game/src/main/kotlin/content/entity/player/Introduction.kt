package content.entity.player

import content.area.misthalin.tutorial_island.inTutorial
import content.bot.isBot
import content.entity.player.bank.bank
import content.entity.player.dialogue.type.nameEntry
import content.entity.player.dialogue.type.statement
import content.social.friend.nameTaken
import content.social.friend.rename
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.epochMilliseconds
import java.util.concurrent.TimeUnit

class Introduction : Script {

    fun welcome(player: Player) {
        player.message("Welcome to ${Settings["server.name"]}.", ChatType.Welcome)
        player["login_time"] = epochMilliseconds()
        if (player.contains("creation")) {
            return
        }
        if (player.inTutorial) {
            return // Tutorial Island owns character creation, the welcome and the starter kit
        }
        // Character creation ends with the name panel, otherwise fall back to a dialogue prompt
        if (player["choose_name", false] && !player.isBot && !Settings["world.start.creation", true]) {
            player.sendVariable("movement")
            player["delay"] = -1
            player.strongQueue("choose_name") {
                chooseDisplayName()
                start(player)
            }
            return
        }
        start(player)
    }

    private fun start(player: Player) {
        if (Settings["world.start.creation", true] && !player.isBot) {
            player.sendVariable("movement")
            player["delay"] = -1
            World.queue("welcome_${player.name}", 1) {
                player.open("character_creation")
            }
        } else {
            player.flagAppearance()
            setup(player)
        }
    }

    init {
        playerSpawn(::welcome)

        interfaceClosed("character_creation") {
            if (inTutorial) {
                return@interfaceClosed
            }
            flagAppearance()
            setup(this)
        }

        playerDespawn {
            val start = get("login_time", 0L)
            val duration = epochMilliseconds() - start
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration).toInt()
            inc("playtime", seconds)
        }
    }

    /**
     * Accounts registered with an email address are given a placeholder display name until the player picks one
     */
    private suspend fun Player.chooseDisplayName() {
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

    fun setup(player: Player) {
        player.queue("welcome") {
            player.statement("Welcome to Lumbridge! To get more help, simply click on the Lumbridge Guide or one of the Tutors - these can be found by looking for the question mark icon on your minimap. If you find you are lost at any time, look for a signpost or use the Lumbridge Home Teleport spell.")
        }
        player.stop("delay")
        player["creation"] = System.currentTimeMillis()
        starterKit(player)
    }
}

fun starterKit(player: Player) {
    if (!Settings["world.start.gear", true]) {
        return
    }
    if (player["had_starter", false]) {
        return
    }
    player.bank.add("coins", 25)
    player.inventory.apply {
        add("bronze_hatchet")
        add("tinderbox")
        add("small_fishing_net")
        add("shrimps")
        add("bucket")
        add("empty_pot")
        add("bread")
        add("bronze_pickaxe")
        add("bronze_dagger")
        add("bronze_sword")
        add("wooden_shield")
        add("shortbow")
        add("bronze_arrow", 25)
        add("air_rune", 25)
        add("mind_rune", 15)
        add("water_rune", 6)
        add("earth_rune", 4)
        add("body_rune", 2)
    }
    player["had_starter"] = true
}
