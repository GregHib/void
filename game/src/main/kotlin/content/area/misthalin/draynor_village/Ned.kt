package content.area.misthalin.draynor_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Ned : Script {

    val floorItems: FloorItems by inject()

    init {
        npcOperate("Talk-to", "ned") {
            npc<Happy>("Why, hello there, ${if (male) "lad" else "lass"}. Me friends call me Ned. I was a man of the sea, but it's past me now. Could I be making or selling you some rope?")
            choice {
                otherThings(this@npcOperate)
                rope()
                option<Talk>("No thanks, Ned. I don't need any.") {
                    npc<Talk>("Well, old Neddy is always here if you do. Tell your friends. I can always be using the business.")
                }
            }
        }
    }

    // TODO add achievement dialogue

    fun ChoiceBuilder2.wig() {
        option<Quiz>("How about some sort of wig?") {
            npc<Talk>("Well... that's an interesting thought. Yes, I think I could do something. Give me three balls of wool and I might be able to do it.")
            if (!holdsItem("ball_of_wool", 3)) {
                player<Happy>("Great, I will get some. I think a wig would be useful.")
                return@option
            }
            choice {
                option<Talk>("I have them here. Please make me a wig.") {
                    npc<Talk>("Okay, I'll have a go.")
                    inventory.transaction {
                        remove("ball_of_wool", 3)
                        add("wig_grey")
                    }
                    item("wig_grey", 400, "Ned gives you a pretty good wig.")
                    npc<Happy>("Here you go. How's that for a quick effort? Not bad I think!")
                    player<Happy>("Thanks Ned. There's more to you than meets the eye.")
                }
                option<Talk>("Actually, I don't need one right now.") {
                    npc<Happy>("Fair enough.")
                }
            }
        }
    }

    fun ChoiceBuilder2.otherThings(player: Player) {
        when (player.quest("prince_ali_rescue")) {
            "leela", "equipment", "joe_one_beer", "joe_two_beers", "joe_three_beers", "tie_up_lady_keli" -> {
                option<Quiz>("Could you make other things apart from rope?") {
                    npc<Happy>("I'm sure I can. What are you thinking of?")
                    choice {
                        option<Talk>("Could you knit me a sweater?") {
                            npc<Angry>("Do I look like a member of a sewing circle? Be off wi' you. I have fought monsters that would turn your hair blue.")
                            npc<Angry>("I don't need to be laughed at just 'cos I'm getting a bit old.")
                        }
                        wig()
                        option<Quiz>("Could you repair the arrow holes in the back of my shirt?") {
                            npc<Talk>("Ah yes, it's a tough world these days. There's a few brave enough to attack from ten metres away.")
                            statement("Ned pulls out a needle and attacks your shirt.")
                            npc<Happy>("There you go, good as new.")
                            player<Talk>("Thanks Ned. Maybe next time they will attack me face to face.")
                        }
                        option<Talk>("Actually, I don't need anything.")
                    }
                }
            }
        }
    }

    fun ChoiceBuilder2.rope() {
        option<Talk>("Yes, I would like some rope.") {
            npc<Happy>("Well, I can sell you some rope for 15 coins. Or I can be making you some if you gets me four balls of wool. I strands them together I does, makes em strong.")
            player<Quiz>("You make rope from wool?")
            npc<Shifty>("Of course you can!")
            player<Quiz>("I thought you needed hemp or jute.")
            npc<Angry>("Do you want some rope or not?")
            choice {
                option<Talk>("Okay, please sell me some rope.") {
                    buyRope()
                }
                option<Talk>("That's a little more than I want to pay.") {
                    npc<RollEyes>("Well, if you ever need rope that's the price. Sorry. An old sailor needs money for a little drop o' rum.")
                }
                if (inventory.contains("ball_of_wool", 4)) {
                    option<Talk>("I have some balls of wool. Could you make me some rope?") {
                        npc<Happy>("Sure I can.")
                        statement("You hand over four balls of wool. Ned gives you a coil of rope.")
                        inventory.transaction {
                            remove("ball_of_wool", 4)
                            add("rope")
                        }
                    }
                } else {
                    option<Talk>("I will go and get some wool.") {
                        npc<Talk>("Aye, you do that. Remember, it takes four balls of wool to make strong rope.")
                    }
                }
            }
        }
    }

    suspend fun Player.buyRope() {
        if (inventory.contains("coins", 15)) {
            npc<Happy>("There you go, finest rope in Gielinor.")
            statement("You hand Ned 15 coins. Ned gives you a coil of rope.")
            inventory.remove("coins", 15)
            if (inventory.isFull()) {
                floorItems.add(tile, "rope", disappearTicks = 300, owner = this)
            } else {
                inventory.add("rope")
            }
        } else {
            statement("You don't have enough coins to buy any rope!")
        }
    }
}
