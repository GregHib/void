package content.area.misthalin.tutorial_island

import content.entity.player.bank.bank
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.modal.GameFrame
import content.entity.player.starterKit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearHints
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile

class RunescapeGuide : Script {

    init {
        npcOperate("Talk-to", "runescape_guide") {
            when (tutorialStage) {
                0 -> {
                    npc<Happy>("Greetings! I see you are a new arrival to this land. My job is to teach you a few basic skills and functions.")
                    if (!Settings["world.start.tutorial.skippable", false]) {
                        begin()
                        return@npcOperate
                    }
                    choice("Do you want to go through the tutorial?") {
                        option<Neutral>("Yes, show me how to play.") {
                            begin()
                        }
                        option<Happy>("No, send me to the mainland.") {
                            npc<Neutral>("As you wish. I'll send you on your way with the usual supplies.")
                            finishTutorial()
                        }
                    }
                }
                2 -> {
                    npc<Neutral>("The options panel lets you change the screen brightness, the volume of the music and sound effects, and whether other players may offer you help.")
                    npc<Happy>("That's all I have to teach you. Go through that door and the Survival Expert will show you how to look after yourself.")
                    player<Neutral>("Thanks, I'll do that.")
                    advanceTutorial(2)
                }
                else -> {
                    npc<Neutral>("You've learnt all I have to teach. Follow the arrow to your next instructor.")
                    npc<Neutral>("Welcome to ${Settings["server.name"]}.")
                }
            }
        }

        // Home Teleport is how the tutorial ends, so it's the one spell that gets through - and
        // casting it is what completes the tutorial. The spell does the travelling itself.
        teleportTakeOff("modern") { spell ->
            if (!inTutorial) {
                return@teleportTakeOff true
            }
            if (tutorialStage != TutorialIsland.lastStage || spell != "lumbridge_home_teleport") {
                return@teleportTakeOff false
            }
            completeTutorial()
            true
        }
    }

    private fun exitTile() = Tile(Settings["world.start.tutorial.exit.x", 0], Settings["world.start.tutorial.exit.y", 0], Settings["world.start.tutorial.exit.level", 0])

    fun Player.finishTutorial() {
        completeTutorial()
        Teleport.teleport(this, exitTile(), "modern")
        // Teleporting is a strong queue, so this has to wait its turn rather than run inline - an
        // open message would otherwise block the teleport until the player dismissed it.
        queue("welcome") {
            statement("Welcome to Lumbridge! To get more help, simply click on the Lumbridge Guide or one of the Tutors - these can be found by looking for the question mark icon on your minimap.")
        }
    }

    /**
     * Ends the tutorial and hands over the standard kit, leaving the player where they are. The
     * Home Teleport that finishes the tutorial does its own travelling.
     */
    fun Player.completeTutorial() {
        leaveTutorial()
        clearMinimap()
        TutorialRestrictions.restore(this)
        // Everyone leaves the island with the same kit, whatever they gathered on it.
        inventory.clear()
        equipment.clear()
        bank.clear()
        starterKit(this)
        for (component in GameFrame.components) {
            open(component)
        }
    }

    fun Player.leaveTutorial() {
        set("tutorial_stage", -1)
        set("tutorial_complete", true)
        // Marks the introduction as done so `Introduction` never re-runs character creation
        // or hands out a second starter kit.
        this["creation"] = System.currentTimeMillis()
        clear("tab_flash")
        clear("tutorial_progress")
        clearHints()
        close("tutorial_text")
        close("tutorial_overlay")
    }

    private suspend fun Player.begin() {
        npc<Neutral>("First we shall go through some of the game's control panels, which you can find at the bottom right of your screen.")
        npc<Neutral>("Click on the flashing spanner icon to open your game options.")
        advanceTutorial(0)
    }
}
