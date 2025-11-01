package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.male

class Iffie : Script {

    init {
        npcOperate("Talk-to", "iffie") {
            npc<Happy>("Hello, dearie! Were you wanting to collect a random event costume, or is there something else I can do for you today?")
            choice {
                option("I've come for a random event costume.") {
                    npc<Happy>("Some of these costumes even come with a free emote!")
                    npc<Happy>("Just buy one piece of the mine of zombie costumes and I'll show you the relevant moves.")
                    openShop("iffies_random_costume_shop")
                }
                option<Quiz>("Aren't you selling anything?") {
                    npc<Chuckle>("Oh, yes, but only costumes. Thessalia sells some other clothes and runs the makeover service.")
                }
                option<Talk>("I just came for a chat.") {
                    npc<Sad>("Oh, I'm sorry, but I'll never get my knitting done if I stop for a chit-chat with every young ${if (male) "lad" else "lass"} who wanders through the shop!")
                }
            }
        }

        npcOperate("Claim-costume", "iffie") {
            openShop("iffies_random_costume_shop")
        }
    }
}
