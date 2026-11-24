package content.area.fremennik_province.keldagrim

import content.entity.player.bank.bank
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Dondakan's boatman ships gold ore from the Arzinian mine to the player's bank for a cut.
 * https://runescape.wiki/w/Transcript:Dwarven_Boatman_(ore_hauler)
 */
class OreHaulerBoatman : Script {

    init {
        npcOperate("Talk-to", "dwarven_boatman_keldagrim_4") {
            if (get("ore_hauler_introduced", false)) {
                npc<Quiz>("Do you have any gold ore that you want me to take to the bank in the city?")
                deliver()
                return@npcOperate
            }
            player<Quiz>("Hello there. I don't think I've seen you here before.")
            npc<Neutral>("Oh, Dondakan's employed me recently. He needs a ship to take all his gold to the city.")
            player<Quiz>("The city? Can you take me there?")
            npc<Neutral>("There is only space on my boat for two things... me, and the gold. But if you want, I can take your gold ore to the city and bring it to the bank.")
            player<Happy>("That's a good service! Will it cost me anything?")
            npc<Neutral>("Only a little bit. One piece of gold ore for every shipment, and one piece of gold ore for every five pieces of ore on top of that.")
            player<Quiz>("So... if I had ten pieces of ore, how much would you take?")
            npc<Neutral>("That would be three pieces. One for the shipment, two for the load. Seven pieces would go into your bank account. So, are you interested?")
            set("ore_hauler_introduced", true)
            deliver()
        }

        npcOperate("Deliver-gold", "dwarven_boatman_keldagrim_4") {
            deliver()
        }
    }

    private suspend fun Player.deliver() {
        val ores = inventory.count("gold_ore")
        if (ores == 0) {
            player<Sad>("I don't have any, I'm afraid.")
            return
        }
        if (ores == 1) {
            player<Neutral>("Well, I have this one piece of ore...")
            npc<Neutral>("That wouldn't do you much good, I'm afraid. My minimum fee is one piece of gold ore, there wouldn't be anything left for you.")
            return
        }
        choice {
            option("Yes, please take my gold to the city.") {
                player<Happy>("Yes, please take my gold to the city. I've got $ores pieces of ore right here.")
                val fee = fee(ores)
                npc<Neutral>("Let's see, if I take that to the bank for you, I'd keep $fee for myself and bring ${ores - fee} to the bank for you. Does that sound fair?")
                choice {
                    option("Yes.") {
                        player<Happy>("Yes, it does.")
                        npc<Happy>("Excellent, it's been good doing business with you! I'll take these to the city with my next shipment.")
                        ship(ores, fee)
                    }
                    option("No.") {
                        player<Neutral>("No, that's far too expensive for me.")
                    }
                }
            }
            option<Neutral>("No, I'll hang on to these myself.")
            if (equipped(EquipSlot.Ring).id == "ring_of_charos_a") {
                option("[Charm] It all seems a bit expensive, really.") {
                    player<Quiz>("It all seems a bit expensive, really.")
                    npc<Neutral>("I don't know... it's just what Dondakan told me, if someone wanted me to ship their gold.")
                    player<Quiz>("There's plenty of gold coming from that rock now, surely you can lower the price somewhat?")
                    val fee = charmedFee(ores)
                    npc<Neutral>("Alright. Tell you what, I'll only keep $fee for myself and bring ${ores - fee} ores to the bank for you. Deal?")
                    choice {
                        option("Yes.") {
                            player<Happy>("Yes, much appreciated.")
                            npc<Happy>("Excellent, it's been good doing business with you!")
                            ship(ores, fee)
                        }
                        option("No.") {
                            player<Neutral>("No, I'll hang on to these myself, thanks.")
                        }
                    }
                }
            }
        }
    }

    private fun Player.ship(ores: Int, fee: Int) {
        inventory.transaction {
            remove("gold_ore", ores)
            link(bank).add("gold_ore", ores - fee)
        }
        if (inventory.transaction.error != TransactionError.None) {
            message("Your bank is too full to take any more gold ore.") // TODO proper message
        }
    }

    companion object {
        // Charges 20% rounded up, or 10% with a ring of charos (a) https://runescape.wiki/w/Dondakan%27s_mine?oldid=4286432
        fun fee(ores: Int) = (ores + 4) / 5

        fun charmedFee(ores: Int) = (ores + 9) / 10
    }
}
