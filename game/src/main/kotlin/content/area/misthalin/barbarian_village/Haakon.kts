package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.quest
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption

npcOperate("Talk-to", "haakon_the_champion") {
    menu()
}

val validStages = setOf("tell_gudrun", "write_poem", "more_poem", "one_more_poem", "poem_done", "poem", "recital", "gunnars_ground")

suspend fun NPCOption<Player>.menu() {
    npc<Angry>("I am Haakon, champion of this village. Do you seek to challenge me?")
    choice {
        option<Neutral>("I challenge you!") {
            attack()
        }
        if (validStages.contains(player.quest("gunnars_ground"))) {
            option<Neutral>("You argued with Gunthor.") {
                npc<Frustrated>("There is no argument. I honour my father and my ancestors.")
                choice {
                    option<Neutral>("Don't you want to settle permanently?") {
                        npc<Angry>("You test my patience by quuestioning my loyalty to my chieftain. Take up my challenge, outerlander, that I might honourably split your skull open..")
                        choice {
                            option<Neutral>("I'll take your challenge!") {
                                attack()
                            }
                            option<Neutral>("No thanks.")
                        }
                    }
                    option<Neutral>("How about that challenge?") {
                        attack()
                    }
                    option<Neutral>("Goodbye then.")
                }
            }
        }
        option<Amazed>("Er, no.")
    }
}

suspend fun NPCOption<Player>.attack() {
    npc<Mad>("Make peace with your god, outerlander!")
    target.mode = Interact(target, player, PlayerOption(target, player, "Attack"))
}
