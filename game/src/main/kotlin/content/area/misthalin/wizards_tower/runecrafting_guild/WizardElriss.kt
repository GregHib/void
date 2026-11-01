package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class WizardElriss : Script {
    init {
        npcOperate("Talk-to", "wizard_elriss") {
            npc<Happy>("Welcome to the Runecrafting Guild.")
            choice {
                whatIsThisPlace()
                whatCanIDoHere()
                whoIsOutside()
                neverMind()
            }
        }

        npcOperate("Exchange", "wizard_elriss") {
            open("rc_guild_rewards")
        }
    }

    private fun ChoiceOption.whoIsOutside() {
//        option("Who is that wizard outside the guild portal?") {
//            npc<Happy>("Ah, you have met Wizard Finix; a promising young wizard.")
//            npc<Shifty>("Even if he does have some rather controversial theories.")
//            player<Quiz>("What theories has he come up with?")
//        }
    }

    private fun ChoiceOption.whatCanIDoHere() {
        option<Quiz>("What can I do here?") {
            npc<Neutral>("Wizard Acantha and Wizard Vief are running The Great Orb Project. It requires large numbers of runecrafters, so you should speak with them if you want something to do.")
            choice {
                option<Neutral>("Tell me about Acantha and Vief's project.") {
                    explain()
                }
                neverMind()
            }
        }
    }

    private suspend fun Player.explain() {
        npc<Neutral>("The Orb Proj...I beg your pardon, The Great Orb Project? It's truly fascinating. Wizards Acantha and Vief have found that energy leaks out of some of the Runecrafting altars. They are recruiting teams of experienced")
        npc<Neutral>("runecrafters, such as yourself, to force the energy back in.")
        npc<Neutral>("Join one of the teams by speaking to Wizard Acantha or Wizard Vief. When the wizards have enough helpers, I will open a portal to the Air Altar.")
        npc<Neutral>("The energy appears in the form of floating orbs. These can be moved by means of wands that attract or repel them. Acantha or Vief will give you one of each wand.")
        npc<Neutral>("Your goal is to move the correct colour orb to the altar stone, while keeping the other orbs away. Wizard Acantha favours the green orbs, while Wizard Vief favours the yellow ones.")
        npc<Neutral>("You will also have a third wand, which allows you to create magical barriers to block the opposing team's orbs.")
        npc<Neutral>("After two minutes, the team that absorbed the most orbs wins that altar. I then open the portal to the next altar in the sequence. After you have visited all eight altars, you will be returned here.")
        choice {
            whatsInItForMe()
            instructions()
            bestColour()
            option("Thanks.")
        }
    }

    private fun ChoiceOption.whatIsThisPlace() {
        option<Quiz>("What is this place?") {
            npc<Neutral>("This is the Runecrafting Guild, as I said. I set the guild up several years ago as a place for the most advanced runecrafters to work together.")
            choice {
                option<Quiz>("Work together towards what?") {
                    npc<Neutral>("Towards a greater understanding of Runecrafting, of course. The basics of Runecrafting were preserved when the first Tower was destroyed, but many of the more advanced secrets remain unknown.")
                }
                whereAreWe()
                whatCanIDoHere()
                neverMind()
            }
        }
    }

    private fun ChoiceOption.whereAreWe() {
        option<Quiz>("Where are we, exactly?") {
            npc<Neutral>("You will have noticed that, whenever you use a Runecrafting altar, you enter another place: a self- contained island, or cave, or some other place, which contains the true altar.")
            npc<Neutral>("These temples are not exactly on Gielinor. They are pocket dimensions unto themselves: areas of folded space created by the energy of the rune altar.")
            choice {
                option<Quiz>("So, we're in something similar?") {
                    npc<Neutral>("Quite right. This is a shadow of the Wizards' Tower, created by our own magic - also known as the Runespan. What better place to study the mysteries of Runecrafting?")
                }
                option<Quiz>("What does that have to do with the guild?") {
                    npc<Neutral>("Don't you see? The Runecrafting Guild exists in a similar pocket dimension, created by our own magic - also known as the Runespan. What better place to study the mysteries of Runecrafting?")
                }
                option<Neutral>("Not the Astral Altar. That has no pocket dimension.") {
                    npc<Happy>("Quite right. I have heard of the Astral Altar, although I have not been there myself.")
                    npc<Neutral>("The lunar wizards have found a way to keep the altar open. Their magic has flattened out the space around the altar so the pocket dimension becomes part of normal space.")
                }
            }
        }
    }

    private fun ChoiceOption.bestColour() {
        option<Quiz>("Which colour orb is best?") {
            npc<Neutral>("Wizard Acantha believes that the green orbs are best. Wizard Vief believes that the yellow ones are. You should help out the wizard whose team you join.")
            player<Quiz>("But what do you think?")
            npc<Quiz>("Does it matter?")
            choice {
                option<Shock>("Of course it matters.") {
                    npc<Neutral>("Of course it does, of course it does. Be careful which team you join, then. I'll accept your reward tokens, either way.")
                }
                option<Neutral>("No, I suppose not.") {
                    npc<Neutral>("No. The important thing is that the orbs get pushed back into the altars, whatever colour they are.")
                    choice {
                        whatIsThisPlace()
                        whatCanIDoHere()
                        whoIsOutside()
                        neverMind()
                    }
                }
                neverMind()
            }
        }
    }

    private fun ChoiceOption.instructions() {
        option<Neutral>("Could you go over the instructions again?") {
            explain()
        }
    }

    private fun ChoiceOption.neverMind() {
        option<Neutral>("Never mind.")
    }

    private fun ChoiceOption.whatsInItForMe() {
        option<Quiz>("What's in it for me?") {
            npc<Happy>("A fair question. We have agreed on a token scheme that allows you to choose from several rewards. When you return from the last altar, your senior wizard will give you a number of tokens.")
            npc<Neutral>("You will get 50 tokens per altar that your team captured, provided that you contributed to the capture in some way. You will get an extra 100 tokens if your team captured more altars overall, or 50 extra if it is a draw.")
            npc<Neutral>("You can exchange the tokens for rewards by speaking to me.")
        }
    }
}
