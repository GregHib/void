package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.event.TargetContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

npcOperate("Talk-to", "haakon_the_champion") {
    menu()
}

val validStages = setOf("tell_gudrun", "write_poem", "more_poem", "one_more_poem", "poem_done", "poem", "recital", "gunnars_ground")

suspend fun TargetContext<Player, NPC>.menu() {
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

suspend fun TargetContext<Player, NPC>.attack() {
    npc<Mad>("Make peace with your god, outerlander!")
    target.mode = Interact(target, player, PlayerOption(target, player, "Attack"))
}