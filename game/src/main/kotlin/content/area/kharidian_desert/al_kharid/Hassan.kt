package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.sound.jingle
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.softQueue

class Hassan : Script {

    init {
        npcOperate("Talk-to", "hassan") {
            when (quest("prince_ali_rescue")) {
                "unstarted" -> {
                    npc<Talk>("Greetings! I am Hassan, Chancellor to the Emir of Al Kharid.")
                    choice {
                        anyHelp()
                        tooHot()
                        killWarriors()
                        option<Talk>("I'd better be off.")
                    }
                }
                "prince_ali_disguise" -> {
                    npc<Talk>("You have the eternal gratitude for the Emir for rescuing his son. I am authorised to pay you 700 coins.")
                    if (!inventory.add("coins")) {
                        statement("Leela tries to give you a reward, but you don't have enough room for it.") // TODO proper message
                        return@npcOperate
                    }
                    jingle("quest_complete_1")
                    refreshQuestJournal()
                    AuditLog.event(this, "quest_completed", "prince_ali_rescue")
                    set("prince_ali_rescue", "completed")
                    inc("quest_points", 3)
                    message("Congratulations! Quest complete!")
                    softQueue("quest_complete", 1) {
                        questComplete(
                            "Prince Ali Rescue",
                            "3 Quest Points",
                            "700 coins",
                            item = "coins_8",
                        )
                    }
                    clear("prince_ali_rescue_key_made")
                    clear("prince_ali_rescue_key_given")
                    clear("prince_ali_rescue_leela")
                }
                "completed" -> npc<Happy>("Thank you for being a friend to Al Kharid. You are always welcome here.")
                else -> npc<Talk>("Hello again. I hear you have agreed to help rescue Prince Ali. On behalf of the Emir, I will have a reward ready for you upon your success.")
            }
        }
    }

    fun ChoiceBuilder2.killWarriors() {
        option<Quiz>("Do you mind if I just kill your warriors?") {
            npc<Uncertain>("Kill our warriors? I assume this is some sort of joke?")
            player<Quiz>("I'll take that as a no. Forget I asked.")
            choice {
                anyHelp()
                tooHot()
                option("I'd better be off.")
            }
        }
    }

    fun ChoiceBuilder2.anyHelp() {
        option<Happy>("Can I help you? You must need some help here in the desert.") {
            set("prince_ali_rescue", "osman")
            npc<Uncertain>("I need the services of someone, yes. If you are interested, see the spymaster, Osman. I manage the finances here. Come to me when you need payment.")
        }
    }

    fun ChoiceBuilder2.tooHot() {
        option<Upset>("It's just too hot here. How can you stand it?") {
            npc<Talk>("We manage, in our humble way. We are a wealthy town and we have water. It cures many thirsts.")
            inventory.add("jug_of_water")
            statement("The chancellor hands you some water.")
            choice {
                anyHelp()
                killWarriors()
                option<Talk>("I'd better be off.")
            }
        }
    }
}
