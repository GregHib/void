package world.gregs.voidps.world.map.goblin_village

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import kotlin.random.Random


fun redGoblins(id: String) = id.startsWith("goblin_") && id.endsWith("_red")
fun greenGoblins(id: String) = id.startsWith("goblin_") && id.endsWith("_green")

on<NPCOption>({ operate && redgoblins(target.id) && option == "Talk-to" }) { player: Player ->
    when (Random.nextInt(0, 2)) {
        0 -> {
            npc<Talking>("Red armour best!", largeHead = true)
            choice {
                option<Uncertain>("Why is red best?"){
                    npc<Afraid>("Cos General Bentnoze says so, and he bigger than me.", largeHead = true)
                }
                option<Uncertain>("Err, okay."){
                }
            }
        }
        1 -> {
            player<Unsure>("Why are you fighting?")
            npc<Angry>("""
                He wearing green armour! General Bentnoze tell us
                wear red!
             """, largeHead = true)
            npc<Angry>(npcId = "goblin_staff_green","But General Wartface say we must wear green!", largeHead = true)
        }
    }
}


on<NPCOption>({ operate && greengoblins(target.id) && option == "Talk-to" }) { player: Player ->
    when (Random.nextInt(0, 2)) {
        0 -> {
            npc<Talking>("green armour best!", largeHead = true)
            choice {
                option<Uncertain>("Why is green best?") {
                    npc<Afraid>("Cos General Wartface says so, and he bigger than me.", largeHead = true)
                }
                option<Uncertain>("Err, okay.") {
                }
            }
        }

        1 -> {
            player<Unsure>("Why are you fighting?")
            npc<Angry>("""
                He wearing red armour! General Wartface tell us wear
                green!
         """, largeHead = true)
            npc<Angry>(npcId = "goblin_shield_battleaxe_red", "But General Bentnoze say we must wear red!", largeHead = true)
        }
    }
}

on<NPCOption>({ operate && target.id == "grubfoot" && option == "Talk-to" }) { player: Player ->
    npc<Sad>("""
        Grubfoot wear red armour! Grubfoot wear green
        armour!
    """, largeHead = true)
    npc<Unsure>("Why they not make up their minds?", largeHead = true)
    npc<Angry>(npcId = "general_bentnoze","Shut up Grubfoot!", largeHead = true)
}
