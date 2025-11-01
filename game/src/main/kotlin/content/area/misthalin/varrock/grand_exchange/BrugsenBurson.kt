package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.ContinueSuspension

class BrugsenBurson : Script {

    init {
        npcOperate("Talk-to", "brugsen_bursen") {
            if (!questCompleted("grand_exchange_tutorial")) {
                player<Quiz>("What is this place?")
                npc<Laugh>("Well, this is the fantastic Grand Exchange!")
                npc<Happy>("I am only too happy to help teach you everything you could possibly want to know. The Tutor nearby can give a brief introduction, too, but he's not as fun as me!")
                choice {
                    option<Talk>("I want to know everything from you!") {
                        npc<Laugh>("Hahaha! Well, let's being, my friend!")
                        tutorial()
                    }
                    option<Talk>("I'd rather speak to the Tutor and get a plain idea.") {
                        npc<RollEyes>("Oh, how boring. I'm so much more entertaining than he is!")
                    }
                    option<Talk>("I'm not interested in either!") {
                        npc<Quiz>("How could this be so?")
                        npc<Talk>("Well...I shall be waiting for you if you change your mind.")
                    }
                }
                return@npcOperate
            }
            npc<Happy>("It's the young entrepreneur! How can I help?")
            choice {
                teachMeAgain()
                systemDetails()
                commonPrices()
                whereDidItComeFrom()
                option<Talk>("Never mind.")
            }
        }

        interfaceOption("Continue", "continue*", "exchange_offers_tutorial") {
            (player.dialogueSuspension as? ContinueSuspension)?.resume(Unit)
        }

        interfaceOption("Continue", "continue*", "exchange_buy_tutorial") {
            (player.dialogueSuspension as? ContinueSuspension)?.resume(Unit)
        }

        interfaceOption("Continue", "continue*", "exchange_confirm_tutorial") {
            (player.dialogueSuspension as? ContinueSuspension)?.resume(Unit)
        }

        interfaceOption("Continue", "continue*", "exchange_wait_tutorial") {
            (player.dialogueSuspension as? ContinueSuspension)?.resume(Unit)
        }
    }

    // https://www.youtube.com/watch?v=2gpKlHgdQ30

    suspend fun Player.tutorial() {
        // TODO camera
        statement("~ The Grand Exchange ~")
        // TODO camera
        npc<Happy>("Welcome, my friend, to the Grand Exchange! From here you can simply tell us what you want to buy or sell and for how much, and we'll pair you up with another player and make the trade!")
        npc<Happy>("Let me start by telling you how to buy and sell items. They are both quite similar, and can be explained in five simple steps.")
        npc<Talk>("<maroon>Step 1</maroon>: You decide what to buy or sell and come here with the items to sell or the money to buy with.")
        npc<Talk>("<maroon>Step 2</maroon>: Speak with one of the clerks, behind the desk in the middle of the building and you'll place and offer as follows...")
        open("exchange_offers_tutorial")
        interfaces.sendText("exchange_offers_tutorial", "summary", "First you will see a selection of boxes, each of which represent a possible offer you can place.")
        interfaces.sendVisibility("exchange_offers_tutorial", "summary_layer", true)
        interfaces.sendVisibility("exchange_offers_tutorial", "box_highlight", true)
        ContinueSuspension.get(this)
        interfaces.sendVisibility("exchange_offers_tutorial", "offer_highlight", true)
        interfaces.sendText("exchange_offers_tutorial", "summary", "Upon clicking on one of the boxes, you will see two buttons appear - one to make a buy offer and one to make a sell offer.")
        ContinueSuspension.get(this)
        open("exchange_buy_tutorial")
        interfaces.sendText("exchange_buy_tutorial", "summary", "If you selected the buy option you would then see this screen. Here you define what to buy by clicking on the box with the magnifying glass and choosing an item.")
        ContinueSuspension.get(this)
        open("exchange_confirm_tutorial")
        interfaces.sendText("exchange_confirm_tutorial", "summary", "In this example we have selected a staff of air. You can then define the quantity, and the cost before selecting the `Confirm Offer` button.")
        ContinueSuspension.get(this)
        open("exchange_offers_tutorial")
        interfaces.sendText("exchange_offers_tutorial", "summary", "Now the offer is placed! You can click on this anytime you want to see the details of your offer. The progress is shown with a progress bar underneath.")
        ContinueSuspension.get(this)
        closeMenu()
        npc<Talk>("Selling items is very much a similar process, just that you are picking an item you already have.")
        npc<Talk>("<maroon>Step 3</maroon>: The clerks will have taken the items or money off you and will look for someone to complete the trade.")
        npc<Talk>("<maroon>Step 4</maroon>: You then need to wait perhaps a matter of moments or maybe days until someone is looking for what you have offered.")
        open("exchange_wait_tutorial")
        ContinueSuspension.get(this)
        closeMenu()
        npc<Talk>("<maroon>Step 5</maroon>: When the trade is complete, we will let you know with a message and you can pick up your winnings by talking to the clerks or by visiting any banker in ${Settings["server.name"]}.")
        npc<Talk>("To see costs of commonly traded items, you can talk to one of the characters around the outside of the building.")
        npc<Talk>("Taking note of past successes and failures is important, so the clerks will show you your previous buy and sell attempts on the Grand Exchange.")
        set("grand_exchange_tutorial", "completed")
        npc<Happy>("There's a lot to learn, but you're not free to use the Grand Exchange. If you speak with me further I'm more than happy to repeat this tutorial and give more information.")
        npc<Happy>("This extra information will be crucial if you wish to make the best deals!")
        choice {
            teachMeAgain()
            systemDetails()
            commonPrices()
            whereDidItComeFrom()
        }
    }

    fun ChoiceBuilder2.whereDidItComeFrom() {
        option<Quiz>("Where did the Grand Exchange come from?") {
            npc<Happy>("I'm glad you ask! I like telling this story. Are you sitting comfortably?")
            player<Talk>("Erm, I'll stand if that's okay.")
            npc<Talk>("Fine. *Ahem* I shall tell you a story of hard work, dedication and success. I grew up working here in Varrock at my parents' general store. I got a good feel for how the prices of items would rise and fall")
            npc<Talk>("depending on the supply and demand; I always found it interesting how items would go from one person to another, in this long chain of transactions.")
            player<Talk>("So you've always lived here?")
            npc<Talk>("Oh, yes. I'd never consider leaving. Anyway, as I became an adult, I got to know other shop owners around Varrock along with the rich traders that would exchange vast quantities of items in one go.")
            player<Quiz>("How do you class a big quantity?")
            npc<Talk>("Well, the quantities are always increasing. I remember the day when you could buy some cooked shark for five coins!")
            player<Talk>("Impossible!")
            npc<Talk>("Nope. Straight up. Anyway. I organised a group of us to meet each week to see what deals could be made. It seemed to work so well and we developed a system that became so popular, that each meeting would see a")
            npc<Talk>("variety of new people joining. I kept improving on the system that I had created, but soon it became too big a thing to manage.")
            player<Talk>("I can imagine!")
            npc<Talk>("So, in the end, i decided: why not make this a ${Settings["server.name"]}-wide phenomena? Make it public and allow anyone to join in. Up to this point, it catered for people buying and selling large quantities, but I knew it would work on a smaller scale.")
            npc<Talk>("And I was also in for a bit of luck. You see, one of the initial patrons had deep connections to the banks of ${Settings["server.name"]}. Together, I think you'll agree we have a most friendly system.")
            player<Happy>("I feel quite excited now! I have a strange urge to shout, 'Buy, buy, sell, sell!'.")
        }
    }

    fun ChoiceBuilder2.commonPrices() {
        option<Talk>("Can you tell me prices for common items, like...") {
            //            https://youtu.be/K1vo3SY7Z_g?si=Hgole9yhfo2ORjwK&t=98
            choice {
                option<Talk>("The prices of ores.") {
                    npc<Talk>("By all means, but you can probably get at this information quicker by visiting Farid M.")
                    set("common_item_costs", "ores")
                    open("common_item_costs")
                }
                option<Talk>("The prices of runes.") {
                    npc<Talk>("My pleasure, but you can probably get at this information quicker by visiting Murky Matt.")
                    set("common_item_costs", "runes")
                    open("common_item_costs")
                }
                option<Talk>("The prices of logs.") {
                    npc<Talk>("Sure thing, but you can probably get at this information quicker by visiting Relobo.")
                    set("common_item_costs", "logs")
                    open("common_item_costs")
                }
                option<Talk>("The prices of herbs.") {
                    npc<Talk>("Of course, but you can probably get at this information quicker by visiting Bob Barter.")
                    set("common_item_costs", "herbs")
                    open("common_item_costs")
                }
                option<Talk>("The prices of weapons and armour.") {
                    npc<Talk>("That's easy, but you can probably get at this information quicker by visiting Hofuthand.")
                    set("common_item_costs", "combat")
                    open("common_item_costs")
                }
            }
        }
    }

    fun ChoiceBuilder2.systemDetails() {
        option<Talk>("Can you tell me more about how the system works?") {
            npc<Happy>("Oh, I simply love passing on knowledge. Okay, let me hit you with some facts...")
            npc<Talk>("The Grand Exchange calculates a guide price for each item that can be traded through it, based on the price people paid for that item over the previous days.")
            npc<Talk>("An item's guide price is just a suggested value; you can offer any price you like when setting up your bids.")
            npc<Talk>("You'll find various experts dotted around the building. They can tell you the guide prices of common items.")
            npc<Talk>("Once you've set up an offer, our clerks will keep looking for players willing to complete the trade, even while you're not logged in.")
            npc<Talk>("Item sets! Say, for example, you wish to sell a set of rune armour but you don't want to sell the items individually. If you speak with one of the clerks, they will do a swap with you: a single item representing the armour set in")
            npc<Talk>("exchange for all the component parts. You can then sell this single item on the Grand Exchange! The clerks will also exchange the sets back into their component parts.")
            npc<Talk>("Did you know: you can abort your offers at any time by visiting one of the clerks, choosing to exchange in order to view your current bits, right-clicking on the offer and choosing 'abort'.")
            npc<Talk>("When you're selling lots of items, your offer may not be completed in a single transaction. For example, if you're selling 1,000 logs, and someone wants to buy 200, we'll do that trade and let you collect the money. The remaining 800 logs will stay in the Grand Exchange, waiting for someone else to buy them.")
            player<Uncertain>("That's a lot of info. I think my brain just melted.")
            npc<Laugh>("Hahaha. Not to worry: you can ask me as many times as you want.")
            npc<Happy>("Trust me, you'll get the hang of it eventually.")
            choice {
                teachMeAgain()
                systemDetails()
                commonPrices()
                whereDidItComeFrom()
                option<Talk>("Never mind.")
            }
        }
    }

    fun ChoiceBuilder2.teachMeAgain() {
        option<Quiz>("Can you teach me about the Grand Exchange again?") {
            npc<Laugh>("Hahaha. It would be my absolute pleasure!")
            tutorial()
        }
    }
}
