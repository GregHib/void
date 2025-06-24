package content.area.kandarin.tree_gnome_stronghold

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.SuspendableContext

npcOperate("Talk-to", "captain_errdo") {
    choice {
        takeMe()
        option<Talk>("Why are gliders better than other transport?") {
            npc<Happy>("Oh we have a whole network! It's wonderful for getting to hard to reach places.")
            npc<Happy>("There are so many places where your teleports cannot reach!")
            player<Happy>("How did you all manage to build such an established network?")
            npc<Talk>("I think you'll find that is a gnome trade secret!")
            choice {
                takeMe()
                nothing()
            }
        }
        nothing()
    }
}

fun ChoiceBuilder<NPCOption<Player>>.takeMe() {
    option<Quiz>("Can you take me on the glider?") {
        npc<Happy>("Of course!")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.nothing() {
    option<Uncertain>("Sorry, I don't want anything now.")
}

suspend fun SuspendableContext<Player>.embarrassing() {
    npc<Uncertain>("Ah, how embarrassing.")
    player<Quiz>("What happened?")
    npc<Talk>("A bit of a technical hitch with the landing gear. I won't be able to fly you anywhere, sorry.")
}