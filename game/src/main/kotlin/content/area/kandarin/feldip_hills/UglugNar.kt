package content.area.kandarin.feldip_hills

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.member.ogre.zogre_flesh_eaters
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class UglugNar : Script {
    init {
        npcOperate("Talk-to", "uglug_nar") { (target) ->
            val progress = zogre_flesh_eaters
            when (progress) {
                0 -> firstMeetingMenu()
                in 2..4 -> repeatMeetingMenu()
                else -> repeatMeetingMenu()
            }
        }

        npcOperate("Trade", "uglug_nar") { (target) ->
            if (get("thzfe_sold_balm", false)) {
                openShop("uglugs_stuffsies")
            } else {
                npc<Neutral>(
                    "Me's not got no glug-glugs to sell, yous bring me da sickies glug-glug " +
                        "den me's open da stufsies for ya.",
                )
            }
        }

        registerSale("relicyms_balm_4", price = 1000)
        registerSale("relicyms_balm_3", price = 650)
        registerSale("relicyms_balm_2", price = 300)
        registerSale("relicyms_balm_1", price = 100)
    }

    // ===== Talk-to: First-time meeting (progress 0) =====

    suspend fun Player.firstMeetingMenu() {
        choice {
            whatsGoingOn()
            whatAreYouSelling()
            okayThanks()
        }
    }

    fun ChoiceOption.whatsGoingOn(): Unit = option<Neutral>("Hey, what's going on here?") {
        npc<Neutral>(
            "Dem's dead ogre's come out of da ground...dey's makin' da rest of us into " +
                "sick-uns ...and dead-uns.",
        )
        player<Neutral>("That doesn't sound good!")
        npc<Neutral>("Grish want's da person go down der - see what's what!")
    }

    fun ChoiceOption.whatAreYouSelling(): Unit = option<Neutral>("What are you selling?") {
        if (get("thzfe_sold_balm", false)) {
            npc<Happy>("Me's showin' you da stufsies for yous creatures!")
            openShop("uglugs_stuffsies")
        } else {
            npc<Neutral>(
                "Me's not got no glug-glugs to sell, yous bring me da sickies glug-glug den " +
                    "me's open da stufsies for ya.",
            )
        }
    }

    fun ChoiceOption.okayThanks(): Unit = option<Neutral>("Ok, thanks.")

    // ===== Talk-to: Repeat meeting (progress 2+) =====

    suspend fun Player.repeatMeetingMenu() {
        choice {
            helloAgain()
            whatAreYouSelling()
            okayThanks()
        }
    }

    fun ChoiceOption.helloAgain(): Unit = option<Neutral>("Hello again.") {
        if (get("thzfe_sold_balm", false)) {
            npc<Neutral>(
                "Hey yous creature...yous did good fings gedin that glug-glugs for da " +
                    "sickies! All is ogries pepels are not gettin dead cos of you.",
            )
        } else {
            npc<Neutral>("Hey yous creature...yous still here?")
            player<Neutral>(
                "Yeah, I'm going to help Grish by figuring out what went on here.",
            )
            npc<Neutral>(
                "If yous finds somefin for da sickies, yous brings to me...and I's gives you " +
                    "bright pretties, den me make more for alls pepels.",
            )
            player<Neutral>("Hmm, ok, I'll try to bear that in mind.")
        }
    }

    private fun registerSale(potion: String, price: Int) {
        itemOnNPCOperate(potion, "uglug_nar") {
            if (get("thzfe_sold_balm", false)) {
                npc<Neutral>(
                    "Yous creatures is da funny ones...yous already solds me's ones now..and " +
                        "us can now sell un to yous!",
                )
                return@itemOnNPCOperate
            }
            item(item = potion, text = "You show the potion to Uglug Nar.")
            player<Neutral>(
                "Hey, here you go! I brought you some of the potion which should cure the " +
                    "disease. You said that you would buy some from me.",
            )
            npc<Neutral>(
                "Yous creatures done da good fing...yous get many bright pretties for dis...!",
            )
            set("thzfe_sold_balm", true)
            inventory.remove(potion)
            addOrDrop("coins", price)
            items(potion, "coins", "You sell the potion and get $price coins in return.")
        }
    }
}
