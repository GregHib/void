package content.area.morytania.fenkenstrains_castle

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class LordRologarth : Script {

    init {
        npcOperate("Talk-to", "fenkenstrains_monster*") {
            when (quest("creature_of_fenkenstrain")) {
                "creature_loose" -> firstMeeting()
                "creature_convinced" -> howToStopFenkenstrain()
                "completed" -> postQuestChat()
            }
        }

        npcOperate("Talk-to", "lord_rologarth*") {
            when (quest("creature_of_fenkenstrain")) {
                "creature_loose" -> firstMeeting()
                "creature_convinced" -> howToStopFenkenstrain()
                "completed" -> postQuestChat()
            }
        }
    }

    private suspend fun Player.firstMeeting() {
        player<Angry>("I am commanded to destroy you, creature!")
        npc<Drunk>("Oh that's *hic* not very *hic* nice ...")
        player<Quiz>("Are you feeling okay?")
        npc<Drunk>("Abso *hic* lutely. Never *buuurrp* better.")
        player<Confused>("You don't look very dangerous.")
        npc<Confused>("How *hic* do I look?")
        player<Confused>("You really don't know, do you? Have a look for yourself.")
        statement("The creature stumbles over towards the mirror, focuses upon his reflection and...")
        npc<Scared>("AAAAARRGGGGHHHH!")
        statement("The creature becomes instantly sober, horror all too evident in his undead eyes.")
        player<Sad>("I'm sorry. I suppose I'm partly to blame for this.")
        npc<Angry>("No - it was him I wager - Fenkenstrain - wasn't it? He's brought me back to life!")
        player<Quiz>("Who are - were - you?")
        npc<Neutral>("I was Rologarth, Lord of the North Coast - this castle was once mine. Fenkenstrain was the castle doctor.")
        player<Shock>("So this castle wasn't really abandoned when he found it?")
        npc<Sad>("Is that what he told you? No, no, this castle was once full of people and life. Fenkenstrain advised me to sell them to the vampires, which - I am sad to say - I did.")
        player<Sad>("I found your brain in a jar in Canifis, so he must have sold you too.")
        npc<Sad>("Of that I will not speak. There lie memories that should rest with the dead, the living unable to bear them.")
        // TODO check anim
        player<Sad>("That's it - I'm leaving this dreadful place, whether I get paid or not. Is there anything I can do for you before I leave?")
        set("creature_of_fenkenstrain", "creature_convinced")
        npc<Neutral>("Only one - please stop Fenkenstrain from carrying on his experiments, once and for all, so that no other poor soul has to endure suffering such as that of my people and I.")
    }

    private suspend fun Player.howToStopFenkenstrain() {
        player<Quiz>("Do you know how I can stop Fenkenstrain's experiments?")
        npc<Neutral>("Take the Ring of Charos from him.")
        player<Quiz>("What is this ring?")
        npc<Neutral>("It was my birthright, passed down to me through the ages, its origin forgotten.")
        npc<Neutral>("The Ring of Charos has many powers, but Fenkenstrain has bent them to his own evil purposes. Without the power of the ring, he will not be able to raise the dead from their sleep.")
        npc<Neutral>("It has one other, extremely important use - it confuses the werewolves' senses, making them believe that they smell one of their own kind. Without the ring, Fenkenstrain will be at their mercy.")
    }

    private suspend fun Player.postQuestChat() {
        npc<Quiz>("How goes it, friend?")
        player<Happy>("I stole the Ring of Charos from Fenkenstrain.")
        npc<Neutral>("I saw him climb up into the Tower to hide. It doesn't matter - soon the werewolves will come for him, and his experiments will be forever ceased.")
        player<Quiz>("Do you want the ring back? It is yours after all.")
        npc<Neutral>("No, you keep it, my friend. Werewolves hunger for the scent of live flesh. I have no need for the ring. I have my castle back, if not my soul.")
    }
}
