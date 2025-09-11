package content.area.karamja

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.random

@Script
class ShipyardWorker {

    init {
        npcOperate("Talk-to", "shipyard_guard") {
            when (random.nextInt(12)) {
                0 -> {
                    player<Talk>("Hello.")
                    player<Talk>("You look busy!")
                    npc<Upset>("We need double the men to get this order out on time!")
                }
                1 -> {
                    player<Talk>("Hello.")
                    npc<Happy>("Hello matey!")
                    player<Talk>("How are you?")
                    npc<Upset>("Tired!")
                    player<Talk>("You shouldn't work so hard!")
                }
                2 -> {
                    player<Talk>("Hello.")
                    npc<Talk>("Hello there, are you too lazy to work as well?")
                    player<Uncertain>("Something like that.")
                    npc<Happy>("I'm just sun bathing!")
                }
                3 -> {
                    player<Talk>("Hello.")
                    npc<Angry>("Ouch!")
                    player<Uncertain>("What's wrong?!")
                    npc<Upset>("I cut my finger!")
                    npc<Upset>("Do you have a bandage?")
                    player<Upset>("I'm afraid not.")
                    npc<Talk>("That's ok, I'll use my shirt.")
                }
                4 -> {
                    player<Talk>("Hello.")
                    npc<Talk>("What do you want?")
                    player<Angry>("Is that any way to talk to your new superior?")
                    npc<Surprised>("Oh, I'm sorry, I didn't realise!")
                }
                5 -> {
                    player<Talk>("Hello.")
                    npc<Talk>("Can I help you?")
                    player<Talk>("I'm just looking around.")
                    npc<Talk>("Well there's plenty of work to be done, so if you don't mind...")
                    player<Talk>("Of course. Sorry to have disturbed you.")
                }
                6 -> {
                    player<Talk>("Hello.")
                    npc<Angry>("I've had enough of this!")
                    player<Surprised>("What?")
                    npc<Angry>("Breaking my back for pennies! It's just not on!")
                }
                7 -> {
                    player<Talk>("Hello.")
                    player<Talk>("Looks like hard work?")
                    npc<Talk>("I like to keep busy.")
                }
                8 -> {
                    player<Talk>("Hello.")
                    player<Talk>("What are you building?")
                    npc<Chuckle>("Are you serious?")
                    player<Chuckle>("Of course not! You're obviously building a boat.")
                }
                9 -> {
                    player<Talk>("Hello.")
                    npc<Talk>("No time to talk we've a fleet to build!")
                }
                10 -> {
                    player<Talk>("Hello.")
                    player<Talk>("How are you?")
                    npc<Angry>("Too busy to waste time gossiping!")
                    player<Surprised>("Touchy!")
                }
                11 -> {
                    player<Talk>("Hello.")
                    player<Happy>("Quite a few ships you're building!")
                    npc<Happy>("This is just the start! The completed fleet will be awesome!")
                }
            }
        }

        objectOperate("Open", "gate_shipyard_north_closed", "gate_shipyard_south_closed") {
            statement("The gate is locked.")
            cancel()
        }
    }
}
