package content.area.asgarnia.entrana

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class Mazion : Script {
    init {
        npcOperate("Talk-to", "mazion") {
            when (quest("hand_in_the_sand")) {
                "search_entrana" -> foundTheHead()
                "return_head" -> returnToYanille()
                else -> greeting()
            }
        }
    }

    private suspend fun Player.greeting() {
        when (random.nextInt(3)) {
            0 -> npc<Happy>("Hello $name, fine day today!")
            1 -> npc<Happy>("Nice weather we're having today!")
            else -> npc<Sad>("Please leave me alone, a parrot stole my banana.")
        }
    }

    private suspend fun Player.foundTheHead() {
        player<Happy>("Hello there!")
        npc<Neutral>("Uh...greetings $name!")
        player<Shock>("Uhh... How do you know my name?")
        npc<Happy>("Oh, I like to keep ahead of things.")
        player<Quiz>(
            "Err.. ok. Well, I've been sent from the Wizards' Guild in Yanille. There's been " +
                "an... incident... Do you have any body parts?",
        )
        npc<Shock>("How did you know! I found the most awful thing in my sandpit - a head!")
        player<Happy>("Ahhh good! I need to take it back to be buried!")
        npc<Shock>("You're very strange, but if it means I get rid of the horrid thing...")
        if (inventory.spaces < 1) {
            npc<Angry>("Or I would if you had any room in your bag!")
            return
        }
        set("hand_in_the_sand", "return_head")
        addOrDrop("wizard_head")
        item(item = "wizard_head", text = "Mazion gives you the head.")
    }

    private suspend fun Player.returnToYanille() {
        if (inventory.contains("wizard_head")) {
            npc<Shock>("I see you still have that head! Take it back to the Wizards in Yanille!")
            return
        }
        npc<Happy>("Hello again $name!")
        choice {
            option<Quiz>("What should I do with the head?") {
                npc<Angry>(
                    "It was you that came demanding the head to give to the wizard in the first " +
                        "place! Go back to Yanille with it!",
                )
            }
            option<Shock>("I've lost my head!") {
                replaceHead()
            }
        }
    }

    private suspend fun Player.replaceHead() {
        if (inventory.spaces < 1) {
            npc<Neutral>("You dropped it and you don't have room to carry it now, come back when you do.")
            return
        }
        npc<Neutral>(
            "Keep your hair on! You dropped it! Make sure you take it straight back to the " +
                "wizards else you won't have a leg to stand on.",
        )
        addOrDrop("wizard_head")
        item(item = "wizard_head", text = "Mazion hands you the head.")
    }
}
