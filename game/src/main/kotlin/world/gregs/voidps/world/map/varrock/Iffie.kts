package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

npcOperate("Talk-to", "iffie") {
    npc<Cheerful>("Hello, dearie! Were you wanting to collect a random event costume, or is there something else I can do for you today?")
    choice {
        option("I've come for a random event costume.") {
            npc<Cheerful>("Some of these costumes even come with a free emote!")
            npc<Cheerful>("Just buy one piece of the mine of zombie costumes and I'll show you the relevant moves.")
            player.openShop("iffies_random_costume_shop")
        }
        option<Unsure>("Aren't you selling anything?") {
            npc<Chuckle>("Oh, yes, but only costumes. Thessalia sells some other clothes and runs the makeover service.")
        }
        option<Talk>("I just came for a chat.") {
            npc<Sad>("Oh, I'm sorry, but I'll never get my knitting done if I stop for a chit-chat with every young ${if (player.male) "lad" else "lass"} who wanders through the shop!")
        }
    }
}

npcOperate("Claim-costume", "iffie") {
    player.openShop("iffies_random_costume_shop")
}