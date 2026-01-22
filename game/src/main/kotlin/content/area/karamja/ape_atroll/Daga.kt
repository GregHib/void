package content.area.karamja.ape_atroll

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Daga : Script {

    init {

        npcOperate("Talk-to", "daga") {

            val amulet = equipped(EquipSlot.Amulet)

            if (amulet.id == "monkeyspeak_amulet") {

                npc<Shifty>("daga", "Would you like to buy or sell some scimitars?")

                choice {

                    option("Yes please.") {
                        player<Shifty>("Yes, please.")
                        openShop("dagas_scimitar_smithy")
                    }

                    option("No, thanks.") {
                        player<Shifty>("No, thanks.")
                    }

                    option("Do you have any Dragon Scimitars in stock?") {
                        player<Shifty>("Do you have any Dragon Scimitars in stock?")
                        npc<Shifty>(
                            "daga",
                            "It just so happens I recently got a fresh delivery. <br>Do you want to buy one?",
                        )
                        choice {
                            option("Yes.") {
                                player<Shifty>("Yes, please.")

                                inventory.transaction {
                                    remove("coin", 100_000)
                                    add("dragon_scimitar")
                                }
                                when (inventory.transaction.error) {
                                    is TransactionError.Full -> {
                                        inventoryFull()
                                        npc<Shifty>("daga", "Sorry, you don't have enough space in your inventory.")
                                    }

                                    TransactionError.None -> {
                                        npc<Shifty>("daga", "There you go. Pleasure doing business with you.")
                                    }

                                    else -> npc<Shifty>(
                                        "daga",
                                        "Sorry, you don't have enough coins. <br>It costs 100,000 gold coins.",
                                    )
                                }

                                option("No.") {
                                    player<Shifty>("No.")
                                }
                            }
                        }
                    }
                }
            } else {
                npc<Shifty>("daga", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }

        npcOperate("Trade", "daga") {
            val amulet = equipped(EquipSlot.Amulet)
            if (amulet.id == "monkeyspeak_amulet") {
                openShop("dagas_scimitar_smithy")
            } else {
                npc<Shifty>("daga", "Ook! Ah Uh Ah! Ook Ook! Ah!")
            }
        }
    }
}
