package content.area.misthalin.edgeville.monastery

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class BrotherJered : Script {

    private val cost = 1_000_000

    init {
        npcOperate("Talk-to", "brother_jered") { (target) ->
            choice {
                bold()
                if (inventory.contains("holy_elixir") && inventory.contains("spirit_shield")) {
                    option<Quiz>("Can you do anything with this holy elixir?") {
                        offerBlessing()
                    }
                }
                option<Happy>("Praise be to Saradomin!") {
                    npc<Happy>("Yes! Praise he who brings life to this world.")
                }
            }
        }
    }

    private suspend fun Player.offerBlessing() {
        npc<Happy>("An ethereal shield and a holy elixir! Saradomin's blessing would bind the two together into something far stronger.")
        npc<Neutral>("Such a blessing is no small labour, though; the monastery would need a donation of 1,000,000 coins for it.")
        choice {
            option<Happy>("I am always happy to contribute towards the monastery's upkeep.") {
                blessShield()
            }
            option<Sad>("That's a bit expensive!") {
                npc<Neutral>("It is a small price for Saradomin's favour. Come and see me again if you change your mind.")
            }
            option<Neutral>("No, thank you.")
        }
    }

    private suspend fun Player.blessShield() {
        if (!inventory.contains("coins", cost)) {
            player<Sad>("But, unfortunately, I don't have enough money with me.")
            npc<Neutral>("I am sorry to hear that. If you should find yourself in wealthier times come back and see me.")
            return
        }
        val success = inventory.transaction {
            remove("holy_elixir")
            remove("spirit_shield")
            remove("coins", cost)
            add("blessed_spirit_shield")
        }
        if (!success) {
            return
        }
        item("blessed_spirit_shield", "Jered pours the elixir over the shield, closes his eyes and softly chants. Jered passes you the blessed spirit shield.")
        npc<Happy>("Wear it with faith, and may Saradomin watch over you.")
    }

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
                        item("holy_symbol", "You give Jered the symbol. Jered closes his eyes and places his hand on the symbol. He softly chants. Jered passes you the holy symbol.")
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
                        buySkillcape(Skill.Prayer, deficient = "But, unfortunately, I don't have enough money with me.")
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
