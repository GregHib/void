package content.area.misthalin.ham_hideout

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.entity.character.player.Player

class JohanhusUlsbrechtDialogue : Script {
    private val johanhus = "johanhus_ulsbrecht_ham_cave"

    init {
        npcOperate("Talk-to", johanhus) {
            dialogueMenu()
        }
    }

    private suspend fun Player.dialogueMenu() {
        choice("Select an Option") {
            option<Neutral>("What kind of organisation is this?") {
                organisationDialogue()
                dialogueMenu()
            }
            option<Neutral>("Who are you and what do you do here?") {
                identityDialogue()
                dialogueMenu()
            }
            option<Neutral>("Okay, thanks.") {
                closeDialogue()
            }
        }
    }

    private suspend fun Player.organisationDialogue() {
        npc<Happy>(johanhus, "We're a proactive organisation working towards the cessation of monsters in normal civilised human society. There seems to be no backbone in this land so we're stepping up to the challenge.")
        player<Neutral>("Hmm, how do you propose to do that? I mean, I see people every day killing goblins around here!")
        npc<Happy>(johanhus, "We're mobilising people and we're starting our own society...cleaning out the caves once inhabited by these foul creatures and defending them so that they can never again shelter sub-human species.")
        player<Neutral>("That sounds kind of strange, but, hey, it's your choice.")
    }

    private suspend fun Player.identityDialogue() {
        npc<Neutral>(johanhus, "My name is Johanhus and I lead these glorious people on a courageous mission called 'Humans Against Monsters'. We mean to make this land free of monsters, so that we can all live in peace.")
    }
}
