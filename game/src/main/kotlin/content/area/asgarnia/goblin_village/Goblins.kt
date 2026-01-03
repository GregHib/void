package content.area.asgarnia.goblin_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Goblins : Script {

    init {
        npcOperate("Talk-to", "goblin_*_red") {
            when (random.nextInt(0, 2)) {
                0 -> {
                    npc<Idle>("Red armour best!")
                    choice {
                        option<Confused>("Why is red best?") {
                            npc<Scared>("Cos General Bentnoze says so, and he bigger than me.")
                        }
                        option<Confused>("Err, okay.")
                    }
                }
                1 -> {
                    player<Quiz>("Why are you fighting?")
                    npc<Frustrated>("He wearing green armour! General Bentnoze tell us wear red!")
                    npc<Frustrated>(npcId = "goblin_staff_green", "But General Wartface say we must wear green!")
                }
            }
        }

        npcOperate("Talk-to", "goblin_*_green") {
            when (random.nextInt(0, 2)) {
                0 -> {
                    npc<Idle>("green armour best!")
                    choice {
                        option<Confused>("Why is green best?") {
                            npc<Scared>("Cos General Wartface says so, and he bigger than me.")
                        }
                        option<Confused>("Err, okay.")
                    }
                }

                1 -> {
                    player<Quiz>("Why are you fighting?")
                    npc<Frustrated>("He wearing red armour! General Wartface tell us wear green!")
                    npc<Frustrated>(npcId = "goblin_shield_battleaxe_red", "But General Bentnoze say we must wear red!")
                }
            }
        }

        npcOperate("Talk-to", "grubfoot*") {
            npc<Disheartened>("Grubfoot wear red armour! Grubfoot wear green armour!")
            npc<Quiz>("Why they not make up their minds?")
            npc<Frustrated>(npcId = "general_bentnoze_rfd", "Shut up Grubfoot!")
        }
    }
}
