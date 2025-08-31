package content.area.asgarnia.goblin_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Script
import world.gregs.voidps.type.random
import world.gregs.voidps.type.sub.Option

class Goblins {

    @Option("Talk-to", "goblin_*_red")
    suspend fun red(player: Player, npc: NPC) = player.talkWith(npc) {
        when (random.nextInt(0, 2)) {
            0 -> {
                npc<Neutral>("Red armour best!")
                choice {
                    option<Uncertain>("Why is red best?") {
                        npc<Afraid>("Cos General Bentnoze says so, and he bigger than me.")
                    }
                    option<Uncertain>("Err, okay.")
                }
            }
            1 -> {
                player<Quiz>("Why are you fighting?")
                npc<Frustrated>("He wearing green armour! General Bentnoze tell us wear red!")
                npc<Frustrated>(npcId = "goblin_staff_green", "But General Wartface say we must wear green!")
            }
        }
    }

    @Option("Talk-to", "goblin_*_green")
    suspend fun green(player: Player, npc: NPC) = player.talkWith(npc) {
        when (random.nextInt(0, 2)) {
            0 -> {
                npc<Neutral>("green armour best!")
                choice {
                    option<Uncertain>("Why is green best?") {
                        npc<Afraid>("Cos General Wartface says so, and he bigger than me.")
                    }
                    option<Uncertain>("Err, okay.")
                }
            }

            1 -> {
                player<Quiz>("Why are you fighting?")
                npc<Frustrated>("He wearing red armour! General Wartface tell us wear green!")
                npc<Frustrated>(npcId = "goblin_shield_battleaxe_red", "But General Bentnoze say we must wear red!")
            }
        }
    }

    @Option("Talk-to", "grubfoot*")
    suspend fun talk(player: Player, npc: NPC) = player.talkWith(npc) {
        npc<Sad>("Grubfoot wear red armour! Grubfoot wear green armour!")
        npc<Quiz>("Why they not make up their minds?")
        npc<Frustrated>(npcId = "general_bentnoze_rfd", "Shut up Grubfoot!")
    }

}
