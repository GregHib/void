package content.area.misthalin.zanaris.puro_puro

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Elnock : Script {
    init {
        npcOperate("Talk-to", "elnock_inquisitor") {
            // TODO intro
            npc<Neutral>("Ah, good day, it's you again. Would you like to trade, or get an impling collector's scroll? Or can I help you some other way?")
            menu()
        }
    }

    private suspend fun Player.menu() {
        choice("What would you like to say?") {
            catchImplings()
            tradeJars()
            option<Quiz>("Could you store some equipment for me?") {
                npc<Neutral>("I suppose I could keep hold of a net and a few empty jars for you. Just give me what you want me to hold.")
            }
            spareEquipment()
            option("More...") {
                more()
            }
        }
    }

    private fun ChoiceOption.tradeJars() {
        option<Quiz>("Can I trade some jarred implings please?") {
            // TODO interface
        }
    }

    private fun ChoiceOption.spareEquipment() {
        option<Quiz>("Do you have some spare equipment I can use?") {
            npc<Happy>("I have already given you some equipment.")
            npc<Neutral>("If you are ready to start hunting implings, then enter the main part of the maze.")
            npc<Neutral>("Just push through the wheat that surrounds the centre of the maze and get catching!")
            choice("What would you like to say?") {
                wheat()
                thanks()
            }
        }
    }

    private suspend fun Player.more() {
        choice("What would you like to say?") {
            option("Can I have an impling collector's scroll, please?") {
                if (carriesItem("impling_scroll")) {
                    npc<Confused>("You've already got one with you - carrying more won't help! You can always get replacements from me if you lose it.")
                    return@option
                }
                if (inventory.add("impling_scroll")) {
                    item("impling_scroll", 400, "Elnock gives you a scroll. If you check it whilst in the maze, you will see how many of each impling you have captured.") // 11273
                } else {
                    // TODO
                }
                menu()
            }
            wheat()
            buyJars()
            thanks()
            option("Back...") {
                menu()
            }
        }
    }

    private fun ChoiceOption.wheat() {
        option<Quiz>("Can you tell me about the wheat?") {
            npc<Neutral>("The wheat here can be hard to push through and pushing through it can make you stronger. I found it easier to push through it for about half an hour just after coming through a temporary crop circle. I call it")
            npc<Neutral>("the Farmer's Affinity.")
            player<Quiz>("I want to become stronger pushing through the wheat.")
            choice("Turn wheat Strength XP ON?") {
                option("Yes") {
                    // TODO
                }
                option("No") {
                    // TODO
                }
            }
        }
    }

    private fun ChoiceOption.catchImplings() {
        option<Neutral>("Can you remind me how to catch implings again?") {
            npc<Neutral>("Certainly.")
            npc<Neutral>("Firstly you will need a butterfly net in which to catch them and at least one special impling jar to store an impling.")
            npc<Neutral>("You will also require some experience as a Hunter since these creatures are elusive. The more immature implings require less experience, but some of the rarer implings are extraordinarily hard to find and catch.")
            npc<Neutral>("Once you have caught one, you may break the jar open and obtain the object the impling is carrying. Alternatively, you may exchange certain combinations of jars with me. I will return the jars to my clients. In")
            npc<Neutral>("exchange I will be able to provide you with some equipment that may help you hunt butterflies more effectively.")
            choice("What would you like to say?") {
                jars()
                thievingImps()
                option("So what's this equipment you can give me then?")
                spareEquipment()
                buyJars()
            }
        }
    }

    private fun ChoiceOption.thievingImps() {
        option("Tell me more about these thieving imps.") {
            npc<Neutral>("Imps and implings appear to be related, and the imps here are quite protective of their smaller relations. If you allow them to get too close then they will attempt to steal jarred implings from your pack, if you have them.")
            npc<Neutral>("They will then set them free, dropping your jar on the floor. So, if you're quick, you may be able to catch it again.")
            npc<Neutral>("I have some impling deterrent which I may trade if you prove that you can catch implings well.")
            choice("What would you like to say?") {
                jars()
                thievingImps()
                equipment()
                spareEquipment()
                buyJars()
            }
        }
    }

    private fun ChoiceOption.jars() {
        option("Tell me more about these jars.") {
            npc<Neutral>("You cannot use an ordinary butterfly jar as a container as the implings will escape from them with ease. However, I have done some investigation and have come up with a solution - if a butterfly jar is coated")
            npc<Neutral>("with a thin layer of a substance noxious to them they become incapable of escape.")
            player<Quiz>("What substance is that, then?")
            npc<Neutral>("I have tried a few experiments with the help of a friend back home, and it turns out that a combination of anchovy oil and flowers - marigolds, rosemary or nasturtiums - will work.")
            player<Quiz>("How do you make anchovy oil then?")
            npc<Neutral>("I'd grind up some cooked anchovies and pass them through a sieve.")
            player<Quiz>("Where do I make these jars?")
            npc<Neutral>("Well, I believe there is a chemist in Rimmington that has a small still that you could use.")
            player<Quiz>("Is there anywhere I can buy these jars?")
            npc<Neutral>("Well I may be able to let you have a few - if it means you will start hunting these implings - although I do not have an infinite supply.")
            npc<Neutral>("Would you like to buy some?")
            choice("Would you like to buy some impling jars?") {
                option("Yes") {
                    trade()
                }
                option("No") {
                    choice("What would you like to say?") {
                        thievingImps()
                        equipment()
                        spareEquipment()
                        buyJars()
                        thanks()
                    }
                }
            }
        }
    }

    private fun ChoiceOption.buyJars() {
        option<Neutral>("Can I buy a few impling jars?") {
            trade()
        }
    }

    private suspend fun Player.trade() {
        npc<Neutral>("I usually prefer trading these jars for implings, but if you desperately need them I'm willing to sell you up to 10 jars per day.")
        npc<Neutral>("I will sell these jars for 2,000 coins each. How many would you like to purchase?")
        val jars = intEntry("How many jars would you like to purchase? (1 - 10)")
        val cost = jars * 2000
        npc<Neutral>("So you would like to purchase $jars ${"jar".plural(jars)}, costing you a total of ${cost.toDigitGroupString()} coins. Is that correct?")
        choice("Purchase $jars impling ${"jar".plural(jars)} for ${cost.toDigitGroupString()} coins?") {
            option("Yes") {
                inventory.transaction {
                    remove("coins", cost)
                    add("impling_jar", jars)
                }
                when (inventory.transaction.error) {
                    is TransactionError.Deficient -> npc<Neutral>("You don't seem to have brought enough coins, please come back with ${cost.toDigitGroupString()} coins if you'd like to buy the jars.")
                    is TransactionError.Full -> TODO()
                    TransactionError.None -> {
                        // TODO track bought per day
                        npc<Neutral>("Here you go. You can come back if you need more and tomorrow I will have another 10 jars ready for purchase.")
                    }
                    else -> {}
                }
            }
            option("No") {
                npc<Neutral>("My time is valuable, please give me a serious offer next time.")
            }
        }
    }

    private fun ChoiceOption.equipment() {
        option<Quiz>("So what's this equipment you can give me, then?") {
            npc<Neutral>("I have been given permission by my clients to give three pieces of equipment to able hunters.")
            npc<Neutral>("Firstly, I have some imp deterrent. If you bring me three baby implings, two young implings and one gourmet impling already jarred, I will give you a vial. Imps don't like the smell, so they will be less likely to")
            npc<Neutral>("steal jarred implings from you.")
            npc<Neutral>("Secondly, I have magical butterfly nets. If you bring me three gourmet implings, two earth implings and one essence impling I will give you a new net. It will help you catch both implings and butterflies.")
            npc<Neutral>("Lastly, I have magical jar generators. If you bring me three essence implings, two eclectic implings and one nature impling I will give you a jar generator. This object will create either butterfly or impling jars (up to")
            npc<Neutral>("a limited number of charges) without having to carry a pack full of them.")
            choice("What would you like to say?") {
                jars()
                thievingImps()
                spareEquipment()
                buyJars()
                thanks()
            }
        }
    }

    private fun ChoiceOption.thanks() {
        option<Happy>("Thanks, I'll get going.")
    }
}
