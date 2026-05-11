package content.area.wilderness.daemonheim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.Unimpressed
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class Smuggler : Script {
    init {
        npcOperate("Talk-to", "smuggler_dungeoneering") {
            npc<Shifty>("Hail, $name. Need something?")
            menu()
        }

        npcOperate("Trade", "smuggler_dungeoneering") {
            npc<Neutral>("Sorry, but I don't have anything to sell.")
        }

        itemOnNPCOperate("*", "smuggler_dungeoneering") {
            player<Quiz>("Can you tell me what this is used for?")
            npc<Neutral>("That's metal armour. It gives excellent protection against melee and ranged attacks, but weakens your magic powers and renders you vulnerable to magic.")
            npc<Neutral>("That's a melee weapon, so hit enemies with it. It will be particularly effective against opponents with light or no armour.")
            npc<Neutral>("That's mage armour. It offers limited protection against attacks, but enhances your magical abilities.")
            npc<Neutral>("Those are magical runes. They contain the magicl energies needed to cast spells. You can't be proficient in magic without them.")

            npc<Shifty>("Those? Those are nothing. You can give them all to me, if you want.")
            player<Quiz>("If they're nothing, why would you want them?")
            npc<Unimpressed>("Pft. It was worth a try.") // Roll eyes
            npc<Neutral>("Those are rusty coins: lovely money. You can buy things from me with them.")
        }
    }

    private suspend fun Player.menu() {
        choice {
            whatCanYouTellMe()
            whoAreYou()
            doIHaveRewards()
            hereToTrade()
            startingItems()
        }
    }

    private fun ChoiceOption.startingItems() {
        option<Quiz>("Can I change my starting items?") {
            if (get("dungeoneering_exchange_coins", false)) {
                npc<Neutral>("I'm currently exchanging your initial selection of coins for feathers, rune essence and antipoison.")
            } else {
                npc<Neutral>("You're currently starting with a stack of coins. I could set up a pre-order, if you'd like.")
                npc<Neutral>("I'll automatically take your coins, and provide a selection of feathers, rune essence and antipoison in return.")
            }
            choice {
                option("I would like to receive coins.") {
                    set("dungeoneering_exchange_coins", false)
                    npc<Neutral>("Very well. Is there anything else you need?")
                    menu()
                }
                option("I would like to receive feathers, rune essence and antipoison.") {
                    set("dungeoneering_exchange_coins", true)
                    npc<Neutral>("Very well. Is there anything else you need?")
                    menu()
                }
                option("I don't want to change anything.") {
                    npc<Neutral>("Very well. Is there anything else you need?")
                    menu()
                }
            }
        }
    }

    private fun ChoiceOption.hereToTrade() {
        option<Confused>("I'm here to trade.") {
            // Looking down
            npc<Neutral>("Sorry, but I don't have anything to sell.")
        }
    }

    private fun ChoiceOption.doIHaveRewards() {
        option("Do I have any rewards to claim?") {
            npc<Sad>("I have no rewards for you at the moment.")
        }
    }

    private fun ChoiceOption.whoAreYou() {
        option<Quiz>("Who are you?") {
            npc<Shifty>("A friend.")
            player<Quiz>("Okay, what are you doing here, friend?")
            npc<Shifty>("I'm here to help out.")
            player<Quiz>("With what?")
            npc<Shifty>("Well, let's say you find yourself in need of adventuring kit, and you've a heavy pile of rusty coins weighing you down. I can help you with both those problems. Savvy?")
            player<Shock>("Ah, so you're a trader.")
            npc<Angry>("Keep it down, you fool!")
            npc<Shifty>("Yes, I'm a trader. But I'm not supposed to be trading here.")
            npc<Angry>("If you want my goods, you'll learn not to talk about me.")
            player<Disheartened>("Right, got you.") // Head down but not sad
            player<Quiz>("Is there anything else you can do for me?")
            npc<Neutral>("Well, there's the job I'm supposed to be doing down here.")
            player<Quiz>("Which is?")
            npc<Shifty>("Say you chance upon an object that you know little about. Show it to me, and I'll tell you what it's used for.")
            player<Pleased>("That's good to know.")
            npc<Neutral>("I can also offer hints about the behaviour of powerful opponents you might meet in the area. I've spent a long time down here, observing them.")
            player<Neutral>("I'll be sure to come back if I find a particularly strong opponent, then.")
            npc<Neutral>("You'd be wise to, $name.")
            player<Quiz>("How do you know my name?")
            npc<Laugh>("Nothing gets in or out of Daemonheim without me knowing about it.")
            player<Neutral>("Fair enough. Back to my other questions.")
            menu()
        }
    }

    private fun ChoiceOption.whatCanYouTellMe() {
        option<Quiz>("What can you tell me about this place?") {
            npc<Happy>("This floor is less complex than most, and gives you a good introduction to Daemonheim.")
            npc<Happy>("Combat will be the only challenge you'll face, so I've given you a selection of melee, ranged and magical gear.")
            choice {
                option<Happy>("Thanks.") {
                    npc<Shifty>("Well, it's not entirely selfless. For me, it's...a long-term investment.")
                    menu()
                }
                option<Angry>("I don't need your help.") {
                    npc<Happy>("Haha! Well, take it or leave it; it's no skin off my back.")
                    menu()
                }
                option("I was expected more of a challenge.") {
                    player<Confused>("I was expecting more of a challenge, perhaps something to test my other skills.")
                    npc<Pleased>("All in good time, $name. This floor may not be complex, but, as you get deeper, you'll need to use all your skills to survive.")
                    menu()
                }
            }
        }
    }
}
