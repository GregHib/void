package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.minigame.great_orb_project.GreatOrbProject
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class WizardVief : Script {
    init {
        npcOperate("Talk-to", "wizard_vief") {
            if (get("gop_team", "") == "yellow") {
                npc<Quiz>("The portal will open any moment. Are you ready?")
                choice("Select an Option") {
                    option<Happy>("I'm ready!") {
                        npc<Happy>("Excellent. I knew I could count on you.")
                    }
                    option<Neutral>("No. What am I supposed to do?") {
                        npc<Neutral>("It's quite complicated, but a runecrafter of your calibre should have no trouble understanding. After all, most of the people they let in here are highly intelligent.")
                        npc<Neutral>("The Runecrafting altars are sources of energy. Every time we craft runes, that energy comes out of the altars and into the essence.")
                        npc<Neutral>("Sometimes, too much energy comes out, and it forms into yellow energy orbs that hang in the air. These attract another form of energy from the surrounding space, which manifests as green orbs.")
                        npc<Neutral>("The yellow orbs need to be put back into the altar. The green orbs should be kept away. You can move the orbs around by attracting and repelling them using special wands. I'll give you a third wand to create barriers.")
                        npc<Neutral>("Barriers will stop green orbs from getting to the altar.")
                        npc<Quiz>("Yellow orbs are good, green ones are bad. Got that?")
                        choice {
                            greenGoodYellowBad()
                            yellowGoodGreenBad()
                            whatsInItForMe()
                            howCanYouBeSure()
                        }
                    }
                    option("I want to quit the team.") {
                        // TODO quit, no message
                    }
                }
                return@npcOperate
            }
            npc<Happy>("Ah! You'll help me, won't you?")
            choice {
                option<Happy>("Yes, I'll help.") {
                    join()
                }
                no()
                option<Quiz>("Help with what?") {
                    npc<Happy>("You must join The GREAT Orb Project! It's a wonderful name, isn't it? All my idea.")
                    npc<Quiz>(" Oh, I'm sorry. Haven't I told you what to do yet?")
                    choice("Choose an option:") {
                        option<Neutral>("Yes, you told me what to do.") {
                            npc<Neutral>("Excellent.")
                        }
                        option<Neutral>("Wizard Elriss told me what to do.") {
                            npc<Neutral>("Well, that's something, but I should make sure you have the details right. Remember, you need to get the yellow orbs into the altar. Got that? The yellow ones. Keep the green ones away.")
                            choice {
                                greenGoodYellowBad()
                                yellowGoodGreenBad()
                                whatsInItForMe()
                                howCanYouBeSure()
                            }
                        }
                        option<Neutral>("Wizard Acantha told me what to do.") {
                            npc<Angry>("You listened to her? You idiot!")
                        }
                        option<Neutral>("No. What am I supposed to do?") {
                            npc<Neutral>("It's quite complicated, but a runecrafter of your calibre should have no trouble understanding. After all, most of the people they let in here are highly intelligent.")
                            npc<Neutral>("The Runecrafting altars are sources of energy. Every time we craft runes, that energy comes out of the altars and into the essence.")
                            npc<Neutral>("Sometimes, too much energy comes out, and it forms into yellow energy orbs that hang in the air. These attract another form of energy from the surrounding space, which manifests as green orbs.")
                        }
                    }
                }
            }
        }

        npcOperate("Join", "wizard_vief") {
            join()
        }
    }

    private fun ChoiceOption.no() {
        option<Neutral>("No.") {
            npc<Neutral>("Suit yourself. I'm sure I'll manage without you.")
        }
    }

    private fun ChoiceOption.yellowGoodGreenBad() {
        option<Neutral>("Yellow good, green bad. Got it.") {
            npc<Happy>("That's right! I can tell that you belong in the Runecrafting Guild.")
        }
    }

    private fun ChoiceOption.greenGoodYellowBad() {
        option<Neutral>("Green good, yellow bad. Got it.") {
            npc<Angry>("Weren't you paying attention? Yellow orbs are good, green ones are bad! Move the yellow ones next to the altar and keep the green ones away.")
        }
    }

    private fun ChoiceOption.whatsInItForMe() {
        option<Quiz>("What's in it for me, anyway?") {
            npc<Neutral>("A sensible question. Of course, I know you're busy and I wouldn't expect you to work for nothing. If you succeed in getting more yellow orbs into the altar than the green team gets green orbs, you win the altar. You will be")
        }
    }

    private fun ChoiceOption.howCanYouBeSure() {
        option<Quiz>("How can you be sure you're right?") {
            npc<Shock>("I...but...isn't it obvious?")
            npc<Neutral>("You just need to take Wizard Hallam's discoveries about the elemental rune energy signature and then extrapolate to include all rune types. Then, if you apply a Lefebvre transformation to them... I assume you know the")
            npc<Neutral>("Lefebvre equations?")
            player<Confused>("Well, I-")
            npc<Neutral>("Of course you do, I shouldn't have asked. Anyway, if you do that, you'll easily see the signature of an external manifestation of Runecrafting energy. Then you need to apply Wizard Beenay's theory of light...")
            statement("Wizard Vief continues at length and you don't understand any of it.")
        }
    }

    private suspend fun Player.join() {
        if (get("gop_team", "") != "") {
            statement("You are already in a team.")
            return
        }
        choice("Are you sure you want to join?") {
            option("Yes.") {
                if (equipped(EquipSlot.Hat).isNotEmpty() || equipped(EquipSlot.Hands).isNotEmpty()) {
                    npc<Neutral>("All that junk you're carrying will interfere with the magic. I'll need to give you a hat, put something in your hand, and two things in your inventory.")
                    return@option
                }
                GreatOrbProject.join(this, "yellow")
                npc<Happy>("Excellent! You are now part of my team.")
            }
            no()
        }
    }
}
