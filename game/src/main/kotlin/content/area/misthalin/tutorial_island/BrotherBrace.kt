package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class BrotherBrace : Script {

    init {
        npcOperate("Talk-to", "brother_brace") {
            when (tutorialStage) {
                56 -> {
                    npc<Happy>("Hello there, and welcome to the church. I'm here to tell you about Prayer.")
                    npc<Neutral>("Open your prayer list and take a look at what's available to you.")
                    advanceTutorial(56)
                }
                58 -> {
                    npc<Neutral>("Prayers drain your prayer points while they're active. Bury bones or pray at an altar to restore them.")
                    npc<Neutral>("Now let me show you the friends list. Open it and you'll see who's online.")
                    advanceTutorial(58)
                }
                61 -> {
                    npc<Neutral>("Your ignore list works the same way, but for people you'd rather not hear from.")
                    npc<Happy>("That's me done. Head out of the church and speak to the Magic Instructor.")
                    advanceTutorial(61)
                }
                else -> npc<Neutral>("May Saradomin watch over you.")
            }
        }
    }
}
