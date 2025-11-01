package content.area.kandarin.ardougne.west_ardougne

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Clerk : Script {

    val stages = setOf("has_cure_paper", "gave_cure", "freed_elena", "completed", "completed_with_spell")

    init {
        npcOperate("Talk-to", "clerk_west_ardougne") {
            when (quest("plague_city")) {
                "talk_to_bravek" -> talkToBravek()
                // todo find out about "has_cure_paper", "gave_cure", "freed_elena"
                else -> menu()
            }
        }
    }

    suspend fun Player.menu() {
        npc<Neutral>("Hello, welcome to the Civic Office of West Ardougne. How can I help you?")
        choice {
            if (quest("plague_city") == "need_clearance") {
                option<Neutral>("I need permission to enter a plague house.") {
                    permission()
                }
            }
            option<Quiz>("Who is through that door?") {
                throughThatDoor()
            }
            option<Neutral>("I'm just looking thanks.") {
            }
        }
    }

    suspend fun Player.throughThatDoor() {
        npc<Neutral>("The city warder Bravek is in there.")
        player<Quiz>("Can I go in?")
        if (stages.contains(quest("plague_city"))) {
            npc<Neutral>("I suppose so.")
        } else {
            npc<Neutral>("He has asked not to be disturbed.")
            if (quest("plague_city") == "need_clearance") {
                choice {
                    option<Angry>("This is urgent though! Someone's been kidnapped!") {
                        urgent()
                    }
                    option<Neutral>("Okay, I'll leave him alone.") {
                    }
                    option<Quiz>("Do you know when he will be available?") {
                        npc<Neutral>("Oh I don't know, an hour or so maybe.")
                    }
                }
            }
        }
    }

    suspend fun Player.permission() {
        npc<Neutral>("Rather you than me! The mourners normally deal with that stuff, you should speak to them. Their headquarters are right near the city gate.")
        choice {
            option<Neutral>("I'll try asking them then.") {
            }
            option<Quiz>("Surely you don't let them run everything for you?") {
                npc<Neutral>("Well, they do know what they're doing here. If they did start doing something badly Bravek, the city warder, would have the power to override them. I can't see that happening though.")
                choice {
                    option<Neutral>("I'll try asking them then.") {
                    }
                    option<Quiz>("Can I speak to Bravek anyway?") {
                        npc<Neutral>("He has asked not to be disturbed.")
                        choice {
                            option<Angry>("This is urgent though! Someone's been kidnapped!") {
                                urgent()
                            }
                            option<Neutral>("Okay, I'll leave him alone.") {
                            }
                            option<Quiz>("Do you know when he will be available?") {
                                npc<Neutral>("Oh I don't know, an hour or so maybe.")
                            }
                        }
                    }
                }
            }
            option<Angry>("This is urgent though! Someone's been kidnapped!") {
                urgent()
            }
        }
    }

    suspend fun Player.urgent() {
        npc<Neutral>("I'll see what I can do I suppose.")
        npc<Neutral>("Mr Bravek, there's a man here who really needs to speak to you.")
        set("plague_city", "talk_to_bravek")
        npc<Uncertain>("bravek", "I suppose they can come in then. If they keep it short.")
    }

    suspend fun Player.talkToBravek() {
        npc<Neutral>("Bravek will see you now but keep it short!")
        player<Happy>("Thanks, I won't take much of his time.")
    }
}
