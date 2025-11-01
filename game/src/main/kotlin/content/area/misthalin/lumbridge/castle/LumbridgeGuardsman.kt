package content.area.misthalin.lumbridge.castle

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class LumbridgeGuardsman : Script {

    init {
        npcOperate("Talk-to", "guardsman_*") {
            when (random.nextInt(0, 5)) {
                0 -> player<Happy>("Howdy.")
                1 -> player<Happy>("Salutations!")
                2 -> player<Happy>("Good day.")
                3 -> player<Happy>("Nice to meet you.")
                4 -> player<Happy>("Greetings.")
            }
            when (random.nextInt(0, 3)) {
                0 -> npc<Happy>("Well met, adventurer.")
                1 -> npc<Happy>("Well, hello there.")
                2 -> npc<Happy>("Good day to you, adventurer.")
            }
            choice("What would you like to say?") {
                tellMeGuardsmen()
                aroundHere()
                tellMeLumbridge()
                whatAreGuarding()
                option<Neutral>("Bye.")
            }
        }
    }

    fun ChoiceOption.tellMeGuardsmen(): Unit = option<Quiz>("Tell me about the Lumbridge Guardsmen.") {
        when (random.nextInt(0, 2)) {
            0 -> npc<Happy>("I won't pretend that we're an elite fighting force, but we know how to work with the castle's defences. That means just a few of us can hold a fairly strong defence, if we ever need to.")
            1 -> npc<Happy>("I spoke to a few people who asked me about joining the Lumbridge Guardsmen. To be asked, you need to be a true local. I mean, when the call to arms is raised, you don't want your troops to be scattered across the world: they need to be waiting here, ready to spring in to action.")
        }
        choice("What would you like to say?") {
            tellMeGuardsmen()
            aroundHere()
            tellMeLumbridge()
            whatAreGuarding()
            option<Neutral>("Bye.")
        }
    }

    fun ChoiceOption.aroundHere(): Unit = option<Quiz>("What is there to do around here?") {
        when (random.nextInt(0, 2)) {
            0 -> npc<Happy>("If you're interested in learning to make leather armour, there's the cow field to get hides. Then, you can take your the hides to a tanner.")
            1 -> npc<Happy>("If you'd like to make a bit of spending money, try speaking to the skill tutors. They go through a lot of supplies, so I'm sure they'd be happy to pay you to make the supplies they need.")
        }
        choice("What would you like to say?") {
            tellMeGuardsmen()
            aroundHere()
            tellMeLumbridge()
            whatAreGuarding()
            option<Neutral>("Bye.")
        }
    }

    fun ChoiceOption.tellMeLumbridge(): Unit = option<Quiz>("Tell me about Lumbridge.") {
        when (random.nextInt(0, 3)) {
            0 -> npc<Neutral>("It used to be much nicer here, before the goblins overran the east side of town. You'd think that the Guardsmen would be sent to flush them out, but for every one we slay, three more appear in its place.")
            1 -> npc<Neutral>("If I'm honest, I don't much care for it here. I understand why so many do enjoy the lifestyle, but I long for the city life. If I do well in my job here, perhaps I could move to Varrock, or Falador, and join the guards there.")
            2 -> npc<Neutral>("I suppose it's a good place to find your feet in the world. There's reasonable fishing in the river, and with lots of farmland around it's rare that you go hungry. Yes, there's plenty worse places you could live in than Lumbridge.")
        }
        choice("What would you like to say?") {
            tellMeGuardsmen()
            aroundHere()
            tellMeLumbridge()
            whatAreGuarding()
            option<Neutral>("Bye.")
        }
    }

    fun ChoiceOption.whatAreGuarding(): Unit = option<Quiz>("What exactly are you guarding?") {
        when (random.nextInt(0, 2)) {
            0 -> npc<Happy>("Peace, happiness, tranquillity, that sort of thing. So, let me know if you see anything upsetting. Dragons in cellars, goblins under beds, that sort of thing.")
            1 -> npc<Happy>("I suppose I'm guarding the castle, and the Lumbridge way of life. Although, there was once a dragon near here, and I helped defend the castle from it. That was an exciting time!")
        }
        choice("What would you like to say?") {
            tellMeGuardsmen()
            aroundHere()
            tellMeLumbridge()
            whatAreGuarding()
            option<Neutral>("Bye.")
        }
    }
}
