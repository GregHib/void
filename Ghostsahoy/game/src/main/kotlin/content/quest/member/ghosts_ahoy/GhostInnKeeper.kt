package content.quest.member.ghosts_ahoy

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class GhostInnKeeper : Script {
    init {
        npcOperate("Talk-To", "ahoy_ghost_innkeeper") {
            if (!checkGhostspeak()) return@npcOperate
            player<Happy>("Hello there!")
            npc<Neutral>("Greetings, traveller. Welcome to 'The Green Ghost' Tavern. What can I do you for?")
            menu()
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Quiz>("Can I buy a beer?") {
                npc<Neutral>(
                    "Sorry, but our pumps dried up over half a century ago. We of this village " +
                        "do not have much of a thirst these days.",
                )
            }
            option<Quiz>("Can I hear some gossip?") {
                npc<Neutral>("I suppose, as long as you keep it to yourself...")
                npc<Neutral>(
                    "You see Gravingas out there in the marketplace? He speaks for the silent " +
                        "majority of Port Phasmatys, for those of us who would prefer to pass " +
                        "over into the next world.",
                )
                if (ghosts_ahoy == 0) {
                    npc<Neutral>(
                        "But old Gravingas is far too obvious in his methods. Now Velorina, " +
                            "she's a ghost of a different colour altogether. If you feel like " +
                            "helping our cause at all, go speak to Velorina.",
                    )
                }
            }
            option<Neutral>("Do you have any jobs I can do?") {
                if (ownsItem("bedsheet")) {
                    npc<Neutral>("Well, you could take that bedsheet through to Robin like I asked.")
                    return@option
                }
                npc<Neutral>(
                    "Yes, actually, I do. We have a very famous Master Bowman named Robin staying " +
                        "with us at the moment. Could you take him some clean bed linen for me?",
                )
                choice {
                    option<Happy>("Yes, I'd be delighted.") {
                        addOrDrop("bedsheet")
                        npc<Neutral>(
                            "Oh, thank you. Be careful with that Robin, though - he's far too " +
                                "full of himself, that one.",
                        )
                    }
                    option<Neutral>("No, I didn't mean a job like that.")
                }
            }
            option<Neutral>("Nothing, thanks.")
        }
    }
}
