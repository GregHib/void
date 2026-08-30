package content.area.tirannwn.isafdar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Islwyn : Script {

    init {
        npcOperate("Talk-to", "islwyn") {
            if (!questCompleted("roving_elves")) {
                npc<Happy>("Hello there, it's a lovely day for a walk in the woods. So what can I help you with?")
                player<Neutral>("I'm just looking around.")
                return@npcOperate
            }
            npc<Happy>("Welcome back to the land of the elves, friend! Do you need your seeds charged into equipment?")
            choice {
                option<Neutral>("I need to buy a new piece of equipment.") {
                    npc<Neutral>("Ah, very well. I will sell you a new bow or shield for 900,000 coins.")
                    choice {
                        option<Neutral>("I'd like to buy a new bow.") {
                            buy("new_crystal_bow", "crystal bow")
                        }
                        option<Neutral>("I'd like to buy a new shield.") {
                            buy("new_crystal_shield", "crystal shield")
                        }
                        option<Neutral>("Oh well... never mind then.")
                    }
                }
                option<Neutral>("I need to recharge my seeds into equipment.") {
                    recharge()
                }
            }
        }

        itemOnNPCOperate("crystal_seed", "islwyn") {
            if (!questCompleted("roving_elves")) {
                npc<Neutral>("That's a curious crystal you have there, friend.")
                return@itemOnNPCOperate
            }
            recharge()
        }
    }

    suspend fun Player.buy(item: String, name: String) {
        if (!inventory.remove("coins", NEW_PRICE)) {
            player<Sad>("Sorry but I don't have that much.")
            npc<Neutral>("Well sorry, but I can't let it go for anything less.")
            return
        }
        inventory.add(item)
        statement("You hand over ${String.format("%,d", NEW_PRICE)} coins and get a $name in return.")
        npc<Happy>("Good hunting.")
        player<Happy>("Thanks... goodbye.")
    }

    suspend fun Player.recharge() {
        if (!inventory.contains("crystal_seed")) {
            statement("You don't have any seeds to recharge.")
            return
        }
        val price = (900_000 - 180_000 * get("crystal_seed_attunements", 0)).coerceAtLeast(180_000)
        npc<Neutral>("Certainly, that will cost you ${String.format("%,d", price)} coins.")
        choice {
            option<Neutral>("Recharge my seed into a bow, please.") {
                attune("crystal_bow_full", price)
            }
            option<Neutral>("Recharge my seed into a shield, please.") {
                attune("crystal_shield_full", price)
            }
            option<Neutral>("That's too much for me.")
        }
    }

    suspend fun Player.attune(item: String, price: Int) {
        if (!inventory.contains("coins", price)) {
            statement("You don't have enough coins, you need ${String.format("%,d", price)}.")
            return
        }
        if (inventory.remove("coins", price) && inventory.remove("crystal_seed")) {
            inventory.add(item)
            inc("crystal_seed_attunements")
            npc<Happy>("Good hunting.")
        }
    }

    companion object {
        private const val NEW_PRICE = 900_000
    }
}
