package content.area.misthalin.edgeville.monastery

import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.smithing.SpiritShieldSigils
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class BrotherBordiss : Script {

    private val cost = 1_500_000

    init {
        npcOperate("Talk-to", "brother_bordiss,brother_bordiss_falador") {
            npc<Happy>("Hello there. What can I do for you?")
            choice {
                val sigil = SpiritShieldSigils.SIGILS.firstOrNull { inventory.contains(it) }
                if (sigil != null) {
                    option<Quiz>("Can you do anything with this sigil?") {
                        offerAttachment(sigil)
                    }
                }
                option<Quiz>("What is a dwarf doing in a monastery?") {
                    npc<Neutral>("Most of my kin follow Guthix, it's true, but I found Saradomin's teachings spoke to me. Balance is all very well; I would rather leave the world better than I found it.")
                    npc<Neutral>("That is what my power station is for, and I have a forge here to keep my hands busy in the meantime.")
                }
                option<Neutral>("Nothing, thanks.")
            }
        }
    }

    private suspend fun Player.offerAttachment(sigil: String) {
        npc<Amazed>("Now that is a sigil of the corporeal beast! I never thought I'd hold one of those.")
        npc<Happy>("If you have a blessed spirit shield, I could mount that sigil onto it for you. It is fiddly work, mind, and my furnace does not stoke itself.")
        npc<Neutral>("I would need 1,500,000 coins for the trouble. What do you say?")
        choice {
            option<Happy>("Yes, please!") {
                attach(sigil)
            }
            option<Sad>("That's a bit expensive!") {
                npc<Neutral>("It is a fair price for work this delicate. Come and find me again if you change your mind.")
            }
            option<Neutral>("No, thanks.")
        }
    }

    private suspend fun Player.attach(sigil: String) {
        if (!inventory.contains(SpiritShieldSigils.SHIELD)) {
            npc<Neutral>("You'll need a blessed spirit shield for me to mount it on. Come back and see me once you have one.")
            return
        }
        if (!inventory.contains("coins", cost)) {
            player<Sad>("I don't seem to have enough coins, I will return once I do.")
            return
        }
        val shield = SpiritShieldSigils.shield(sigil)
        val success = inventory.transaction {
            remove(sigil)
            remove(SpiritShieldSigils.SHIELD)
            remove("coins", cost)
            add(shield)
        }
        if (!success) {
            return
        }
        item(shield, "Bordiss heats the shield through and works the sigil into it, and hands the finished shield back to you.")
        npc<Happy>("There you go. Bear it well, and mind what you point it at.")
    }
}
