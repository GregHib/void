package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class MagicInstructor : Script {

    init {
        npcOperate("Talk-to", "magic_instructor") {
            when (tutorialStage) {
                63 -> {
                    player<Happy>("Hello.")
                    npc<Happy>("Good day, newcomer. My name is Terrova. I'm here to tell you about Magic. Let's start by opening your spellbook.")
                    advanceTutorial(63)
                }
                65 -> {
                    npc<Neutral>("Good. This is a list of your spells. Currently you can only cast one offensive spell called Wind Strike. Let's try it out on one of those chickens.")
                    inventory.add("air_rune", 5)
                    inventory.add("mind_rune", 5)
                    item("air_rune", "Terrova gives you five <blue>air runes</col> and five <blue>mind runes</col>!")
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
        npc<Happy>("Well you're all finished here now. I'll give you a reasonable number of runes when you leave.")
        choice("Do you want to go to the mainland?") {
            option<Happy>("Yes.") {
                leave()
            }
            option<Neutral>("No.") {
                npc<Neutral>("That's fine. Talk to me again whenever you're ready.")
            }
        }
    }

    private suspend fun Player.leave() {
        player<Happy>("Yes, I'm ready to leave.")
        npc<Happy>("Good good. I've deactivated the protective spells around the island, so now you can teleport yourself out of here.")
        npc<Neutral>("When you get to the mainland you will find yourself in the town of Lumbridge. If you want some ideas on where to go next, talk to my friend Phileas, also known as the Lumbridge Guide. You can't miss him; he's holding a big staff with a question mark on the end.")
        npc<Neutral>("He also has a white beard and carries a rucksack full of scrolls. There are also tutors willing to teach you about the many skills you could learn.")
        item("questionmark_icon", "When you get to Lumbridge, look for this icon on your minimap. The Lumbridge Guide and the other tutors will be standing near one of these.")
        item("questionmark_icon", "The Lumbridge Guide should be standing slightly to the north-east of the castle's courtyard and the others you will find scattered around Lumbridge.")
        npc<Neutral>("If all else fails, visit the RuneScape website for a whole chestload of information on quests, skills and minigames as well as a very good starter's guide.")
        advanceTutorial(67)
    }
}
