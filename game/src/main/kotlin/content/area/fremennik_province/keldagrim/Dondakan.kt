package content.area.fremennik_province.keldagrim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

/**
 * Dondakan after Between a Rock... shoots players wearing a gold helmet into the Arzinian mine.
 * https://runescape.wiki/w/Transcript:Dondakan_the_Dwarf
 */
class Dondakan : Script {

    init {
        npcOperate("Talk-to", "dondakan_the_dwarf_keldagrim_3") {
            player<Quiz>("How are you doing, Dondakan?")
            npc<Happy>("Oh, just fine, just fine! This gold is certainly creating a steady flow of income for me! After the company takes some of the cut, of cause.")
            choice {
                option<Quiz>("Can you shoot me into the rock again?") {
                    if (!wearingHelmet()) {
                        refuse()
                        return@option
                    }
                    npc<Happy>("Of course, just a second, let me adjust my cannon again.")
                    fireIntoRock()
                }
                option<Quiz>("Are you selling those boots of yours?") {
                    boots()
                }
                option<Neutral>("That's great, but I have to go now.") {
                    npc<Happy>("Come back any time!")
                }
            }
        }

        npcOperate("Fire-into-rock", "dondakan_the_dwarf_keldagrim_3") {
            if (!wearingHelmet()) {
                refuse()
                return@npcOperate
            }
            fireIntoRock()
        }

        itemRemoved("gold_helmet", "worn_equipment", EquipSlot.Hat) {
            if (tile !in Areas["arzinian_mine"]) {
                return@itemRemoved
            }
            queue("arzinian_mine_exit") {
                message("The world turns black and you feel as if you're being sucked out of the rock.")
                leaveMine()
            }
        }

        playerSpawn {
            // Logging out leaves the mine
            if (tile in Areas["arzinian_mine"]) {
                tele(exit)
            }
        }
    }

    private fun Player.wearingHelmet() = equipped(EquipSlot.Hat).id == "gold_helmet"

    private suspend fun Player.refuse() {
        npc<Neutral>("You need to wear that golden helmet... unless you want to crack open your skull on the rock.")
    }

    private suspend fun Player.fireIntoRock() {
        open("fade_out")
        delay(3)
        tele(landing)
        delay(1)
        open("fade_in")
    }

    private suspend fun Player.leaveMine() {
        open("fade_out")
        delay(3)
        tele(exit)
        delay(1)
        open("fade_in")
    }

    private suspend fun Player.boots() {
        npc<Quiz>("What, you mean these granite boots?")
        player<Happy>("Yes, I'd be happy to pay quite some money for them!")
        npc<Laugh>("Haha, I bet you would, I bet you would!")
        npc<Happy>("But I no longer need the money, this rock is generating plenty! Besides, I'm rather fond of them now... best boots I ever had!")
        choice {
            option<Sad>("Oh please...") {
                npc<Neutral>("No, I won't sell them.")
            }
            option<Happy>("Pretty please?") {
                npc<Angry>("No!")
                player<Frustrated>("Oh come on!!!")
                npc<Neutral>("Well...")
                player<Happy>("Yes?")
                npc<Quiz>("How much gold would you be willing to pay for it? Not that I'm selling my boots, mind, I'm just curious.")
                offer(intEntry("Enter amount:"))
            }
            option<Neutral>("Never mind, I don't want them anymore.") {
                npc<Happy>("Great, because I don't want to sell them.")
                player<Neutral>("Okay.")
                npc<Neutral>("Right.")
            }
        }
    }

    private suspend fun Player.offer(amount: Int) {
        if (amount < 1000) {
            npc<Angry>("What, are you trying to scam old Dondakan? You want me to sell these boots for a measly $amount coins?")
            npc<Neutral>("I'm not selling them anyway.")
            return
        }
        if (amount > inventory.count("coins")) {
            npc<Neutral>("Hey, that's quite a bit of money. Which you don't have on you. Oh well, I wasn't selling my boots anyway.")
            return
        }
        if (amount < 75_000) {
            npc<Neutral>("If I were selling my granite boots, which I'm clearly not, I would certainly not accept a mere $amount coins for them.")
            return
        }
        npc<Laugh>("Haha, nice try $name!")
        npc<Happy>("Pity I'm not selling my boots, not for any price or any item. I just want to see how high you would go!")
        player<Frustrated>("Bah, thanks a lot.")
    }

    companion object {
        // Unverified, an open tile on the west side of the mine https://oldschool.runescape.wiki/w/Arzinian_Mine
        private val landing = Tile(2355, 4960)

        // Unverified, beside Dondakan's cannon
        private val exit = Tile(2823, 10164)
    }
}
