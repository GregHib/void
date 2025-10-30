package content.area.misthalin.lumbridge

import content.entity.player.dialogue.*
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.type.random

class LumbridgeMen : Script {

    init {
        npcOperate("Talk-to", "lumbridge_man*", "lumbridge_woman*") {
            when (random.nextInt(0, 6)) {
                0 -> player<Happy>("Howdy.")
                1 -> player<Happy>("Salutations!")
                2 -> player<Happy>("Hello there.")
                3 -> player<Happy>("Good day.")
                4 -> player<Happy>("Nice to meet you.")
                5 -> player<Happy>("Greetings.")
            }
            when (random.nextInt(0, 15)) {
                0 -> npc<Sad>("I have to go all the way through the swamp to go mining. My poor feet!")
                1 -> npc<Sad>("Hello. I'm Sorry to say that Lumbridge is not what it once was.")
                2 -> npc<Happy>("You're not from around here, are you? I can see it in your eyes.")
                3 -> npc<Happy>("Sorry, were you speaking to me? I was daydreaming. Hello, anyway.")
                4 -> npc<Happy>("Sorry, I don't speak to strangers. They're weird.")
                5 -> npc<Happy>("Hello to you too, adventurer.")
                6 -> npc<Happy>("I wish people would stop ringing the church bell. I can't tell what time it is.")
                7 -> npc<Angry>("I can't believe that Lachtopher boy. He tried to borrow money from me again.")
                8 -> npc<Happy>("They really need to fix the castle flag. Every time you raise it, it just comes back down. Shoddy maintenance.")
                9 -> npc<Happy>("Hello, I'm glad to see an adventurer about. There's an increase in goblins hanging around the area.")
                10 -> npc<Happy>("Another adventurer, off to save the world, eh?")
                11 -> npc<Sad>("Don't come near me, I have a cold!")
                12 -> npc<Happy>("Hey, do you like my clothes? They're new.")
                13 -> npc<Happy>("Don't ask me for directions, I'm just a tourist here.")
                14 -> npc<Happy>("Welcome to Lumbridge")
            }
        }
    }
}
