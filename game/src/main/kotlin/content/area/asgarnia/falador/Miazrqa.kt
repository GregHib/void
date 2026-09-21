package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script

class Miazrqa : Script {

    init {
        npcOperate("Talk-to", "miazrqa") {
            if (quest("grim_tales").startsWith("completed")) {
                npc<Idle>("Thanks again for finding my pendant. I felt so lost without it.")
                player<Idle>("You're welcome. Goodbye.")
                return@npcOperate
            }

            player<Idle>("Hello there.")
            npc<Idle>("Oh hello.")
            player<Quiz>("Who are you?")
            npc<Angry>("Who am I? Why, how impertinent! I am a princess and I will be spoken to like one. Now be off with you. I have no time for scruffy adventurers like yourself!")
            player<Quiz>("I notice that there is a dwarf up in the tower there. Why is he up there?")
            npc<Angry>("I do believe that is none of your business, young scallyrumpit! Now kindly leave.")
            player<Idle>("Uh...ok. Never mind then!")
        }
    }
}

// (Attempting to open the door to her tower)
// This door appears to be securely locked.
// Miazrqa: Excuse me. You can't go in there.
// Player: Oh, sorry.

// (Attempting to climb over the crumbling wall behind her tower)
// Miazrqa: Hey, what's that noise over there? Is someone there? Who's near my tower?
// Player: Uh...no...no, nothing to see here. It was just the...er...wind! Yes, the wind.
// It looks like you will need a Thieving skill of 58 to climb over this wall without being seen.
