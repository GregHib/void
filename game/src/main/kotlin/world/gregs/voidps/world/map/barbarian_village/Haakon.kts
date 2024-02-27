package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

npcOperate("Talk-to", "haakon_the_champion") {
    menu()
}

suspend fun CharacterContext.menu() {
    npc<Furious>("I am Haakon, champion of this village. Do you seek to challenge me?")
    choice {
        option<Talking>("I challenge you!") {
            attack()
        }
        val validStages = setOf("tell_gudrun", "write_poem", "more_poem", "one_more_poem", "poem_done", "poem", "recital", "gunnars_ground")
        if (validStages.contains(player.quest("gunnars_ground"))) {
            option<Talking>("You argued with Gunthor.") {
                npc<Angry>("There is no argument. I honour my father and my ancestors.")
                choice {
                    option<Talking>("Don't you want to settle permanently?") {
                        npc<Furious>("You test my patience by quuestioning my loyalty to my chieftain. Take up my challenge, outerlander, that I might honourably split your skull open..")
                        choice {
                            option<Talking>("I'll take your challenge!") {
                                attack()
                            }
                            option<Talking>("No thanks.") {
                            }
                        }
                    }
                    option<Talking>("How about that challenge?") {
                        attack()
                    }
                    option<Talking>("Goodbye then.") {
                    }

                }
            }
        }
        option<Amazed>("Er, no.") {
        }
    }
}

suspend fun CharacterContext.attack() {
    npc<Unknown_expression>("Make peace with your god, outerlander!")
    npc.mode = Interact(npc, player, PlayerOption(npc, player, "Attack"))
}