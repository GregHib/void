package content.area.misthalin.tutorial_island

import content.entity.player.bank.bank
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.modal.gameFrameComponents
import content.entity.player.starterKit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile

class MagicInstructor : Script {

    init {
        npcOperate("Talk-to", "magic_instructor") {
            when (tutorialStage) {
                63 -> {
                    npc<Happy>("Greetings! I am here to teach you the ways of magic.")
                    npc<Neutral>("Open your spellbook to see the spells you can cast.")
                    advanceTutorial(63)
                }
                65 -> {
                    npc<Neutral>("Every spell needs runes. Take these air and mind runes, and cast Wind Strike on one of those chickens.")
                    inventory.add("air_rune", 5)
                    inventory.add("mind_rune", 5)
                    item("air_rune", "The Magic Instructor gives you some air and mind runes.")
                    advanceTutorial(65)
                }
                67 -> finish()
                else -> {
                    // Every rune is spent on a cast, so running out would otherwise strand the stage.
                    if (tutorialStage == 66 && (resupply("air_rune", 5) or resupply("mind_rune", 5))) {
                        npc<Neutral>("Out of runes? Take some more.")
                        return@npcOperate
                    }
                    npc<Neutral>("Cast Wind Strike on a chicken to finish your training.")
                }
            }
        }
    }

    private suspend fun Player.finish() {
        npc<Happy>("Well done, you've completed the tutorial!")
        npc<Neutral>("You're ready to enter the world proper. Would you like me to send you to Lumbridge now?")
        choice {
            option<Happy>("Yes, please.") {
                leave()
            }
            option<Neutral>("Not yet.") {
                npc<Neutral>("That's fine. Talk to me again whenever you're ready.")
            }
        }
    }

    private suspend fun Player.leave() {
        leaveTutorial()
        clearMinimap()
        TutorialRestrictions.restore(this)
        // Everyone leaves the island with the same kit, whatever they gathered on it.
        inventory.clear()
        equipment.clear()
        bank.clear()
        starterKit(this)
        for (component in gameFrameComponents) {
            open(component)
        }
        Teleport.teleport(this, homeTile(), "modern")
        // Teleporting is a strong queue, so this has to wait its turn rather than run inline -
        // an open message would otherwise block the teleport until the player dismissed it.
        queue("welcome") {
            statement("Welcome to Lumbridge! To get more help, simply click on the Lumbridge Guide or one of the Tutors - these can be found by looking for the question mark icon on your minimap.")
        }
    }

    private fun homeTile() = Tile(Settings["world.home.x", 0], Settings["world.home.y", 0], Settings["world.home.level", 0])
}
