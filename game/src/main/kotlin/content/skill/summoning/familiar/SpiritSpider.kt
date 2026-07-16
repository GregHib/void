package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class SpiritSpider : Script {
    init {
        npcOperate("Interact", "spirit_spider_familiar") {
            if (inventory.contains("flies")) {
                npc<Neutral>("So, do I get any of those flies?")
                player<Happy>("I don't know, I was saving these for a pet.")
                npc<Neutral>("I see...")
                player<Happy>("Look, you can have some if you want.")
                npc<Neutral>("Oh, don't do me any favours.")
                player<Frustrated>("Look, here, have some!")
                npc<Neutral>("Don't want them now.")
                player<Shifty>("Siiiigh...spiders.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Where are we going?")
                    player<Happy>("I've not decided yet.")
                    npc<Neutral>("Fine, don't tell me...")
                    player<Happy>("Oh, okay, well, we are going...")
                    npc<Neutral>("Don't want to know now.")
                    player<Shifty>("Siiiigh...spiders.")
                }
                1 -> {
                    npc<Neutral>("Who is that?")
                    player<Happy>("Who?")
                    npc<Neutral>("The two-legs over there.")
                    player<Happy>("I can't see who you mean...")
                    npc<Neutral>("Never mind...")
                    player<Happy>("Can you describe them a little better...")
                    npc<Neutral>("It doesn't matter now.")
                    player<Shifty>("Siiiigh...spiders.")
                }
                2 -> {
                    npc<Neutral>("What are you doing?")
                    player<Happy>("Nothing that you should concern yourself with.")
                    npc<Neutral>("I see, you don't think I'm smart enough to understand...")
                    player<Frustrated>("That's not it at all! Look, I was...")
                    npc<Neutral>("Don't wanna know now.")
                    player<Shifty>("Siiiigh...spiders.")
                }
                3 -> {
                    npc<Neutral>("Sigh...")
                    player<Frustrated>("What is it now?")
                    npc<Neutral>("Nothing really.")
                    player<Happy>("Oh, well that's a relief.")
                }
            }
        }
    }
}
