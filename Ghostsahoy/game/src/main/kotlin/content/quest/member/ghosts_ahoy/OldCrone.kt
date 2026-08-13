package content.quest.member.ghosts_ahoy

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class OldCrone : Script {

    lateinit var crone: NPC

    init {
        npcOperate("Talk-To", "ahoy_crone") { (target) ->
            // TODO: Animal Magnetism branch (Java OldCroneDialogue cases 2-28) — wire up once
            //       that quest is ported. For now we only handle the Ghost's Ahoy flow.
            crone = target
            when {
                ghosts_ahoy == 0 -> idleSmallTalk()
                ghosts_ahoy > 2 -> ahoyBranch()
                else -> {
                    choice {
                        option<Neutral>("I'm here about Necrovarus.") {
                            ahoyBranch()
                        }
                        option<Neutral>("I'm here about the farmers east of here.") {
                            npc<Neutral>("Come back to me about that after you've made some progress with Velorina.")
                        }
                    }
                }
            }
        }

        itemOnNPCOperate("cup_of_milky_tea_ghosts_ahoy,cup_of_tea_ghosts_ahoy,cup_of_milky_tea,cup_of_tea_nettle_tea,nettle_tea,milky_nettle_tea,translation_manual,mystical_robes,book_of_haricanto", "ahoy_crone") { interaction ->
            when {
                TEA_PRIORITY.contains(interaction.item.id) -> offerTea(interaction.item)
                ENCHANTMENT_PRIORITY.contains(interaction.item.id) -> stageFourOptions(interaction.item)
            }
        }
    }

    private suspend fun Player.idleSmallTalk() {
        player<Neutral>("Hello, old woman.")
        when ((0..2).random()) {
            0 -> npc<Neutral>("When 100 years old you reach, look like a prune you will.")
            1 -> npc<Neutral>("I lived here when this was all just fields, you know.")
            2 -> npc<Neutral>("I'm over 100 years old, you know.")
        }
    }

    private suspend fun Player.ahoyBranch() {
        player<Quiz>("I'm here about Necrovarus.")
        when {
            ghosts_ahoy >= 7 -> postQuestThanks()
            ghosts_ahoy == 6 -> enchantmentFollowUp()
            ghosts_ahoy == 5 -> performEnchantment()
            ghosts_ahoy == 4 -> stageFourOptions(Item(firstEnchantmentItem()))
            ghosts_ahoy == 3 -> wereYouADisciple()
        }
    }

    private suspend fun Player.wereYouADisciple() {
        player<Quiz>("Were you once a disciple of Necrovarus in the Temple of Phasmatys, old woman?")
        npc<Neutral>("I don't remember; I am so old and my memory goes back only so far...")
        player<Quiz>("Is there anything that can help to refresh your memory?")
        if (get("ahoy_subquest_nettletea", 0) < 1) {
            set("ahoy_subquest_nettletea", 1)
        }
        npc<Neutral>("Yes, I would love a nice hot cup of nettle tea.")
        val tea = firstNettleTea()
        if (tea != null) {
            offerTea(Item(tea))
        } else {
            player<Quiz>("Do you know where I can find nettles around here?")
            npc<Neutral>("I believe they grow wild in the Haunted Forest.")
        }
    }

    private suspend fun Player.offerTea(itemUsed: Item) {
        if (ghosts_ahoy >= 4) {
            npc<Neutral>("Oh, no thanks; I'm not thirsty anymore.")
            return
        }

        when (itemUsed.id) {
            "cup_of_milky_tea_ghosts_ahoy" -> {
                set("ahoy_subquest_nettletea", 3)
                ghosts_ahoy = 4
                inventory.remove("cup_of_milky_tea_ghosts_ahoy")
                player<Neutral>("Here's a lovely cup of milky tea for you, in your own special cup.")
            }
            "cup_of_tea_ghosts_ahoy" -> {
                player<Neutral>("Here's a lovely cup of tea for you, in your own special cup.")
                npc<Neutral>("Oh no, it hasn't got milk in it. I only drink tea with milk, I'm afraid.")
                return
            }
            else -> {
                player<Neutral>("Here's some tea for you, like you asked.")
                npc<Neutral>("Yes, but it's not in my special cup! I only ever drink from my special cup!!")
                player<Neutral>("I see. Can I have this special cup, then?")
                if (inventory.contains("porcelain_cup") || ownsItem("porcelain_cup")) {
                    npc<Neutral>("I already gave you a cup - what did you do with it?")
                } else {
                    set("ahoy_subquest_nettletea", 2)
                    addOrDrop("porcelain_cup")
                    item(item = "porcelain_cup", text = "The old crone gives you her special cup.")
                }
                return
            }
        }

        statement("As the old woman drinks the cup of milky tea, enlightenment glows from within her eyes.")
        npc<Neutral>("Ah, that's better. Now, let me see... Yes, I was once a disciple of Necrovarus.")
        player<Neutral>("I have come from Velorina. She needs your help.")
        npc<Neutral>("Velorina? That name sounds familiar...")
        npc<Neutral>(
            "Yes, Velorina was once a very good friend! It has been many years since we spoke " +
                "last. How is she?",
        )
        player<Neutral>("She's a ghost, I'm afraid.")
        npc<Neutral>(
            "They are all dead, then. Even Velorina. I should have done something to stop what " +
                "was happening, instead of running away.",
        )
        player<Neutral>(
            "She and many others want to pass on into the next world, but Necrovarus will not " +
                "let them. Do you know of any way to help them?",
        )
        npc<Neutral>("There might be one way...")
        npc<Neutral>("Do you have such a thing as a ghostspeak amulet?")
        if (equipment.contains("ghostspeak_amulet") || inventory.contains("ghostspeak_amulet")) {
            if (equipment.contains("ghostspeak_amulet")) {
                player<Neutral>("Yes, I'm wearing one right now.")
            } else {
                player<Neutral>("Yes, I'm carrying one in my bag.")
            }
            npc<Neutral>("Well, that's a stroke of luck.")
        } else {
            player<Neutral>("No I don't, I'm afraid.")
            npc<Neutral>("Well, you'll need to find one.")
        }
        explainEnchantmentNeeds()
    }

    private suspend fun Player.explainEnchantmentNeeds() {
        npc<Neutral>(
            "There is an enchantment that I can perform on such an amulet that will give it the " +
                "power of command over ghosts. It will work only once, but it will enable you " +
                "to command Necrovarus to let the ghosts pass on.",
        )
        player<Neutral>("What do you need to perform the enchantment?")
        npc<Neutral>("You have already given me robes of Necrovarus.")
        npc<Neutral>(
            "Necrovarus had a magical robe for which he must have no more use. Only these robes " +
                "hold the power needed to perform the enchantment.",
        )
        npc<Neutral>("You have given me the Book of Haricanto.")
        npc<Neutral>(
            "All his rituals came from a book written by an ancient sorcerer from the East " +
                "called Haricanto. Bring me this strange book.",
        )
        npc<Neutral>(
            "I cannot read the strange letters of the eastern lands. I will need something to " +
                "help me translate the book.",
        )
        sendStageFourMenu()
    }

    private suspend fun Player.stageFourOptions(itemUsed: Item) {
        val hasManual = itemUsed.id == "translation_manual" && !get("ahoy_given_manual", false)
        val hasRobes = itemUsed.id == "mystical_robes" && !get("ahoy_given_robes", false)
        val hasBook = itemUsed.id == "book_of_haricanto" && !get("ahoy_given_book", false)

        if (hasManual || hasRobes || hasBook) {
            when (itemUsed.id) {
                "translation_manual" -> acceptQuestItem("translation_manual", "ahoy_given_manual", "A translation manual - yes, this should do the job.")
                "mystical_robes" -> acceptQuestItem("mystical_robes", "ahoy_given_robes", "Good - the robes of Necrovarus.")
                "book_of_haricanto" -> acceptQuestItem("book_of_haricanto", "ahoy_given_book", "The Book of Haricanto! I have no idea how you came by this, but well done!!")
            }

            if (get("ahoy_given_manual", false) &&
                get("ahoy_given_robes", false) &&
                get("ahoy_given_book", false)
            ) {
                npc<Neutral>("Wonderful; that's everything I need.")
                ghosts_ahoy = 5
                npc<Neutral>("I will now perform the ritual of enchantment.")
                performEnchantment()
            }
            return
        }
        sendStageFourMenu()
    }

    private suspend fun Player.acceptQuestItem(itemId: String, flag: String, response: String) {
        inventory.remove(itemId)
        set(flag, true)
        player<Neutral>("I have something for you.")
        npc<Neutral>(response)
    }

    private suspend fun Player.sendStageFourMenu() {
        val toyStage = get("ahoy_subquest_toyboat", 0)
        choice {
            if (toyStage >= 2) {
                option<Neutral>("Good news! I have found your son!") {
                    foundSon()
                }
            } else if (toyStage == 1) {
                if (inventory.contains("model_ship") || inventory.contains("model_ship_silk")) {
                    option<Sad>("I am afraid I have not found your son yet.") {
                        npc<Neutral>(
                            "I never expected that you would find him - although if you do, " +
                                "please let me know.",
                        )
                    }
                } else {
                    option<Sad>("I am afraid I have lost the boat you gave to me.") {
                        npc<Neutral>("No, I've found it - here you go.")
                        addOrDrop("model_ship")
                        item(item = "model_ship", text = "The old woman gives you a toy boat.")
                    }
                }
            } else {
                option<Quiz>("You are doing so much for me - is there anything I can do for you?") {
                    sonStory()
                }
            }
            option<Quiz>("Remind me - what can I do about Necrovarus?") {
                explainEnchantmentNeeds()
            }
            option<Quiz>("What did you want me to get for you?") {
                npc<Neutral>(
                    "Necrovarus had a magical robe for which he must have no more use. Only " +
                        "these robes hold the power needed to perform the enchantment.",
                )
            }
            option<Neutral>("I'll go and search for those items you need.")
        }
    }

    private suspend fun Player.sonStory() {
        npc<Neutral>(
            "I have lived here on my own for many years, but not always. When I left Port " +
                "Phasmatys, I took my son with me. He grew up to be a fine boy, always in " +
                "love with the sea.",
        )
        npc<Neutral>(
            "He was about twelve years old when he ran away with some pirates to be a cabin " +
                "boy. I never saw him again.",
        )
        player<Sad>("That's the second saddest story I have heard today.")
        npc<Neutral>("If you ever see him, please give him this...and tell him that his mother still loves him.")
        set("ahoy_subquest_toyboat", 1)
        set("ahoy_mast_top", (1..6).random())
        set("ahoy_mast_bottom", (1..6).random())
        set("ahoy_mast_skull", (1..6).random())
        addOrDrop("model_ship")
        item(item = "model_ship", text = "The old woman gives you a toy boat.")
        player<Neutral>("Was this his boat?")
        npc<Neutral>(
            "Yes, he made it himself. It is a model of the very ship in which he sailed away. " +
                "The paint has peeled off and it has lost its flag, but I could never throw " +
                "it away.",
        )
        player<Neutral>("If I find him, I will pass it on.")
    }

    private suspend fun Player.foundSon() {
        npc<Neutral>("Goodness! Where is he?")
        player<Neutral>(
            "He lives on a shipwreck to the east of here. He remembers you and wishes you well.",
        )
        npc<Neutral>("Oh thank you!! I will travel to see him as soon as I am able!!")
    }

    private suspend fun Player.performEnchantment() {
        if (!ownsItem("ghostspeak_amulet")) {
            return npc<Neutral>("You don't have a ghostspeak amulet for me to enchant.")
        }
        crone.anim("human_dancing")
        delay(3)
        if (equipment.contains("ghostspeak_amulet")) {
            equipment.replace(EquipSlot.Weapon.index, "ghostspeak_amulet_enchanted", "ghostspeak_amulet")
        } else if (inventory.contains("ghostspeak_amulet")) {
            inventory.remove("ghostspeak_amulet")
            inventory.add("ghostspeak_amulet_enchanted")
        }
        ghosts_ahoy = 6
        item(
            item = "ghostspeak_amulet_enchanted",
            text = "The ghostspeak amulet emits a green glow from its gem.",
        )
    }

    private suspend fun Player.enchantmentFollowUp() {
        if (ownsItem("ghostspeak_amulet_enchanted")) {
            npc<Neutral>("Did it work?")
            player<Neutral>("Actually, I haven't tried it yet.")
            return
        }
        if (inventory.contains("ghostspeak_amulet") || equipment.contains("ghostspeak_amulet")) {
            player<Neutral>("Could you enchant the amulet for me again?")
            npc<Neutral>("I didn't hear the magic word...")
            player<Neutral>("Please?")
            npc<Neutral>("Sorry, didn't quite catch that...")
            player<Neutral>("Pretty please???")
            npc<Neutral>("Oh, alright; come here, then.")
            npc<Neutral>("I will now perform the ritual of enchantment.")
            performEnchantment()
        } else {
            player<Neutral>("I seem to have lost my ghostspeak amulet.")
            npc<Neutral>("Oh dear, that's unfortunate. You'll have to go and find another one, then.")
        }
    }

    private suspend fun Player.postQuestThanks() {
        player<Neutral>("Good news - the enchantment worked!")
        npc<Neutral>(
            "So Necrovarus has let the ghosts go free.... You have achieved many wonderful " +
                "things, my young friend.",
        )
    }

    private fun Player.firstNettleTea(): String? = TEA_PRIORITY.firstOrNull { inventory.contains(it) }

    private fun Player.firstEnchantmentItem(): String = ENCHANTMENT_PRIORITY.first { inventory.contains(it) }

    companion object {
        private val TEA_PRIORITY = listOf(
            "cup_of_milky_tea_ghosts_ahoy",
            "cup_of_tea_ghosts_ahoy",
            "cup_of_milky_tea",
            "cup_of_tea_nettle_tea",
            "milky_nettle_tea",
            "nettle_tea",
        )

        private val ENCHANTMENT_PRIORITY = listOf(
            "translation_manual",
            "mystical_robes",
            "book_of_haricanto",
        )
    }
}
