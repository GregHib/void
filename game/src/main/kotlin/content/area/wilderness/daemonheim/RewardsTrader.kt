package content.area.wilderness.daemonheim

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddCharge.charge
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

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
            val item = it.item
            val base = when {
                item.id.contains("chaotic") -> 200
                item.id.contains("gravite") -> 100
                else -> {
                    npc<Teary>("No good! Take it away.")
                    return@itemOnNPCOperate
                }
            }
            if (!item.def.contains("charges")) {
                npc<Teary>("No good! Take it away.")
                return@itemOnNPCOperate
            }
            val charges = item.charges(this)
            val total = item.def.get<Int>("charges")
            val percentage = (charges / total) * 100
            if (percentage == 100) {
                npc<Teary>("Your item is...uh..usable as it is. Return when there is something to mend.")
                return@itemOnNPCOperate
            }
            val coins = percentage * (base * 10)
            val tokens = percentage * base
            npc<Teary>("Fixing it is possible, yes. The cost is up to you.")
            choice {
                option("${coins * 10}gp.") {
                    inventory.transaction {
                        remove("coins", coins * 10)
                        charge(it.slot, total - charges)
                    }
                    when (inventory.transaction.error) {
                        is TransactionError.Deficient -> TODO()
                        TransactionError.None -> npc<Teary>("All done. Move on. Please...")
                        else -> {}
                    }
                }
                option("${coins}gp and $tokens tokens.") {
                    val current = get("dungeoneering_tokens", 0)
                    if (current < tokens) {
                        message("You don't have enough tokens to recharge that.") // TODO proper message
                        return@option
                    }
                    inventory.transaction {
                        remove("coins", coins * 10)
                        charge(it.slot, total - charges)
                    }
                    when (inventory.transaction.error) {
                        is TransactionError.Deficient -> message("Don't have enough coins") // TODO proper message
                        TransactionError.None -> {
                            npc<Teary>("All done. Move on. Please...")
                        }
                        else -> {}
                    }
                }
                option<Neutral>("No way!")
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
