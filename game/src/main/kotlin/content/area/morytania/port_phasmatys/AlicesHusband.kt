package content.area.morytania.port_phasmatys

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.member.ghosts_ahoy.checkGhostspeak
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * Sells undead chickens for ecto-tokens after Animal Magnetism.
 * https://runescape.wiki/w/Transcript:Alice%27s_husband
 */
class AlicesHusband : Script {

    init {
        npcOperate("Talk-to", "alices_husband_3") {
            if (!checkGhostspeak()) {
                return@npcOperate
            }
            npc<Happy>("Hello, how can I help you? I'm sellin' if you have ecto-tokens.")
            choice {
                option<Quiz>("Could I buy those chickens now?") {
                    npc<Neutral>("I can hand over a chicken if you give me 10 of them ecto-token thingies per bird.")
                    choice {
                        buyChickens(1, "Could I buy 1 chicken now?")
                        buyChickens(2, "Could I buy 2 chickens?")
                        option<Neutral>("Your animals don't look too healthy, I'll buy elsewhere.") {
                            npc<Laugh>("Har har, how many ghost chicky farmers d'you know, then?")
                        }
                    }
                }
                option<Neutral>("Your animals don't look too healthy.") {
                    animals()
                }
                option<Neutral>("I'm okay, thank you.")
                option<Quiz>("Where could I get more of these ecto-tokens?") {
                    npc<Neutral>("The ghosts I talk to say that the tokens have something to do with the tower just east of here. If you need to collect some I'd try there.")
                }
            }
        }
    }

    private fun ChoiceOption.buyChickens(amount: Int, text: String) {
        option<Quiz>(text) {
            inventory.transaction {
                remove("ecto_token", amount * 10)
                add("undead_chicken", amount)
            }
            when (inventory.transaction.error) {
                TransactionError.None -> if (amount == 1) {
                    npc<Happy>("Ta, some day soon I'll be havin' a nice undead bed to kip in.")
                } else {
                    npc<Happy>("Great! I'm laying away me tokens for some killer cows. That'll learn them bone rustlers.")
                }
                is TransactionError.Full -> if (amount == 1) {
                    npc<Angry>("Turnip-head, come back when you have space.")
                } else {
                    npc<Angry>("Yer sack's too small. Throw away some of that junk yer carting about.")
                }
                else -> npc<Angry>("I'm not a charity here, ya know. Bad enough all you cow-killing folks are a'slaughterin' me beasts. Come back when ya have enough tokens.")
            }
        }
    }

    private suspend fun Player.animals() {
        npc<Neutral>("It's that fountain thingy in the temple to the east. It's turned them all into zombies.")
        player<Quiz>("What use are zombie animals?")
        npc<Neutral>("None at all, mate, except that those worshippers at that temple keep comin' and killin' 'em all for their bones. Don't ask me why.")
        player<Quiz>("But you're a ghost - surely you know something about it.")
        npc<Neutral>("I don't know nuthin' about nuthin'. Oim a simple ghost with simple needs. All I know is, years ago, that temple started glowing green and, a few months later, I woke up dead. That's all there is to it.")
    }
}
