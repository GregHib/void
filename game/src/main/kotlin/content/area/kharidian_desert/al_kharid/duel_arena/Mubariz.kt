package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Mubariz : Script {

    init {
        npcOperate("Talk-to", "mubariz") {
            npc<Happy>(
                """
                Welcome to the Duel Arena!
                What can I do for you?
            """,
            )
            menu()
        }
    }

    suspend fun Player.menu() {
        choice {
            place()
            duelling()
            options()
            advice()
            exit()
        }
    }

    fun ChoiceOption.place(): Unit = option<Confused>("What is this place?") {
        npc<Idle>("The Duel Arena has six arenas where you can fight other players in a controlled environment. We have our own dedicated hospital where we guarantee to put you back together, even if you lose.")
        npc<Idle>("The Duel Arena has six arenas where you can fight other players in a controlled environment. We have our own dedicated hospital where we guarantee to put you back together, even if you lose.")
        npc<Idle>("In between the arenas are walkways where you can watch the fights and challenge other players.")
        choice {
            looksOld()
            duelling()
            options()
            advice()
            exit()
        }
    }

    suspend fun Player.looksOld() {
        player<Confused>("It looks really old. Where did it come from?")
        npc<Idle>("The archaeologists that are excavating the area east of Varrock have been working on this site as well. From these cliffs they uncovered this huge building. The experts think it may date back to the second age!")
        npc<Idle>("Now that the archaeologists have moved out, a group of warriors, headed by myself, have bought the land and converted it to a set of arenas for duels. The best fighters from around the world come here to fight!")
        choice {
            challenge()
            duelling()
            options()
            advice()
            exit()
        }
    }

    suspend fun ChoiceOption.challenge(): Unit = option<Frustrated>("I challenge you!") {
        npc<Cackle>("Ho! Ho! Ho!")
        menu()
    }

    suspend fun Player.duelling() {
        player<Confused>("How do I challenge someone to a duel?")
        npc<Idle>("When you go to the arena you'll go up an access ramp to the walkways that overlook the arenas. From the walkways you can watch the duels and challenge other players.")
        npc<Idle>("You'll know you're in the right place as you'll have a Duel-with option when you right-click a player.")
        choice {
            challenge()
            place()
            options()
            advice()
            exit()
        }
    }

    fun ChoiceOption.options(): Unit = option<Confused>("What kind of options are there?") {
        npc<Idle>("You and your opponent can offer coins or platinum as a stake. If you win, you receive what your opponent staked minus some tax, but if you lose, your opponent will get whatever items you staked.")
        npc<Idle>("You can choose to use rules to spice things up a bit. For instance if you both agree to use the 'No Magic' rule then neither player can use magic to attack the other player. The fight will be restricted to ranging and")
        npc<Idle>("melee only.")
        npc<Idle>("The rules are fairly self-evident with lots of different combinations for you to try out!")
        choice {
            place()
            duelling()
            advice()
            exit()
        }
    }

    fun ChoiceOption.advice(): Unit = option<Quiz>("Do you have any advice for me?") {
        npc<Laugh>("Win. And if you ever stop having fun, stop dueling.")
    }

    fun ChoiceOption.exit(): Unit = option<Bored>("I'll be off.") {
        npc<Shifty>("See you in the arenas!")
    }
}
