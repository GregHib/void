package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class HoneyBadger : Script {
    init {
        npcOperate("Interact", "honey_badger_familiar") {
            when (random.nextInt(5)) {
                0 -> {
                    npc<Neutral>("*An outpouring of sanity-straining abuse*")
                    player<Happy>("Why do I talk to you again?")
                }
                1 -> {
                    npc<Neutral>("*An outpouring of spittal-flecked insults.*")
                    player<Happy>("Why do I talk to you again?")
                }
                2 -> {
                    npc<Neutral>("*A lambasting of visibly illustrated obscenities.*")
                    player<Happy>("Why do I talk to you again?")
                }
                3 -> {
                    npc<Neutral>("*A tirade of biologically questionable threats*")
                    player<Happy>("Why do I talk to you again?")
                }
                4 -> {
                    npc<Neutral>("*A stream of eye-watering crudities*")
                    player<Happy>("Why do I talk to you again?")
                }
            }
        }
    }
}
