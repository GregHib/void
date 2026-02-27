package content.area.misthalin.edgeville.monastery

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class BrotherJared : Script {
    init {
        npcOperate("Talk-to", "brother_jared") { (target) ->
            choice {
                bold()
                option<Happy>("Praise be to Saradomin!") {
                    npc<Happy>("Yes! Praise he who brings life to this world.")
                }
            }
        }
    }

    private val logger = InlineLogger()

    private fun ChoiceOption.bold() {
        option<Quiz>("What can you do to help a bold adventurer like myself?") {
            val hasStar = inventory.contains("unblessed_symbol")
            if (hasStar) {
                npc<Happy>("Well I can bless that star of Saradomin you have, or I could tell you about the Skillcape of Prayer!")
            } else if (hasMax(Skill.Prayer, 99)) {
                skillcape()
                return@option
            } else {
                npc<Happy>("I can tell you about holy symbols or the Skillcape of Prayer.")
            }
            choice {
                if (hasStar) {
                    option("Bless star, please.") {
                        player<Neutral>("Yes please.")
                        inventory.replace("unblessed_symbol", "holy_symbol")
                        item("holy_symbol", 400, "You give Jered the symbol. Jered closes his eyes and places his hand on the symbol. He softly chants. Jered passes you the holy symbol.")
                    }
                } else {
                    option<Neutral>("Tell me about holy symbols.") {
                        npc<Happy>("If you have a silver star, which is the holy symbol of Saradomin, then I can bless it. Then if you are wearing it, it will help you when you are praying.")
                    }
                }
                option<Quiz>("Tell me about the Skillcape of Prayer.") {
                    npc<Neutral>("The Skillcape of Prayer is the hardest of all the skillcapes to get; it requires much devotion to acquire but also imbues the wearer with the ability to briefly fly!")
                    npc<Neutral>("The Cape of Prayer also increases the amount of Prayer points restored from drinking potions when it is equipped. Is there something else I can do for you?")
                    choice {
                        bold()
                        option("No, thank you.") {
                            player<Neutral>("No thank you.")
                        }
                    }
                }
            }
        }
    }

    private suspend fun Player.skillcape() {
        npc<Happy>("Well, seeing as you are so devout in praising the gods, I could sell you a Skillcape of Prayer, which increases the amount of Prayer points restored when drinking potions.")
        choice {
            option<Happy>("Yes, please. So few people have Skillcapes of Prayer!") {
                npc<Neutral>("One as pious as you has certainly earned the right to wear one, but the monastery requires a donation of 99000 coins for the privilege.")
                choice {
                    option("I'm afraid I can't afford that.") {
                        noThanks()
                    }
                    option<Happy>("I am always happy to contribute towards the monastery's upkeep.") {
                        inventory.transaction {
                            val trimmed = Skill.entries.any { it != Skill.Prayer && levels.getMax(it) >= Level.MAX_LEVEL }
                            remove("coins", 99_000)
                            add("prayer_cape${if (trimmed) "_t" else ""}")
                            add("prayer_hood")
                        }
                        when (inventory.transaction.error) {
                            is TransactionError.Deficient -> {
                                player<Sad>("But, unfortunately, I don't have enough money with me.")
                                npc<Idle>("Well, come back and see me when you do.")
                            }
                            is TransactionError.Full -> npc<Quiz>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                            TransactionError.None -> npc<Happy>("Excellent! Wear that cape with pride my friend.")
                            else -> logger.debug { "Error buying prayer skillcape: ${inventory.transaction.error}." }
                        }
                    }
                }
            }
            option("No thanks, I can't afford one of those.") {
                noThanks()
            }
        }
    }

    private suspend fun Player.noThanks() {
        npc<Sad>("No thanks, I can't afford one of those.")
        npc<Neutral>("I am sorry to hear that. If you should find yourself in wealthier times come back and see me.")
    }
}
