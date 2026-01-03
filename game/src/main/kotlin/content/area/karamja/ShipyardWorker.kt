package content.area.karamja

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class ShipyardWorker : Script {

    init {
        npcOperate("Talk-to", "shipyard_guard") {
            when (random.nextInt(12)) {
                0 -> {
                    player<Neutral>("Hello.")
                    player<Neutral>("You look busy!")
                    npc<Sad>("We need double the men to get this order out on time!")
                }
                1 -> {
                    player<Neutral>("Hello.")
                    npc<Happy>("Hello matey!")
                    player<Neutral>("How are you?")
                    npc<Sad>("Tired!")
                    player<Neutral>("You shouldn't work so hard!")
                }
                2 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>("Hello there, are you too lazy to work as well?")
                    player<Confused>("Something like that.")
                    npc<Happy>("I'm just sun bathing!")
                }
                3 -> {
                    player<Neutral>("Hello.")
                    npc<Angry>("Ouch!")
                    player<Confused>("What's wrong?!")
                    npc<Sad>("I cut my finger!")
                    npc<Sad>("Do you have a bandage?")
                    player<Sad>("I'm afraid not.")
                    npc<Neutral>("That's ok, I'll use my shirt.")
                }
                4 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>("What do you want?")
                    player<Angry>("Is that any way to talk to your new superior?")
                    npc<Shock>("Oh, I'm sorry, I didn't realise!")
                }
                5 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>("Can I help you?")
                    player<Neutral>("I'm just looking around.")
                    npc<Neutral>("Well there's plenty of work to be done, so if you don't mind...")
                    player<Neutral>("Of course. Sorry to have disturbed you.")
                }
                6 -> {
                    player<Neutral>("Hello.")
                    npc<Angry>("I've had enough of this!")
                    player<Shock>("What?")
                    npc<Angry>("Breaking my back for pennies! It's just not on!")
                }
                7 -> {
                    player<Neutral>("Hello.")
                    player<Neutral>("Looks like hard work?")
                    npc<Neutral>("I like to keep busy.")
                }
                8 -> {
                    player<Neutral>("Hello.")
                    player<Neutral>("What are you building?")
                    npc<Laugh>("Are you serious?")
                    player<Laugh>("Of course not! You're obviously building a boat.")
                }
                9 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>("No time to talk we've a fleet to build!")
                }
                10 -> {
                    player<Neutral>("Hello.")
                    player<Neutral>("How are you?")
                    npc<Angry>("Too busy to waste time gossiping!")
                    player<Shock>("Touchy!")
                }
                11 -> {
                    player<Neutral>("Hello.")
                    player<Happy>("Quite a few ships you're building!")
                    npc<Happy>("This is just the start! The completed fleet will be awesome!")
                }
            }
        }

        objectOperate("Open", "gate_shipyard_north_closed,gate_shipyard_south_closed") {
            statement("The gate is locked.")
        }
    }
}
