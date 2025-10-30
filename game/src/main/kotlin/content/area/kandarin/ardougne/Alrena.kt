package content.area.kandarin.ardougne

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class Alrena : Script {

    init {
        npcOperate("Talk-to", "alrena") {
            when (player.quest("plague_city")) {
                "unstarted" -> {
                    player<Neutral>("Hello Madam.")
                    npc<Neutral>("Oh, hello there.")
                    player<Quiz>("Are you ok?")
                    npc<Sad>("Not too bad... I've just got some troubles on my mind...")
                }
                "started" -> started()
                "has_mask" -> hasMask()
                "about_digging" -> aboutDigging()
                "one_bucket_of_water", "two_bucket_of_water", "three_bucket_of_water" -> bucketOfWater()
                "four_bucket_of_water" -> fourBucketOfWater()
                "sewer" -> sewer()
                "grill_rope", "grill_open", "spoken_to_jethick", "returned_book", "spoken_to_ted", "spoken_to_milli", "need_clearance", "talk_to_bravek", "has_cure_paper", "gave_cure" -> grillOpen()
                else -> freedElena()
            }
        }
    }

    suspend fun NPCOption<Player>.started() {
        player<Neutral>("Hello, Edmond has asked me to help find your daughter.")
        npc<Neutral>("Yes he told me. I've begun making your special gas mask, but I need some dwellberries to finish it.")
        if (player.holdsItem("dwellberries")) {
            player<Happy>("Yes I've got some here.")
            item("dwellberries", 600, "You give the dwellberries to Alrena.")
            target.anim("human_herbing_grind")
            statement("Alrena crushes the berries into a smooth paste. She then smears the paste over a strange mask.")
            target.anim("gasmask_application")
            player.inventory.replace("dwellberries", "gas_mask")
            player["plague_city"] = "has_mask"
            item("gas_mask", 300, "Alrena gives you the mask.")
            player.clearAnim()
            npc<Neutral>("There we go, all done. While in West Ardougne you must wear this at all times, or you could catch the plague.")
            npc<Neutral>("I'll make a spare mask. I'll hide it in the wardrobe in case the mourners come in.")
        } else {
            player<Talk>("I'll try to get some.")
            npc<Talk>("The best place to look is in McGrubor's Wood, just west of Seers' Village.")
        }
    }

    suspend fun NPCOption<Player>.hasMask() {
        player<Happy>("Hello Alrena.")
        npc<Happy>("Hello darling, I think Edmond had a good idea of how to get into West Ardougne, you should hear his idea.")
        player<Happy>("Alright, I'll go and see him now.")
    }

    suspend fun NPCOption<Player>.aboutDigging() {
        // todo check
        player<Happy>("Hello Alrena.")
        npc<Happy>("Hello darling, how's that tunnel coming along?")
        player<Neutral>("I just need to soften the soil a little more and then we'll start digging.")
    }

    suspend fun NPCOption<Player>.bucketOfWater() {
        player<Happy>("Hello Alrena.")
        npc<Happy>("Hello darling, how's that tunnel coming along?")
        player<Neutral>("I just need to soften the soil a little more and then we'll start digging.")
        if (!player.ownsItem("gas_mask")) {
            npc<Talk>("Also, don't forget about that spare gas mask if you need it. It's hidden in the cupboard.")
            player<Talk>("Great, thanks Alrena!")
        }
    }

    suspend fun NPCOption<Player>.fourBucketOfWater() {
        player<Happy>("Hello again Alrena.")
        npc<Neutral>("How's the tunnel going?")
        player<Neutral>("I'm getting there.")
        npc<Shifty>("One of the mourners has been sniffing around asking questions about you and Edmond, you should keep an eye out for him.")
        if (player.ownsItem("gas_mask")) {
            player<Neutral>("Okay, thanks for the warning.")
        } else {
            npc<Neutral>("Also, don't forget about that spare gas mask if you need it. It's hidden in the wardrobe.")
            player<Neutral>("Great, thanks Alrena!")
        }
    }

    suspend fun NPCOption<Player>.sewer() {
        player<Happy>("Hello Alrena.")
        npc<Neutral>("Hi, have you managed to get through to West Ardougne?")
        player<Sad>("Not yet, but I should be going soon.")
        npc<Sad>("Make sure you wear your mask while you're over there! I can't think of a worse way to die.")
        if (player.ownsItem("gas_mask")) {
            player<Neutral>("Okay, thanks for the warning.")
        } else {
            npc<Neutral>("Don't forget, I've got a spare one hidden in the cupboard if you need it.")
            player<Neutral>("Great, thanks Alrena!")
        }
    }

    suspend fun NPCOption<Player>.grillOpen() {
        player<Neutral>("Hello Alrena.")
        npc<Uncertain>("Hello, any word on Elena?")
        player<Sad>("Not yet I'm afraid.")
        npc<Neutral>("Is there anything else I can do to help?")
        if (player.quest("plague_city") == "spoken_to_jethick") {
            player<Quiz>("Do you have a picture of Elena?")
            npc<Neutral>("Yes. There should be one in the house somewhere. Let me know if you need anything else.")
        } else {
            player<Neutral>("It's alright, I'll get her back soon.")
            if (player.ownsItem("gas_mask")) {
                npc<Neutral>("That's the spirit, dear.")
            } else {
                npc<Talk>("That's the spirit, dear. Don't forget that there's a spare gas mask in the cupboard if you need one.")
            }
        }
    }

    suspend fun NPCOption<Player>.freedElena() {
        npc<Happy>("Thank you for rescuing my daughter! Elena has told me of your bravery in entering a house that could have been plague infected. I can't thank you enough!")
    }
}
