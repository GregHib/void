package content.area.asgarnia.falador

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class WysonTheGardener : Script {

    init {
        npcOperate("Talk-to", "wyson_the_gardener") {
            npc<Idle>("I'm the head gardener around here. If you're looking for woad leaves, or if you need help with owt, I'm yer man.")
            choice {
                option<Idle>("Yes please, I need woad leaves.") {
                    npc<Shifty>("How much are you willing to pay?")
                    choice {
                        option<Idle>("How about five coins?") {
                            npc<Angry>("No no, that's far too little. Woad leaves are hard to get. I used to have plenty but someone kept stealing them off me!")
                            choice {
                                option<Idle>("How about ten coins?") {
                                    howAboutTen()
                                }
                                option<Idle>("How about 15 coins?") {
                                    buyWoadLeaf()
                                }
                                option<Idle>("How about 20 coins?") {
                                    buyWoadLeaves()
                                }
                                option<Idle>("Actually, I've changed my mind.")
                            }
                        }
                        option<Idle>("How about ten coins?") {
                            howAboutTen()
                        }
                        option<Idle>("How about 15 coins?") {
                            buyWoadLeaf()
                        }
                        option<Idle>("How about 20 coins?") {
                            buyWoadLeaves()
                        }
                        option<Idle>("Actually, I've changed my mind.")
                    }
                }
                option<Idle>("Sorry, but I'm not interested.") {
                    npc<Disheartened>("Fair enough.")
                }
            }
        }
    }

    // TODO add selling mole parts

    suspend fun Player.howAboutTen() {
        npc<Angry>("No no, that's far too little. Woad leaves are hard to get. I used to have plenty but someone kept stealing them off me!")
        choice {
            option<Idle>("How about 15 coins?") {
                buyWoadLeaf()
            }
            option<Idle>("How about 20 coins?") {
                buyWoadLeaves()
            }
            option<Idle>("Actually, I've changed my mind.")
        }
    }

    suspend fun Player.buyWoadLeaf() {
        npc<Idle>("Mmmm... okay, that sounds fair.")
        if (inventory.remove("coins", 15)) {
            if (!inventory.add("woad_leaf")) {
                FloorItems.add(tile, "woad_leaf", disappearTicks = 300, owner = this)
            }
            item("woad_leaf", 290, "You buy a woad leaf from Wyson.")
            player<Happy>("Thanks.")
            npc<Idle>("I'll be around if you have any more gardening needs.")
        } else {
            player<Disheartened>("I don't have enough coins to buy the leaves. I'll come back later.")
        }
    }

    suspend fun Player.buyWoadLeaves() {
        npc<Happy>("Okay, that's more than fair.")
        if (inventory.remove("coins", 20)) {
            if (!inventory.add("woad_leaf", 2)) {
                FloorItems.add(tile, "woad_leaf", 2, disappearTicks = 300, owner = this)
            }
            item("woad_leaf", 290, "You buy a pair of woad leaves from Wyson.")
            player<Happy>("Thanks.")
        } else {
            player<Disheartened>("I don't have enough coins to buy the leaves. I'll come back later.")
        }
    }
}
