package content.quest.member.biohazard

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ErrandBoys : Script {

    init {
        npcOperate("Talk-to", "da_vinci_rimmington") {
            if (busy(DA_VINCI, "Oh, it's you again. Please don't distract me now, I'm contemplating the sublime.", "Da Vinci does not feel sufficiently moved to talk.")) {
                return@npcOperate
            }
            player<Neutral>("Hello, I hear you're an errand boy for the chemist.")
            npc<Neutral>("Well that's my job yes. But I don't necessarily define my identity in such black and white terms.")
            player<Quiz>("Good for you. Now can you take a vial to Varrock for me?")
            npc<Shifty>("Go on then.")
            offerVials(DA_VINCI, "ethenea", "painted") {
                player<Neutral>("Ok, we're meeting at the Dancing Donkey in Varrock right?")
                npc<Neutral>("That's right.")
            }
        }

        npcOperate("Talk-to", "chancy_rimmington") {
            if (busy(CHANCY, "Look, I've got your vial but I'm not taking two. I always like to play the percentages.", "Chancy doesn't feel like talking.")) {
                return@npcOperate
            }
            player<Neutral>("Hello, I've got a vial for you to take to Varrock.")
            npc<Sad>("Tssch... that chemist asks for a lot for the wages he pays.")
            player<Neutral>("Maybe you should ask him for more money.")
            npc<Neutral>("Nah... I just use my initiative here and there.")
            offerVials(CHANCY, "liquid_honey", "gambled") {
                player<Neutral>("Right. I'll see you later in the Dancing Donkey Inn.")
                npc<Happy>("Be lucky.")
            }
        }

        npcOperate("Talk-to", "hops_rimmington") {
            if (busy(HOPS, "I suppose I'd better get going. I'll meet you at the Dancing Donkey Inn.", "He isn't in a fit state to talk.")) {
                return@npcOperate
            }
            player<Neutral>("Hi, I've got something for you to take to Varrock.")
            npc<Sad>("Sounds like pretty thirsty work.")
            player<Neutral>("Well, there's an Inn in Varrock if you're desperate.")
            npc<Shifty>("Don't worry, I'm a pretty resourceful fellow you know.")
            offerVials(HOPS, "sulphuric_broline", "drank") {
                player<Neutral>("Ok, I'll see you in Varrock.")
                npc<Happy>("Sure, I'm a regular at the Dancing Donkey Inn as it happens.")
            }
        }

        npcOperate("Talk-to", "da_vinci") {
            if (!waiting(DA_VINCI, "Da Vinci does not feel sufficiently moved to talk.")) {
                return@npcOperate
            }
            npc<Happy>("Hello again. I hope your journey was as pleasant as mine.")
            if (get(DA_VINCI, "none") == "delivered") {
                player<Happy>("Well, as they say, it's always sunny in RuneScape.")
                npc<Neutral>("Ok, here it is.")
                handBack(DA_VINCI, "ethenea", "He gives you the vial of ethenea.")
                player<Happy>("Thanks, you've been a big help.")
                return@npcOperate
            }
            player<Happy>("Yep. Anyway, I'll take the package off you now.")
            npc<Confused>("Package? That's a funny way to describe a liquid of such exquisite beauty!")
            set(DA_VINCI, "none")
            choice {
                badFeeling()
                giveMeTheStuff()
            }
        }

        npcOperate("Talk-to", "chancy") {
            if (!waiting(CHANCY, "Chancy doesn't feel like talking.")) {
                return@npcOperate
            }
            player<Happy>("Hi, thanks for doing that.")
            if (get(CHANCY, "none") == "delivered") {
                npc<Happy>("No problem.")
                handBack(CHANCY, "liquid_honey", "He gives you the vial of liquid honey.")
                npc<Sad>("Next time give me something more valuable... I couldn't get anything for this on the blackmarket.")
                player<Neutral>("That was the idea.")
                return@npcOperate
            }
            npc<Happy>("No problem. I've got some money for you actually.")
            player<Confused>("What do you mean?")
            npc<Neutral>("Well, it turns out that potion you gave me, was quite valuable...")
            player<Shock>("What?")
            npc<Happy>("I know that I probably shouldn't have sold it... but some friends and I were having a little wager, the odds were just too good!")
            player<Angry>("You sold my vial and gambled with the money?!")
            npc<Happy>("Actually yes... but praise be to Saradomin because I won! So all's well that ends well right?")
            set(CHANCY, "none")
            choice {
                nothingFurther()
                noIdea()
            }
        }

        npcOperate("Talk-to", "hops") {
            if (!waiting(HOPS, "He isn't in a fit state to talk.")) {
                return@npcOperate
            }
            player<Happy>("Hello, how was your journey?")
            npc<Neutral>("Pretty thirst-inducing actually...")
            player<Neutral>("Please tell me that you haven't drunk the contents...")
            if (get(HOPS, "none") == "delivered") {
                npc<Shock>("Oh the gods no! What do you take me for?")
                npc<Happy>("Here's your vial anyway.")
                handBack(HOPS, "sulphuric_broline", "He gives you the vial of sulphuric broline.")
                player<Happy>("Thanks, I'll let you get your drink now.")
                return@npcOperate
            }
            npc<Shifty>("Of course I can tell you that I haven't drunk the contents...")
            npc<Neutral>("But I'd be lying. Sorry about that me old mucker, can I get you a drink.")
            set(HOPS, "none")
            player<Angry>("No I think you've done enough for now.")
        }
    }

    private suspend fun Player.busy(errand: String, carrying: String, silent: String): Boolean {
        if (get(errand, "none") != "none") {
            npc<Neutral>(carrying)
            return true
        }
        if (questStage("biohazard") != 12) {
            message(silent)
            return true
        }
        return false
    }

    private fun Player.waiting(errand: String, silent: String): Boolean {
        if (questStage("biohazard") != 12 || get(errand, "none") == "none") {
            message(silent)
            return false
        }
        return true
    }

    private suspend fun Player.offerVials(errand: String, wanted: String, wasted: String, farewell: suspend Player.() -> Unit) {
        choice {
            for (vial in VIALS) {
                option("You give him the vial of ${vial.replace('_', ' ')}...") {
                    if (!inventory.remove(vial)) {
                        statement("You can't give him what you don't have.")
                        return@option
                    }
                    set(errand, if (vial == wanted) "delivered" else wasted)
                    message("You give him the vial of ${vial.replace('_', ' ')}.")
                    farewell()
                }
            }
        }
    }

    private fun Player.handBack(errand: String, vial: String, text: String) {
        set(errand, "none")
        addOrDrop(vial)
        message(text)
    }

    private fun ChoiceOption.badFeeling(): Unit = option("I'm getting a bad feeling about this.") {
        player<Sad>("I'm getting a bad feeling about this. You do still have it don't you?")
        paintedIt()
    }

    private fun ChoiceOption.giveMeTheStuff(): Unit = option("Just give me the stuff now please.") {
        player<Neutral>("Just give me the stuff now please. You do still have it don't you?")
        paintedIt()
    }

    private suspend fun Player.paintedIt() {
        npc<Happy>("Absolutely. It's just not stored in a vial anymore.")
        player<Confused>("What?")
        npc<Happy>("Instead it has been liberated. It now gleams from the canvas of my latest epic: The Majesty of Varrock!")
        player<Sad>("That's great. Thanks to you I'll have to walk back to East Ardougne to get another vial.")
        npc<Neutral>("Well you can't put a price on art.")
    }

    private fun ChoiceOption.nothingFurther(): Unit = option<Angry>("No! Nothing could be further from the truth!") {
        npc<Neutral>("Well, there's no pleasing some people.")
    }

    private fun ChoiceOption.noIdea(): Unit = option<Angry>("You have no idea what you have just done!") {
        npc<Neutral>("Ignorance is bliss I'm afraid.")
    }

    private companion object {
        const val DA_VINCI = "biohazard_davinci_errand"
        const val CHANCY = "biohazard_chancy_errand"
        const val HOPS = "biohazard_hops_errand"
        val VIALS = listOf("ethenea", "liquid_honey", "sulphuric_broline")
    }
}
