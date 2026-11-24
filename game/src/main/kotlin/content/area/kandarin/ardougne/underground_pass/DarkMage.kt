package content.area.kandarin.ardougne.underground_pass

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class DarkMage : Script {

    init {
        npcOperate("Talk-to", "dark_mage_underground_pass") {
            if (questCompleted("underground_pass")) {
                npc<Neutral>("Oh, it's the liberator of Ardougne. Why do you interrupt me?")
            } else {
                npc<Neutral>("Why do you interrupt me, traveller?")
            }
            player<Quiz>("I just wondered what you're doing?")
            npc<Neutral>("I experiment with dark magic. It's a dangerous craft.")
            if (!inventory.contains("ibans_staff_broken")) {
                return@npcOperate
            }
            player<Quiz>("Could you fix this staff?")
            npc<Shock>("Almighty Zamorak! The Staff of Iban!")
            npc<Neutral>("This truly is dangerous magic, traveller. I can fix it, but it will cost you. The process could kill me.")
            player<Quiz>("How much?")
            npc<Neutral>("200,000 gold pieces. Not a penny less.")
            choice {
                option<Neutral>("Okay then.") {
                    inventory.transaction {
                        remove("coins", 200_000)
                        replace("ibans_staff_broken", "ibans_staff")
                    }
                    if (inventory.transaction.error == TransactionError.None) {
                        player<Happy>("Thanks. I appreciate that.")
                        npc<Neutral>("You be careful with that thing!")
                    } else {
                        player<Sad>("Oops! I'm a bit short.")
                        npc<Angry>("...Fine by me.")
                    }
                }
                option<Angry>("No chance. that's ridiculous!") {
                    npc<Angry>("...Fine by me.")
                }
            }
        }
    }
}
