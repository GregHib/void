package content.minigame.fist_of_guthix

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class Reggie : Script {

    init {
        npcOperate("Talk-to", "reggie") {
            npc<Happy>("Welcome, adventurer. Have you come to have a look at my fine wares?")
            menu()
        }

        npcOperate("Trade", "reggie") {
            open("fist_of_guthix_rewards")
        }
    }

    private suspend fun Player.menu() {
        choice("Select an Option") {
            option("Yes, I want to see what you're selling.") {
                open("fist_of_guthix_rewards")
            }
            option("No, I have some questions.") {
                questions()
            }
            option<Neutral>("No, I'm just passing by.") {
            }
        }
    }

    private suspend fun Player.questions() {
        choice("Select an Option") {
            option("What currency are you trading in?") {
                player<Quiz>("What currency are you trading in? It doesn't seem to be regular coins.")
                npc<Neutral>("You are absolutely right, my friend. I exchange my goods for so called Fist of Guthix tokens.")
                tokenQuestions()
            }
            option("Can you recharge my items?") {
                rechargeItems()
            }
            option("Can you uncharge my items?") {
                unchargeItems()
            }
            option("Why do all of your wares degrade?") {
                degradingItems()
            }
            option("I have to leave now.") {
                player<Neutral>("Sorry, I have to leave now. Goodbye.")
            }
        }
    }

    private suspend fun Player.tokenQuestions() {
        choice("Select an Option") {
            option<Quiz>("How can I get hold of these tokens?") {
                gettingTokens()
            }
            option("What are Fist of Guthix tokens?") {
                whatAreTokens()
            }
            option("I have to go now.") {
                player<Neutral>("Sorry, but I have to leave now. Goodbye.")
            }
        }
    }

    private suspend fun Player.gettingTokens() {
        npc<Neutral>("We run a little business here, together with Fiara. You see, this is a holy site of Guthix and there is a great power emanating from that cave over there. We're not quite sure what it is yet, but it has to do with Guthix itself. We try to store as much as we can, so that we're ready to use it once we know what it's for.")
        player<Confused>("And what does that have to do with the tokens?")
        npc<Neutral>("Oh, yes, the tokens. Everyone is frere to enter the contest we're running here, and the winners are rewarded with tokens which you can then trade in for my goods. If you want to know more, you should speak to Fiara.")
        choice("Select an Option") {
            option<Quiz>("I want to see what you're trading first.") {
                open("fist_of_guthix_rewards")
            }
            option("I have more questions that I want answered.") {
                questions()
            }
            option<Neutral>("Alright, I will do that.") {
            }
        }
    }

    private suspend fun Player.whatAreTokens() {
        player<Quiz>("What are Fist of Guthix tokens?")
        npc<Neutral>("Oh, they're nothing special in themselves. They are just tokens we use as currency.")
        choice("Select an Option") {
            option<Quiz>("What are you selling?") {
                open("fist_of_guthix_rewards")
            }
            option<Quiz>("How can I get hold of these tokens?") {
                gettingTokens()
            }
            option<Quiz>("I have another question.") {
                questions()
            }
        }
    }

    private suspend fun Player.rechargeItems() {
        player<Quiz>("Can you recharge my items?")
        npc<Happy>("By items, I assume you mean items originally acquired from me. If so, the answer is yes, my friend. I gladly spread the power of Guthix, if I am able to.")
        player<Happy>("That's great.")
        npc<Neutral>("It certainly is. The power of Guthix does not come cheaply, though. So, I'm afraid I will have to charge you some tokens for it. And I can only recharge items that have been completely depleted of their magical energy.")
        player<Neutral>("Sounds fair. So, how do I go about doing this?")
        npc<Neutral>("Just hand me the item you want recharged and I'll see what I can do.")
        choice("Select an Option") {
            option("I want to see what you're selling first.") {
                open("fist_of_guthix_rewards")
            }
            option<Quiz>("How can I get hold of these tokens?") {
                gettingTokens()
            }
            option("I still have more questions.") {
                questions()
            }
            option<Neutral>("Okay, I'll do that.") {
                player<Neutral>("Okay, I'll do that.")
            }
        }
    }

    private suspend fun Player.unchargeItems() {
        player<Quiz>("Can you uncharge my items for me?")
        npc<Confused>("You don't want to keep the power of Guthix? That's strange. But yes, of course I can do this for you. Please be aware that you won't get anything in return, except the degraded item, and it will cost you tokens to have it charged again.")
        player<Neutral>("Yes, I will keep that in mind.")
        npc<Neutral>("Good. In that case, just hand me the item from which you want the charge removed.")
        choice("Select an Option") {
            option("I want to see what you're selling first.") {
                open("fist_of_guthix_rewards")
            }
            option("I still have more questions.") {
                questions()
            }
            option<Neutral>("Okay, I'll do that.") {
            }
        }
    }

    private suspend fun Player.degradingItems() {
        player<Quiz>("Why do all of your wares degrade? I have seen plenty of similar items in my days, but they all seem to last a lot longer than yours.")
        npc<Neutral>("A most accurate observation, my friend. But the other items you've come across in your travels most certainly have not been directly infused with the power emanating from the Fist of Guthix.")
        player<Confused>("Umm... I suppose not. But wouldn't the power of Guthix have made the items stronger?")
        npc<Neutral>("Why, yes, they have. Keeping a shop so close to the source of this power has resulted in the items being infused with energy. This makes them stronger than they normally would have been, but, unfortunately, they are hardly suitable vessels for such divine power. As they are being used, the power will be spent, and all that will remain is an empty, weakened shell. When this happens, just come back to me and I will gladly recharge them for you.")
        player<Neutral>("...For a small fee.")
        npc<Laugh>("Haha. Yes, my friend, for a small fee. The power of a god does not come cheaply, not even in small doses. If we were to give it out for free, we would run out of resources in an instant. Balance must be maintained.")
        player<Neutral>("Okay, I will keep that in mind. Thank you.")
        npc<Neutral>("Now, is there anything else I can help you with?")
        choice("Select an Option") {
            option<Quiz>("What are you selling?") {
                open("fist_of_guthix_rewards")
            }
            option("I have another question.") {
                questions()
            }
            option("I have to go now.") {
                player<Neutral>("Sorry, but I have to leave now. Goodbye.")
            }
        }
    }
}
