package world.gregs.voidps.world.map.goblin_village

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "goblin_*_red") {
    when (random.nextInt(0, 2)) {
        0 -> {
            npc<Talking>("Red armour best!")
            choice {
                option<Uncertain>("Why is red best?") {
                    npc<Afraid>("Cos General Bentnoze says so, and he bigger than me.")
                }
                option<Uncertain>("Err, okay.")
            }
        }
        1 -> {
            player<Unsure>("Why are you fighting?")
            npc<Angry>("He wearing green armour! General Bentnoze tell us wear red!")
            npc<Angry>(npcId = "goblin_staff_green", "But General Wartface say we must wear green!")
        }
    }
}

npcOperate("Talk-to", "goblin_*_green") {
    when (random.nextInt(0, 2)) {
        0 -> {
            npc<Talking>("green armour best!")
            choice {
                option<Uncertain>("Why is green best?") {
                    npc<Afraid>("Cos General Wartface says so, and he bigger than me.")
                }
                option<Uncertain>("Err, okay.")
            }
        }

        1 -> {
            player<Unsure>("Why are you fighting?")
            npc<Angry>("He wearing red armour! General Wartface tell us wear green!")
            npc<Angry>(npcId = "goblin_shield_battleaxe_red", "But General Bentnoze say we must wear red!")
        }
    }
}

npcOperate("Talk-to", "grubfoot*") {
    npc<Sad>("Grubfoot wear red armour! Grubfoot wear green armour!")
    npc<Unsure>("Why they not make up their minds?")
    npc<Angry>(npcId = "general_bentnoze_rfd", "Shut up Grubfoot!")
}
