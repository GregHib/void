package content.area.asgarnia.port_sarim

import content.entity.npc.shop.openShop
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Betty : Script {
    init {
        npcOperate("Talk-to", "betty_port_sarim") { (target) ->
            val progress = questStage("hand_in_the_sand")
            when {
                progress >= COMPLETED -> pinkDyeOffer()
                progress >= MAKE_SERUM -> choice {
                    option("Talk to Betty about the Hand in the Sand.") {
                        handInTheSandChat(target)
                    }
                    option("Talk to Betty about her shop.") {
                        shopMenu(target)
                    }
                }
                else -> {
                    npc<Happy>("Hello there. Welcome to my magic emporium.")
                    shopMenu(target)
                }
            }
        }
    }

    private suspend fun Player.shopMenu(target: NPC) {
        choice {
            option<Quiz>("Can I see your wares?") {
                npc<Happy>("Of course.")
                openShop(target.def["shop"])
            }
            option<Neutral>("Sorry, I'm not into magic.") {
                npc<Happy>("Well, if you see anyone who is, please send them my way.")
            }
        }
    }

    private suspend fun Player.handInTheSandChat(target: NPC) {
        when (quest("hand_in_the_sand")) {
            "make_serum" -> serumChat(target)
            "distract_sandy", "drug_coffee" -> {
                npc<Happy>("Hello deary! How did the serum work?")
                if (ownsItem("truth_serum")) {
                    haventTriedIt()
                } else {
                    lostSerumMenu()
                }
            }
            else -> {
                npc<Happy>("Hello again deary. Come back and tell me what happened when all the fuss is over.")
                player<Neutral>("Ok Betty, I'll be back!")
            }
        }
    }

    private suspend fun Player.serumChat(target: NPC) {
        when (get("handsand_serum", 0)) {
            SERUM_UNSTARTED -> askAboutSerum()
            SERUM_LENS -> focusTheLens(target)
            SERUM_MADE -> lastIngredient()
            else -> askedAboutLens()
        }
    }

    private suspend fun Player.askAboutSerum() {
        player<Quiz>("I've come from Yanille, the wizard says you can make Truth Serum?")
        npc<Happy>("This is true deary, I'll need an empty vial.")
        if (!inventory.contains("vial")) {
            player<Sad>("I'll have to go find one then, I'll be back!")
            return
        }
        player<Happy>("I have one here!")
        if (!inventory.remove("vial")) {
            return
        }
        set("handsand_serum", SERUM_BOTTLE)
        inventory.add("bottled_water")
        npc<Amazed>(
            "That's good, now you'll need to make a rose tinted lens. Pink dye can be made " +
                "from red berries in this bottle to make redberry juice, then add white berries. " +
                "Just use that on a bullseye lens.",
        )
    }

    private suspend fun Player.askedAboutLens() {
        npc<Happy>("Hello deary! Have you managed to make that lens yet?")
        val hasSupplies = ownsItem("bottled_water") || ownsItem("redberry_juice") || ownsItem("pink_dye") || ownsItem("rose_tinted_lens")
        choice {
            option<Sad>("I'm still working on it.")
            option<Sad>("I'm afraid I've forgotten how!") {
                npc<Happy>(
                    "Pink dye can be made from red berries in the bottle I gave you. Add white " +
                        "berries to make the pink dye and then you just need to use that on a " +
                        "bullseye lens. Good luck!",
                )
            }
            if (!hasSupplies) {
                option<Shock>("I've lost my bottle!") {
                    replaceBottle()
                }
            }
        }
    }

    private suspend fun Player.replaceBottle() {
        npc<Happy>("Oh don't worry about that deary, I have plenty and you can start the whole thing again.")
        if (inventory.spaces < 1) {
            npc<Happy>(
                "I'd let you have another but you don't seem to have a spare hand to carry it, " +
                    "come back when you have room.",
            )
            return
        }
        inventory.add("bottled_water")
        npc<Happy>("Here, have another.")
    }

    private suspend fun Player.focusTheLens(target: NPC) {
        if (!inventory.contains("rose_tinted_lens")) {
            statement("Perhaps you should have the rose tinted lens with you before speaking to Betty.")
            return
        }
        npc<Happy>(
            "Wonderful deary. When you're ready, just stand in the open doorway and focus the " +
                "light on the empty vial on my desk and I'll pour the serum into it.",
        )
        player<Happy>("Ok, what does that do?")
        npc<Happy>(
            "Why it makes the person who drinks it unable to hide in the shadow of lies. The " +
                "light of truth will shine!",
        )
        if (get("handsand_counter_multi", false)) {
            return
        }

        delay(3)
        sound("put_down")
        set("handsand_counter_multi", true)
        target.anim("take")
        item(item = "vial", text = "Betty places a vial on her counter.")
    }

    private suspend fun Player.lastIngredient() {
        if (!inventory.contains("truth_serum")) {
            if (ownsItem("truth_serum")) {
                statement("You'll need to get your Truth Serum out of the bank first!")
                return
            }
            player<Sad>("I've lost it!")
            replaceSerum()
            return
        }
        npc<Happy>(
            "Ok, now the last ingredient, something personal from the person you need to tell " +
                "the truth, else it won't work!",
        )
        if (!inventory.contains("sand")) {
            player<Sad>("Ok, I'll see if I can find something.")
            return
        }
        player<Quiz>("What about this sand straight from his pocket?")
        npc<Happy>("That's excellent deary!")
        if (!inventory.remove("sand")) {
            return
        }
        item(
            item = "sand",
            text = "You hand the sand over and watch Betty sprinkle it in the serum, it fizzes.",
        )
        npc<Happy>("Don't forget to dilute it in something like tea or coffee.")
        set("hand_in_the_sand", "distract_sandy")
        set("handsand_serum", SERUM_FINISHED)
    }

    private suspend fun Player.haventTriedIt() {
        player<Idle>("I haven't tried it yet.")
        npc<Happy>("Well don't forget to dilute it in a drink or something else bad things might happen.")
        badThings()
    }

    private suspend fun Player.lostSerumMenu() {
        choice {
            option<Quiz>("I've forgotten how to use the serum.") {
                npc<Happy>("It must be diluted in a drink or something else bad things might happen.")
                badThings()
            }
            option<Sad>("I've lost it!") {
                replaceSerum()
            }
        }
    }

    private suspend fun Player.badThings() {
        player<Quiz>("Bad things?")
        npc<Idle>("Well.... bits might drop off.")
        player<Shock>("Oh! I... see. I'll remember to dilute it then.")
    }

    private suspend fun Player.replaceSerum() {
        if (inventory.spaces < 1) {
            npc<Happy>(
                "That's not a problem, I kept some of it here just in case, but you have no " +
                    "space for it! Come back when you do",
            )
            return
        }
        npc<Happy>("That's not a problem, I kept some of it here just in case, here you are!")
        inventory.add("truth_serum")
        item(item = "truth_serum", text = "Betty hands you a new vial of truth serum.")
    }

    private suspend fun Player.pinkDyeOffer() {
        npc<Happy>(
            "I heard from Zavistic what a good job you did. If you want some more pink dye, I " +
                "have made up a batch and you can have some for 20 gold.",
        )
        choice {
            option<Neutral>("No thanks Betty. Good luck with the shop, I might be back for some dye later.")
            option<Happy>("Yes please!") {
                buyPinkDye()
            }
        }
    }

    private suspend fun Player.buyPinkDye() {
        if (inventory.spaces < 1) {
            npc<Happy>("I'm afraid you don't have space in your pack for anything more, come back when you do.")
            return
        }
        if (!inventory.contains("coins", PINK_DYE_PRICE)) {
            statement("You don't have enough money at the moment, come back later.")
            return
        }
        if (!inventory.remove("coins", PINK_DYE_PRICE)) {
            return
        }
        addOrDrop("pink_dye")
        statement("You hand over 20 gold pieces in return for the dye")
    }

    private companion object {
        const val MAKE_SERUM = 70
        const val COMPLETED = 160

        const val SERUM_UNSTARTED = 0
        const val SERUM_BOTTLE = 1
        const val SERUM_LENS = 4
        const val SERUM_MADE = 5
        const val SERUM_FINISHED = 6

        const val PINK_DYE_PRICE = 20
    }
}
