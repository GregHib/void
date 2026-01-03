package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class Haakon : Script {

    val validStages = setOf("tell_gudrun", "write_poem", "more_poem", "one_more_poem", "poem_done", "poem", "recital", "gunnars_ground")

    init {
        npcOperate("Talk-to", "haakon_the_champion") { (target) ->
            menu(target)
        }
    }

    suspend fun Player.menu(target: NPC) {
        npc<Angry>("I am Haakon, champion of this village. Do you seek to challenge me?")
        choice {
            option<Idle>("I challenge you!") {
                attack(target)
            }
            if (validStages.contains(quest("gunnars_ground"))) {
                option<Idle>("You argued with Gunthor.") {
                    npc<Frustrated>("There is no argument. I honour my father and my ancestors.")
                    choice {
                        option<Idle>("Don't you want to settle permanently?") {
                            npc<Angry>("You test my patience by quuestioning my loyalty to my chieftain. Take up my challenge, outerlander, that I might honourably split your skull open..")
                            choice {
                                option<Idle>("I'll take your challenge!") {
                                    attack(target)
                                }
                                option<Idle>("No thanks.")
                            }
                        }
                        option<Idle>("How about that challenge?") {
                            attack(target)
                        }
                        option<Idle>("Goodbye then.")
                    }
                }
            }
            option<Amazed>("Er, no.")
        }
    }

    suspend fun Player.attack(target: NPC) {
        npc<Mad>("Make peace with your god, outerlander!")
        target.interactPlayer(this, "Attack")
    }
}
