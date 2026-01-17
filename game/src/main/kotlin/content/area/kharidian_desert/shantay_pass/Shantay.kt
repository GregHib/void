package content.area.kharidian_desert.shantay_pass

import content.entity.obj.door.openDoor
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Shantay : Script {

    init {
        npcOperate("Talk-to", "shantay") {
            npc<Neutral>("Hello effendi, I am Shantay.")
            if (get("shantay_state", "new_player") == "new_player") {
                npc<Neutral>("I see you're new. Please read the billboard poster before going into the desert. It'll give yer details on the dangers you can face.")
                set("shantay_state", "returning")
            } else {
                npc<Neutral>("Hello again friend. Please read the billboard poster before going into the desert. It'll give yer details on the dangers you can face.")
            }
            choice {
                whatIsThisPlace()
                shop()
                bye()
                option<Neutral>("I want to buy a Shantay pass for 5 gold coins.") {
                    buyPass()
                }
            }
        }

        npcOperate("Buy-pass", "shantay") {
            buyPass()
        }

        objectOperate("Open", "shantay_prison_door_closed") { (target) ->
            if (get("shantay_state", "new_player") != "jailed") {
                openDoor(target)
                return@objectOperate
            }
            if (tile.x >= 3300) {
                npc<Neutral>("You should be in jail! Well, no doubt the authorities in Port Sarim know what they're doing. But if you get into any more trouble, you'll be stuck back in jail.")
                set("shantay_state", "returning")
            } else {
                message("Shantay saunters over to talk with you.")
                talkWith(NPCs[tile.regionLevel].first { it.id == "shantay" })
                npc<Neutral>("If you want to be let out, you have to pay a fine of five gold. Do you want to pay now?")
                leaveJail()
            }
        }
    }

    private fun ChoiceOption.whatIsThisPlace() {
        option<Neutral>("What is this place?") {
            npc<Neutral>("This is the pass of Shantay. I guard this area with my men. I am responsible for keeping this pass open and repaired.")
            npc<Neutral>("My men and I prevent outlaws from getting out of the desert. And we stop the inexperienced from a dry death in the sands. Which would you say you were?")
            choice {
                option<Neutral>("I am definitely an outlaw, prepare to die!") {
                    npc<Neutral>("Ha, very funny.....")
                    npc<Neutral>("Guards arrest him!")
                    set("shantay_state", "jailed")
                    val guard = findNearestGuard(this)
                    guard?.walkTo(this.tile)
                    delay(1)
                    message("The guards arrest you and place you in the jail.")
                    delay(3)
                    tele(3298, 3124)
                    npc<Neutral>("You'll have to stay in there until you pay the fine of five gold pieces. Do you want to pay now?")
                    leaveJail()
                }
                option<Neutral>("I am a little inexperienced.") {
                    npc<Neutral>("Can I recommend that you purchase a full waterskin and a knife! These items will no doubt save your life. A waterskin will keep water from evaporating in the desert.")
                    npc<Neutral>("And a keen woodsman with a knife can extract the juice from a cactus. Before you go into the desert, it's advisable to wear desert clothes. It's very hot in the desert and you'll surely cook if you wear armour.")
                    npc<Neutral>("To keep the pass bandit free, we charge a small toll of five gold pieces. You can buy a desert pass from me, just ask me to open the shop. You can also use our free banking services by clicking on the chest.")
                    choice {
                        shop()
                        bye()
                        whyCost()
                    }
                }
                option<Neutral>("Er, neither, I'm an adventurer.") {
                    npc<Neutral>("Great, I have just the thing for the desert adventurer. I sell desert clothes which will keep you cool in the heat of the desert. I also sell waterskins so that you won't die in the desert.")
                    npc<Neutral>("A waterskin and a knife help you survive from the juice of a cactus. Use the chest to store your items, we'll take them to the bank. It's hot in the desert, you'll bake in all that armour.")
                    npc<Neutral>("To keep the pass open we ask for 5 gold pieces. And we give you a Shantay Pass, just ask to see what I sell to buy one.")
                    choice {
                        shop()
                        bye()
                        whyCost()
                    }
                }
            }
        }
    }

    private suspend fun Player.leaveJail() {
        choice {
            option<Neutral>("Yes, okay.") {
                npc<Neutral>("Good, I see that you have come to your senses.")
                if (inventory.remove("coins", 5)) {
                    set("shantay_state", "returning")
                    npc<Neutral>("Great Effendi, now please try to keep the peace.")
                    message("Shantay unlocks the door to the cell.")
                } else {
                    npc<Neutral>("You don't have that kind of cash on you I see.")
                    npc<Neutral>("But perhaps you have some in your bank? Or would you prefer the maximum security prison in Port Sarim.")
                    npc<Neutral>("Which is it going to be?")
                    choice {
                        option<Neutral>("I'll pay the fine.") {
                            npc<Neutral>("Okay then..., you'll need access to your bank.")
                            open("bank")
                        }
                        option<Neutral>("No thanks, you're not having my money.") {
                            npc<Neutral>("Very well, I grow tired of you, you'll be taken to a new jail in Port Sarim.")
                            jail()
                        }
                    }
                }
            }
            noThanks()
        }
    }

    private fun ChoiceOption.noThanks() {
        option<Neutral>("No thanks, you're not having my money.") {
            npc<Neutral>("You have a choice. You can either pay five gold pieces or... You can be transported to a maximum security prison in Port Sarim.")
            npc<Neutral>("Will you pay the five gold pieces?")
            choice {
                option<Neutral>("Yes, okay.")
                option("No, do your worst!") {
                    npc<Neutral>("You are to be transported to a maximum security prison in Port Sarim. I hope you've learnt an important lesson from this.")
                    jail()
                }
            }
        }
    }

    private suspend fun Player.jail() {
        open("fade_out")
        delay(2)
        tele(3014, 3180)
        delay(1)
        message("You find yourself in a prison.")
        open("fade_in")
        delay(2)
    }

    fun findNearestGuard(player: Player) = NPCs[player.tile.regionLevel].sortedBy { player.tile.distanceTo(it.tile) }.firstOrNull { it.id == "shantay_guard" }

    private suspend fun Player.buyPass() {
        inventory.transaction {
            remove("coins", 5)
            add("shantay_pass")
        }
        when (inventory.transaction.error) {
            is TransactionError.Full -> npc<Neutral>("Sorry friend, you'll need more inventory space to buy a pass.")
            TransactionError.None -> item("shantay_pass", 400, "You purchase a Shantay Pass.")
            else -> npc<Neutral>("Sorry friend, the Shantay Pass is 5 gold coins. You don't seem to have enough money!")
        }
    }

    private fun ChoiceOption.whyCost() {
        option<Neutral>("Why do I have to pay to go into the desert?") {
            message("Shantay opens his arms wide as if to embrace you.")
            delay(3)
            npc<Neutral>("Effendi, you insult me! I am not interested in making a profit from you! I merely seek to cover my expenses in keeping this pass open.")
            npc<Neutral>("There is repair work to carry out and also the men's wages to consider. For the paltry sum of 5 Gold pieces, I think we offer a great service.")
            choice {
                shop()
                bye()
            }
        }
    }

    private fun ChoiceOption.bye() {
        option<Neutral>("I must be going.") {
            npc<Neutral>("So long...")
        }
    }

    private fun ChoiceOption.shop() {
        option<Neutral>("Can I see what you have to sell please?") {
            npc<Neutral>("Absolutely Effendi!")
            open("shantay_pass_shop")
        }
    }
}
