package content.area.misthalin.lumbridge.thieves_guild

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has

class ChiefThiefRobin : Script {
    init {
        // https://runescape.wiki/w/Transcript:Chief_Thief_Robin#In_the_Thieves'_Guild
        npcOperate("Talk-to", "chief_thief_robin_2") {
            player<Neutral>("Hello there.")
            if (questCompleted("buyers_and_cellars")) {
                npc<Happy>("Ah, our rising star.")
            } else {
                npc<Neutral>("Greetings. I'm Robin, the Guildmaster's assistant.")
            }
            menu()
        }
    }

    private suspend fun Player.menu() {
        npc<Neutral>("Now, what can I do for you?")
        val member = questCompleted("buyers_and_cellars")
        choice {
            option<Neutral>("That's an appropriate name.") {
                npc<Neutral>("Yes, I've never heard that one before.")
                npc<Neutral>("Still, it's not so bad... If I hadn't been mocked for my name as a lad, I might never have decided in a fit of ironic pique to learn how to rob from the rich and give to the poor.")
                player<Quiz>("Are you the poor in question?")
                npc<Neutral>("Well, I was.")
                npc<Happy>("Having given myself the goods stolen from several rich people, I'm now of limited but comfortable means.")
                menu()
            }
            option<Quiz>("How long have you known the Guildmaster?") {
                npc<Neutral>("Oh, some time now. We started in business together when he was a con artist, talking people into handing over their hard-earned valuables with lies and vague promises of reward.")
                npc<Shifty>("Obviously, that's all behind us now.")
                menu()
            }
            if (member) {
                option<Quiz>("Where can I practice my art?") {
                    practice()
                    menu()
                }
                option<Quiz>("Could you help me sell these items?") {
                    // TODO fencing loot bags requires A Guild of Our Own
                    npc<Neutral>("You will have to help the guild more before I will be able to fence items for you.")
                }
            }
            option<Neutral>("Bye for now.")
        }
    }

    private suspend fun Player.practice() {
        npc<Neutral>("I hear there's a fellow in Al Kharid who needs someone for some infiltration work.")
        if (has(Skill.Agility, 40)) {
            npc<Neutral>("There's rumours of a pyramid out in the deep desert full of traps and valuables.")
        }
        if (has(Skill.Agility, 50) && has(Skill.Thieving, 50)) {
            npc<Neutral>("And a colleague of ours has set up a maze game in Burthorpe, if you feel light on your feet.")
        }
        player<Happy>("Thanks, Robin.")
    }
}
