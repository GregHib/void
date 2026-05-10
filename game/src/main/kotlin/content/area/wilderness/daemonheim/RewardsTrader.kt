package content.area.wilderness.daemonheim

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class RewardsTrader : Script {
    init {
        npcOperate("Talk-to", "rewards_trader") {
            npc<Teary>("Oh, hello, I didn't see..")
            player<Quiz>("Hey. I was wondering if you could help me?")
            npc<Teary>("Help? Uh...I'm not sure that I can...uh...")
            choice {
                option<Quiz>("Who are you?") {
                    npc<Teary>("I'm...I used to be...")
                    player<Quiz>("Yes?")
                    npc<Teary>("I just handle the...uh...equipment.")
                    player<Quiz>("I mean, who are you? Like, what is your name?")
                    npc<Teary>("A name? I...uh...")
                    choice {
                        option<Angry>("You are driving me crazy.") {
                            npc<Teary>("I'm sorry. I'm...")
                            player<Angry>("Yes?")
                            npc<Teary>("They called me Ma...")
                            player<Frustrated>("Malcolm? Mandrake? Ma...um..Mango?")
                            npc<Teary>("Uh, no, those aren't quite right.")
                            choice {
                                equipment()
                                doneWithYou()
                            }
                        }
                        equipment()
                        doneWithYou()
                    }
                }
                doneWithYou()
            }
        }

        itemOnNPCOperate("*", "rewards_trader") {
            npc<Teary>("No good! Take it away.")
            npc<Teary>("Fixing it is possible, yes. The cost is up to you.")
            npc<Teary>("Your item is...uh..usable as it is. Return when there is something to mend.")
            choice {
                option("120000gp.") {

                    npc<Teary>("All done. Move on. Please...")
                }
                option("12000gp and 12000 tokens.") {

                }
                option<Idle>("No way!")
            }
        }

        npcOperate("Shop", "rewards_trader") {
            open("daemonheim_rewards")
        }

        npcOperate("Recharge", "rewards_trader") {
            npc<Teary>("I mend things, I guess.")
            whatCanYouMend()
        }
    }

    private fun ChoiceOption.equipment() {
        option<Quiz>("You mentioned something about equipment?") {
            npc<Teary>("Yes...uh...I did, you're quite right. I sell things, mend things. What interests you?")
            choice {
                option("Let's see what you sell, then.") {
                    open("daemonheim_rewards")
                }
                option("What exactly can you mend?") {
                    whatCanYouMend()
                }
                doneWithYou()
            }

        }
    }

    private suspend fun Player.whatCanYouMend() {
        player<Quiz>("What exactly can you mend?")
        npc<Teary>("Weapons, I guess. I became good at mending in...there. If you buy something from me, I should be able to repair it if it's...uh...degraded. Show it to me and I'll see what can be done.")
        npc<Teary>("I feel...like I can trust you. Let me tell you something: I have a friend in...there, someone who brings me tools and items from that place. That is how I can offer you services that others...can't. Don't tell anyone. Please.")
    }

    private fun ChoiceOption.doneWithYou() {
        option<Idle>("I'm done with you.") {
            npc<Teary>("Fine, fine, fine. I'll be here.")
        }
    }
}