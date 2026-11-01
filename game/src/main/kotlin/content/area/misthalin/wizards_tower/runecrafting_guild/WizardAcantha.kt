package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
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

class WizardAcantha : Script {
    init {
        npcOperate("Talk-to", "wizard_acantha") {
            when (get("gop_team", "")) {
                "green" -> {
                    npc<Quiz>("The portal will open any moment. Are you ready?")
                    choice("Select an Option") {
                        option<Happy>("I'm ready!") {
                            npc<Happy>("Good, preparation is the key to success. Keep those yellow orbs away.")
                        }
                        option("No. What am I supposed to do?") {
                            player<Confused>("Sure. What am I supposed to do?")
                            npc<Happy>("When we began formally studying the rune altars, we saw a spike in the energy fields surrounding altars when there is an increase in rune production.")
                            npc<Neutral>("When an essence is used on the altar, energy is transferred and bound into the essence, creating a rune.")
                            npc<Neutral>("As with all transfers of energy, some is lost in the process. This lost energy, manifested as green orbs, needs to be returned to the altar to keep them well maintained.")
                            npc<Sad>("When the green energy escapes during the transfer, yellow energy particles in the air gather around the pure green energy. They form into opposing orbs and must be kept away as they are impure energy forms.")
                            npc<Neutral>("I need you to put the green orbs back in the altar. I will give you two wands to move the orbs around and a third wand to create barriers to prevent those pesky yellow orbs from getting in.")
                            npc<Neutral>("To further this cause, we've set up a project to repair the altars. I wanted to just call it The Orb Project, but the buffoon wanted it to sound grander, so now it's The Great Orb Project. As if it needed an adjective.")
                            menu()
                        }
                        option("I want to quit the team.") {
                            player<Scared>("I want to quit the team.")
                            npc<Neutral>("The youth of today, they can't commit to anything.")
                            GreatOrbProject.leave(this)
                        }
                    }
                }
                "yellow" -> npc<Quiz>("So, you've decided to help 'Wizard' Vief?")
                else -> {
                    npc<Happy>("You look slightly more intelligent than the rest of these goons. Will you help me?")
                    choice {
                        option<Happy>("Yes, I'll help.") {
                            join()
                        }
                        no()
                        option<Confused>("Help with what?") {
                            npc<Neutral>("Don't you keep up with the Runecrafting Theory Journal? We've been making major discoveries! The youth of today are so lazy. Shall I tell you about it?")
                            player<Confused>("Sure. What am I supposed to do?")
                            npc<Happy>("When we began formally studying the rune altars, we saw a spike in the energy fields surrounding altars when there is an increase in rune production.")
                            npc<Neutral>("When an essence is used on the altar, energy is transferred and bound into the essence, creating a rune.")
                            npc<Neutral>("As with all transfers of energy, some is lost in the process. This lost energy, manifested as green orbs, needs to be returned to the altar to keep them well maintained.")
                            npc<Sad>("When the green energy escapes during the transfer, yellow energy particles in the air gather around the pure green energy. They form into opposing orbs and must be kept away as they are impure energy forms.")
                            npc<Neutral>("I need you to put the green orbs back in the altar. I will give you two wands to move the orbs around and a third wand to create barriers to prevent those pesky yellow orbs from getting in.")
                            npc<Neutral>("To further this cause, we've set up a project to repair the altars. I wanted to just call it The Orb Project, but the buffoon wanted it to sound grander, so now it's The Great Orb Project. As if it needed an adjective.")
                            menu()
                        }
                    }
                }
            }
        }

        npcOperate("Join", "wizard_acantha") {
            if (get("gop_team", "") != "") {
                npc<Confused>("What do you think you're up to? You're already on a team.")
                return@npcOperate
            }
            choice("Are you sure you want to join?") {
                option("Yes.") {
                    join()
                }
                no()
            }
        }
    }

    private fun ChoiceOption.no() {
        option<Sad>("No.") {
            npc<Angry>("Humph. The youth today have no sense of adventure.")
        }
    }

    private suspend fun Player.join() {
        if (GreatOrbProject.cantJoin(this)) {
            npc<Angry>("You need your head, right hand and two free inventory slots, or I can't give you the necessary equipment.")
            return
        }
        GreatOrbProject.join(this, "green")
        npc<Neutral>("I knew you were an intelligent lad. Here are your wands. When each team has enough people, Wizard Elriss will open the portal and you can get started. While we wait, shall I tell you what you need to do?")
        choice("Choose an option:") {
            option<Confused>("You already told me what to do.") {
                npc<Angry>("I know that! I'm not senile, you know. Repetition is good for understanding. Tell me what you know, Mr Smarty Pants.")
                player<Scared>("Errr, green orbs good, yellow orbs bad.")
                npc<Laugh>("You won't be winning any awards for verbosity, but you got the gist of it.")
            }
            option<Happy>("Wizard Elriss told me what to do.") {
                npc<Neutral>("She's no fool, even if she is a little young for her position. Repeat it back to me, so I know you learned your lesson.")
                player<Quiz>("I use the wands you gave me to attract and repel the orbs towards the altar.")
                npc<Angry>("Not just any orbs! Only put green orbs into the altar. Do well and you will be rewarded for your efforts.")
                menu()
            }
            option<Happy>("Wizard Vief told me what to do.") {
                npc<Laugh>("That two-bit hack? That boy will quote you all kinds of algorithms and equations that other people have discovered. He's done nothing to forward the science of Runecrafting. I'll bet he told you to put yellow orbs into")
                npc<Laugh>("the altar.")
                player<Confused>("Is that wrong?")
                npc<Angry>("It's completely wrong. He's misinterpreted Wizard Hallam's discoveries about the elemental rune energy signature. That man was as crazy as Wizard Korvak over there. He really should have been reading the discoveries of Wizard")
                npc<Angry>("Fowl in the interpretation of dimensional energy.")
                npc<Neutral>("At any rate, the yellow orbs contain a centre of energy that is harmful to rune altars. They must be kept away. Green orbs must be put back to counteract the triangulated force of dimensional weight opposed against...")
                statement("You lose focus on the conversation and let her prattle on.")
                menu()
            }
            option<Confused>("Sure. What am I supposed to do?") {
                npc<Happy>("When we began formally studying the rune altars, we saw a spike in the energy fields surrounding altars when there is an increase in rune production.")
                npc<Neutral>("When an essence is used on the altar, energy is transferred and bound into the essence, creating a rune.")
                npc<Neutral>("As with all transfers of energy, some is lost in the process. This lost energy, manifested as green orbs, needs to be returned to the altar to keep them well maintained.")
                npc<Sad>("When the green energy escapes during the transfer, yellow energy particles in the air gather around the pure green energy. They form into opposing orbs and must be kept away as they are impure energy forms.")
                npc<Neutral>("I need you to put the green orbs back in the altar. I will give you two wands to move the orbs around and a third wand to create barriers to prevent those pesky yellow orbs from getting in.")
                npc<Neutral>("To further this cause, we've set up a project to repair the altars. I wanted to just call it The Orb Project, but the buffoon wanted it to sound grander, so now it's The Great Orb Project. As if it needed an adjective.")
                menu()
            }
        }
    }

    private suspend fun Player.menu() {
        choice {
            greenInYellowAway()
            yellowInGreenAway()
            whatDoIGet()
            howCanYouBeSure()
        }
    }

    private fun ChoiceOption.yellowInGreenAway() {
        option("Right, yellow orbs into altar, green orbs away.") {
            player<Bored>("Right, yellow orbs into altar, green orbs away.")
            npc<Angry>("Read my lips, whippersnapper! NO YELLOW ORBS IN THE ALTAR.")
            player<Scared>("Eek!")
            player<Scared>("Okay, okay. Green orbs into the altar, yellow orbs away.")
        }
    }

    private fun ChoiceOption.greenInYellowAway() {
        option("Okay, okay. Green orbs into the altar, yellow orbs away.") {
            player<Bored>("Okay, okay. Green orbs into the altar, yellow orbs away.")
            npc<Happy>("Well, at least you aren't as thick as that Wizard Vief. Make sure to stop his apprentices putting yellow orbs into altars. Use your wands intelligently and you'll come out victorious.")
        }
    }

    private fun ChoiceOption.whatDoIGet() {
        option("So, what do I get for all this hard work?") {
            player<Quiz>("So, what do I get for all this hard work?")
            npc<Laugh>("Aaaah, a true mercenary. Although we cannot agree scientifically, Vief and I have decided to reward those who help us. If you get more green orbs into the altar than the yellow team gets yellow orbs, you win the altar. You will")
            npc<Laugh>("receive rune essence each round you win. The team who wins the most altars will receive tokens which you can give to Wizard Elriss in exchange for a reward. I don't know the exact details, so it's best to speak to her.")
        }
    }

    private fun ChoiceOption.howCanYouBeSure() {
        option("How can you be sure about which orb is good?") {
            player<Quiz>("How can you be sure about which orb is good?")
            npc<Angry>("Do you think I got to where I am today on false calculations? I researched it, that's how I know!")
            npc<Sad>("There was a time when my opinion was respected and accepted without question. Age steals that from you.")
            npc<Angry>("I will not explain my findings to fools. If you want to see how I reached my conclusions you should keep up with the scientific wizarding community. I have no time for laziness!")
            npc<Angry>("Wizard Vief, in an attempt to progress his career, has decided to disagree with my findings and cast doubt upon my ability as a wizard. What better way to attract attention to one's self than disagree with those more")
            npc<Angry>("knowledgeable? Pay no attention to that man, he's a buffoon.")
        }
    }
}
