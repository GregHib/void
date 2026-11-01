package content.quest.free.ernest_the_chicken

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Veronica : Script {
    init {
        npcOperate("Talk-to", "veronica") { talk() }
    }

    private suspend fun Player.talk() {
        when (quest("ernest_the_chicken")) {
            "unstarted" -> {
                npc<Neutral>("Can you please help me? I'm in a terrible spot of trouble.")
                choice {
                    option<Neutral>("Aha, sounds like a quest. I'll help.") {
                        npc<Neutral>("Yes yes, I suppose it is a quest. My fiance Ernest and I came upon this house.")
                        npc<Neutral>("Seeing as we were a little lost Ernest decided to go in and ask for directions.")
                        npc<Neutral>("That was an hour ago. That house looks spooky, can you go and see if you can find him for me?")
                        player<Neutral>("Ok, I'll see what I can do.")
                        npc<Happy>("Thank you, thank you. I'm very grateful.")
                        set("ernest_the_chicken", "started")
                    }
                    option<Neutral>("No, I'm looking for something to kill.") {
                        npc<Neutral>("Oooh, you violent person you.")
                    }
                }
            }
            "completed" -> npc<Happy>("Thank you for helping Ernest!")
            "parts" -> {
                npc<Neutral>("Have you found my sweetheart yet?")
                player<Neutral>("Yes, he's a chicken.")
                npc<Neutral>("I know he's not exactly brave but I think you're being a bit harsh.")
                player<Neutral>("No no, he's been turned into an actual chicken by a mad scientist.")
                npc<Neutral>("Eeeeeek!")
                npc<Neutral>("My poor darling, why must these things happen to us.")
                player<Neutral>("Well I'm doing my best to turn him back.")
                npc<Neutral>("Well be quick, I'm sure being a chicken can't be good for him.")
            }
            else -> {
                npc<Neutral>("Have you found my sweetheart yet?")
                player<Neutral>("No, not yet.")
            }
        }
    }
}
