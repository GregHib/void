package content.area.kandarin.catherby

import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate

// TODO: add Family Crest & fill out the rest of dialogue out for quest

npcOperate("Talk-to", "caleb") {
    npc<Talk>("Who are you? What are you after?")

    choice {
        option("Nothing, I will be on my way.") {
            player<Talk>("Nothing, I will be on my way.")
            // Ends dialogue naturally
        }

        option("I see you are a chef, will you cook me anything?") {
            player<Talk>("I see you are a chef... Could you cook me something?")
            npc<Talk>("I would, but I am very busy. I am trying to increase my renown as one of the world's leading chefs by preparing a special and unique fish salad.")
            // Ends dialogue naturally
        }
    }
}
