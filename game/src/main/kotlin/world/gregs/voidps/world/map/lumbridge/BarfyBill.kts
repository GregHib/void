package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.minimumCanoeLevel

npcOperate("Talk-To", "barfy_bill") {
    player<Neutral>("Hello there.")
    npc<Neutral>("Oh! Hello there.")
    choice {
        option<Neutral>("Who are you?") {
            npc<Neutral>("My name is Ex Sea Captain Barfy Bill.")
            player<Unsure>("Ex sea captain?")
            npc<Sad>("Yeah, I bought a lovely ship and was planning to make a fortune running her as a merchant vessel.")
            player<Unsure>("Why are you not still sailing?")
            npc<Sad>("Chronic sea sickness. My first, and only, voyage was spent dry heaving over the rails.")
            npc<Neutral>("If I had known about the sea sickness I could have saved myself a lot of money.")
            player<Uncertain>("What are you up to now then?")
            npc<Suspicious>("Well my ship had a little fire related problem. Fortunately it was well insured.")
            npc<Neutral>("Anyway, I don't have to work anymore so I've taken to canoeing on the river.")
            npc<Cheerful>("I don't get river sick!")
            npc<Unsure>("Would you like to know how to make a canoe?")
            choice {
                option("Yes") {
                    canoeing()
                }
                option("No") {
                    player<Neutral>("No thanks, not right now.")
                }
            }
        }
        option("Can you teach me about Canoeing?") {
            canoeing()
        }
    }
}

suspend fun CharacterContext.canoeing() {
    if (minimumCanoeLevel()) {
        return
    }
    npc<Neutral>("It's really quite simple to make. Just walk down to that tree on the bank and chop it down.")
    npc<Neutral>("When you have done that you can shape the log further with your axe to make a canoe.")
    when (player.levels.get(Skill.Woodcutting)) {
        in 12..26 -> {
            npc<Neutral>("Hah! I can tell just by looking that you lack talent in woodcutting.")
            player<Unsure>("What do you mean?")
            npc<Cheerful>("No Callouses! No Splinters! No camp fires littering the trail behind you.")
            npc<Cheerful>("Anyway, the only 'canoe' you can make is a log. You'll be able to travel 1 stop along the river with a log canoe.")
        }
        in 27..41 -> {
            npc<Cheerful>("With your skill in woodcutting you could make my favourite canoe, the Dugout. They might not be the best canoe on the river, but they get you where you're going.")
            player<Unsure>("How far will I be able to go in a Dugout canoe?")
            npc<Cheerful>("You will be able to travel 2 stops on the river.")
        }
        in 42..56 -> {
            npc<Cheerful>("The best canoe you can make is a Stable Dugout, one step beyond a normal Dugout.")
            npc<Cheerful>("With a Stable Dugout you can travel to any place on the river.")
            player<Unsure>("Even into the Wilderness?")
            npc<Cheerful>("Not likely! I've heard tell of a man up near Edgeville who claims he can use a Waka to get up into the Wilderness.")
            npc<Unsure>("I can't think why anyone would wish to venture into that hellish landscape though.")
        }
        else -> {
            npc<Cheerful>("Hoo! You look like you know which end of an axe is which!")
            npc<Neutral>("You can easily build one of those Wakas. Be careful if you travel into the Wilderness though.")
            npc<Afraid>("I've heard tell of great evil in that blasted wasteland.")
            player<Neutral>("Thanks for the warning Bill.")
        }
    }
}