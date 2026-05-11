package content.area.wilderness.daemonheim

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearHint
import world.gregs.voidps.engine.client.hint
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class DungeoneeringTutor : Script {
    init {
        npcOperate("Talk-to", "dungeoneering_tutor") {
            npc<Frustrated>("Greetings, adventurer.")
            if (!ownsItem("ring_of_kinship")) {
                npc<Neutral>("Before we carry on, let me give you this.")
                if (!inventory.add("ring_of_kinship")) {
                    npc<Sad>("Oh, your hands are full? Come back when you've got space in your inventory.")
                    return@npcOperate
                }
                item("ring_of_kinship", 300, "He hands you a ring.")
            }
            menu()
        }

        timerStart("dungeoneering_tutor_hint_timeout") {
            TimeUnit.SECONDS.toTicks(30)
        }

        timerStop("dungeoneering_tutor_hint_timeout") {
            clearHint()
        }
    }

    private suspend fun Player.menu() {
        choice {
            whatIsThisPlace()
            whatCanIDoHere()
            whatDoesThisRingDo()
            showMeTheJournals()
        }
    }

    private fun ChoiceOption.showMeTheJournals() {
        option("Show me the journals I've found.") {
            open("dungeon_journals")
        }
    }

    private fun ChoiceOption.whatDoesThisRingDo() {
        option<Quiz>("What does this ring do?") {
            npc<Frustrated>("Raiding these forsaken dungeons can be a lot more rewarding if you're fighting alongside friends and allies. It should be more fun and you'll gain experience faster.")
            npc<Frustrated>("The ring shows others that you are interested in raiding a dungeon. It allows you to form, join and manage a raiding party.")
            npc<Frustrated>("We've also set up rooms with the specific purpose of finding a party for you.")
            npc<Happy>("Would you like me to show you? It's the fastest way into the dungeons of Daemonheim.")
            choice {
                option<Happy>("Yes, please.") {
                    npc<Happy>("It's this way.")
                    hint(Tile(3449, 3744), radius = 2)
                    softTimers.start("dungeoneering_tutor_hint_timeout")
                }
                option<Neutral>("No thanks, not right now.") {
                    npc<Frustrated>("Suit yourself.")
                    menu()
                }
            }
        }
    }

    private fun ChoiceOption.whatCanIDoHere() {
        option<Quiz>("What can I do here?") {
            npc<Frustrated>("Beneath these ruins you will find a multitude of dungeons, filled with strange creatures and resources.")
            npc<Frustrated>("Unfortunately, due to the taint that permeates this place, we cannot risk you taking items in or out of Daemonheim.")
            choice {
                howWillISurvive()
                whyNoKit()
                daemonheim()
                back()
            }
        }
    }

    private fun ChoiceOption.howWillISurvive() {
        option<Quiz>("How will I survive without kit?") {
            npc<Frustrated>("When we were within Daemonheim, we found a number of unknown resources. You can use your skills to fashion them into armour and weapons to keep your party alive.")
            npc<Frustrated>("Our contact within the dungeon can give you guidance on each of the items you find. He might even give you some gear to start you off.")
            choice {
                howWillISurvive()
                whyNoKit()
                daemonheim()
                back()
            }
        }
    }

    private fun ChoiceOption.back() {
        option("Back...") {
            menu()
        }
    }

    private fun ChoiceOption.whyNoKit() {
        option<Quiz>("Why can't I take my kit in?") {
            npc<Frustrated>("Within Daemonheim there is a taint. The metal, wood, even the plantlife are not of this world. We cannot risk exposing the surface to them.")
            npc<Frustrated>("Equally, if we allowed you to take items inside, we could not allow you to return with them.")
            npc<Frustrated>("For this reason, our seers have erected a barrier. Nothing in, nothing out without express permission.")
            choice {
                howWillISurvive()
                daemonheim()
                back()
            }
        }
    }

    private fun ChoiceOption.whatIsThisPlace() {
        option<Quiz>("What is this place?") {
            npc<Frustrated>("This is a place of treasures, fierce battles and bitter defeats.")
            npc<Frustrated>("We fought our way into the dungeons beneath this place.")
            npc<Sad>("Those of us who made it out alive...")
            npc<Frustrated>("...called this place Daemonheim.")
            choice {
                daemonheim()
                whatIsThisPlace()
                whatDoesThisRingDo()
                showMeTheJournals()
            }
        }
    }

    private fun ChoiceOption.daemonheim() {
        option<Quiz>("Daemonheim?") {
            npc<Frustrated>("Yes. It resembles the Niflheim of our stories: The Halls of the Dead.")
            npc<Angry>("The creatures within, however, are very much alive...")
            npc<Sad>("...unlike my fallen brothers and sisters.")
            choice {
                whatIsThisPlace()
                whatCanIDoHere()
                whatDoesThisRingDo()
                showMeTheJournals()
            }
        }
    }
}
