package content.area.misthalin.dorgesh_kaan

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.random

class Tegdak : Script {
    init {
        // https://runescape.wiki/w/Transcript:Tegdak
        // https://oldschool.runescape.wiki/w/Transcript:Tegdak
        npcOperate("Talk-to", "tegdak_dorgesh_kaan") {
            npc<Happy>("Hello $name! Thanks again for your help with the dig.")
            choice {
                option<Neutral>("What have you learned about the artefacts we dug up?") {
                    artefacts()
                }
                option<Sad>("I lost the ancient goblin mace.") {
                    lostMace()
                }
            }
        }
    }

    private suspend fun Player.artefacts() {
        when (random.nextInt(5)) {
            0 -> npc<Neutral>("When we examined the blade you dug up, we found that the blade is covered in tiny symbols. The scribe thinks he has enough of them to begin understanding the ancient goblin language, but it will take a while for him to decipher it.")
            1 -> npc<Neutral>("Oldak examined the axeblade you dug up and found it had a faint magic aura. It looks like the mace wasn't the only magical weapon our ancestors brought from the surface. In fact, it could be that a lot of their equipment was magical. This means that either the ancient goblins were skilled magical craftsmen, or some other power was supplying them with magical equipment.")
            2 -> npc<Happy>("The helmet you helped to unearth has come in useful. I used it as a visual aid when I went to talk to the children at the nursery about our history. It's amazing that the ancient goblins' eyes were even smaller than a child's!")
            3 -> npc<Neutral>("We used the armour fragment you unearthed to reconstruct the shape of the original armour, and with it the shape of the goblin who wore the armour. This told us a lot about how the goblin body shape changed from its surface form to that of the cave goblins.")
            else -> npc<Neutral>("Bartak used the design of the shield you extracted to recreate a shield like it. Even though the original shield was poorly made, the design itself was a good one. Perhaps there is some important knowledge that we have lost since the early, more war-like days of Dorgesh-Kaan.")
        }
    }

    private suspend fun Player.lostMace() {
        if (questCompleted("land_of_the_goblins")) {
            npc<Neutral>("I know. We found it lying around in the caves near the city, almost as if it had made its way back here and wanted to be found.")
        } else {
            npc<Neutral>("I know. Zanik says she found it lying around in the caves near the city, almost as if it had made its way back here and wanted her to find it.")
        }
        npc<Neutral>("If you want it back, I will sell it to you for 1,000 coins. That should discourage you from losing it again.")
        choice {
            option<Neutral>("Okay.") {
                buyMace()
            }
            option<Neutral>("No way!") {
                npc<Neutral>("Suit yourself.")
            }
        }
    }

    private suspend fun Player.buyMace() {
        if (!inventory.contains("coins", 1000)) {
            player<Sad>("But I'll have to go and get the money.")
            return
        }
        inventory.transaction {
            remove("coins", 1000)
            add("ancient_mace")
        }
        if (inventory.transaction.error != TransactionError.None) {
            player<Sad>("But I should free up some inventory space first.")
            return
        }
        item("ancient_mace", "Tegdak hands you the ancient goblin mace.")
        npc<Neutral>("Look after it well. All the goblins who have handled it say it made them feel uneasy. We want it kept safe, but we don't want it near the city.")
    }
}
