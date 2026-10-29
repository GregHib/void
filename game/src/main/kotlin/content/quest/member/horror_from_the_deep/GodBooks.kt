package content.quest.member.horror_from_the_deep

import content.entity.npc.shop.openShop
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.random

class GodBooks : Script {

    init {
        for (god in gods) {
            for (page in 1..4) {
                itemOnItem("${god}_page_$page", "damaged_book_$god") { _, _ ->
                    bind(god)
                }
            }
        }

        npcOperate("Talk-to", "jossik") {
            npc<Happy>("Hello again, adventurer. What brings you this way?")
            choice {
                option<Neutral>("Can I see your wares?") {
                    npc<Happy>("Sure thing! I think you'll agree, my prices are remarkable!")
                    openShop("the_lighthouse_store")
                }
                option<Neutral>("Have you found any prayerbooks?") {
                    prayerBooks()
                }
                if (inventory.contains("rusty_casket")) {
                    option<Neutral>("I have a casket I'd like you to look at.") {
                        casket()
                    }
                }
            }
        }

        itemOption("Preach", "saradomins_book_of_wisdom") {
            preach("saradomin")
        }

        itemOption("Preach", "zamoraks_book_of_chaos") {
            preach("zamorak")
        }

        itemOption("Preach", "guthixs_book_of_balance") {
            preach("guthix")
        }

        itemOnItem("unblessed_symbol", "saradomins_book_of_wisdom,guthixs_book_of_balance") { _, _ ->
            bless("unblessed_symbol", "holy_symbol")
        }

        itemOnItem("unpowered_symbol", "zamoraks_book_of_chaos,guthixs_book_of_balance") { _, _ ->
            bless("unpowered_symbol", "unholy_symbol")
        }
    }

    private suspend fun Player.prayerBooks() {
        var returned = false
        for (god in gods) {
            if (get("${god}_book_completed", false) && !ownsItem(book(god)) && inventory.add(book(god))) {
                returned = true
            }
        }
        if (returned) {
            npc<Happy>("As a matter of fact, I did! This book washed up on the beach, and I recognised it as yours!")
            return
        }
        val uncompleted = gods.filter { !get("${it}_book_completed", false) }
        if (uncompleted.isEmpty() || gods.any { ownsItem("damaged_book_$it") }) {
            npc<Sad>("No, sorry adventurer, I haven't.")
            return
        }
        npc<Happy>("Funnily enough I have! I found some books in caskets just the other day! I'll sell one to you for 5000 coins; what do you say?")
        choice {
            for (god in uncompleted) {
                option(god.replaceFirstChar { it.uppercase() }) {
                    // TODO sell damaged_book_$god instead once pages can be obtained from clue scrolls
                    inventory.transaction {
                        remove("coins", 5000)
                        add(book(god))
                    }
                    when (inventory.transaction.error) {
                        TransactionError.None -> {
                            set("${god}_book_completed", true)
                            npc<Happy>("Here you go!")
                        }
                        is TransactionError.Full -> player<Sad>("Sorry, I don't have enough inventory space.")
                        else -> player<Sad>("Sorry, I don't seem to have enough coins.")
                    }
                }
            }
            option("Don't buy anything.")
        }
    }

    private suspend fun Player.casket() {
        npc<Happy>("I can indeed! Here, let me have a closer look...")
        npc<Happy>("Yes! There is something written on it!")
        npc<Happy>("It is very faint however... Can you read it?")
        choice {
            for (god in gods) {
                option<Neutral>(god.replaceFirstChar { it.uppercase() }) {
                    npc<Happy>("I think you're right! Hand it over, and let's see what's inside!")
                    // TODO give damaged_book_$god instead once pages can be obtained from clue scrolls
                    if (inventory.replace("rusty_casket", book(god))) {
                        set("${god}_book_completed", true)
                        val name = when (god) {
                            "saradomin" -> "a Holy Book of Saradomin"
                            "zamorak" -> "an Unholy Book of Zamorak"
                            else -> "a Balance Book of Guthix"
                        }
                        npc<Happy>("Wow! It's $name! I thought these things had all vanished! Well, it's all yours, I hope you appreciate it.")
                    }
                }
            }
        }
    }

    private fun Player.bind(god: String) {
        inventory.transaction {
            for (page in 1..4) {
                remove("${god}_page_$page")
            }
            remove("damaged_book_$god")
            add(book(god))
        }
        if (inventory.transaction.error != TransactionError.None) {
            message("You need all four pages to create this book.")
            return
        }
        set("${god}_book_completed", true)
        message("You bind all four pages into the book.")
    }

    private suspend fun Player.preach(god: String) {
        val passages = chants.getValue(god)
        choice("Select a relevant passage.") {
            option("Wedding Ceremony") {
                chant(god, passages[0])
            }
            option("Last Rites") {
                chant(god, passages[1])
            }
            option("Blessings") {
                chant(god, passages[2])
            }
            option("Preach") {
                chant(god, passages[3 + random.nextInt(passages.size - 3)])
            }
        }
    }

    private fun Player.chant(god: String, text: String) {
        anim("preach_$god")
        say(text)
    }

    private fun Player.bless(symbol: String, blessed: String) {
        if (!has(Skill.Prayer, 50)) {
            message("You need a Prayer level of at least 50 to do this.")
            return
        }
        if (levels.get(Skill.Prayer) < 4) {
            message("You need at least 4 Prayer points to do this.")
            return
        }
        if (!inventory.replace(symbol, blessed)) {
            return
        }
        levels.drain(Skill.Prayer, 4)
        message("You bless the ${symbol.replace('_', ' ')}.")
    }

    private companion object {
        val gods = listOf("saradomin", "zamorak", "guthix")

        fun book(god: String) = when (god) {
            "saradomin" -> "saradomins_book_of_wisdom"
            "zamorak" -> "zamoraks_book_of_chaos"
            else -> "guthixs_book_of_balance"
        }

        val chants = mapOf(
            "saradomin" to listOf(
                "In the name of Saradomin, protector of us all, I now join you in the eyes of Saradomin.",
                "Thy cause was false, thy skills did lack; see you in Lumbridge when you get back.",
                "Go in peace in the name of Saradomin; may his glory shine upon you like the sun.",
                "Protect your self, protect your friends. Mine is the glory that never ends. This is Saradomin's wisdom.",
                "The darkness in life may be avoided, by the light of wisdom shining. This is Saradomin's wisdom.",
                "Show love to your friends, and mercy to your enemies, and know that the wisdom of Saradomin will follow. This is Saradomin's wisdom.",
                "A fight begun, when the cause is just, will prevail over all others. This is Saradomin's wisdom.",
            ),
            "zamorak" to listOf(
                "Two great warriors, joined by hand, to spread destruction across the land. In Zamorak's name, now two are one.",
                "The weak deserve to die, so the strong may flourish. This is the will of Zamorak.",
                "May your bloodthirst never be sated, and may all your battles be glorious. Zamorak bring you strength.",
                "Battles are not lost and won; They simply remove the weak from the equation. Zamorak give me strength!",
                "Those who fight, then run away, shame Zamorak with their cowardice. Zamorak give me strength!",
                "Battle is my calling, and death shall be my rest. Zamorak give me strength!",
                "Strike fast, strike hard, strike true: The strength of Zamorak will be with you. Zamorak give me strength!",
                "There is no opinion that cannot be proven true, by crushing those who choose to disagree with it. Zamorak give me strength!",
            ),
            "guthix" to listOf(
                "Light and dark, day and night, Balance arises from contrast. I unify thee in the name of Guthix.",
                "Thy death was not in vain, for it brought some balance to the world. May Guthix bring you rest.",
                "May you walk the path, and never fall, For Guthix walks beside thee on thy journey. May Guthix bring you peace.",
                "The trees, the earth, the sky, the waters: All play their part upon this land. May Guthix bring you balance.",
            ),
        )
    }
}
