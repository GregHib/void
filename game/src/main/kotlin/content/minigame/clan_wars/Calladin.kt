package content.minigame.clan_wars

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Expression
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.nameEntry
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player

class Calladin : Script {

    companion object {
        private const val LAST_MENU_CHOICE = "calladin_last_menu_choice"
        private const val ABOUT_CLAN_WARS = "about_clan_wars"
        private const val ABOUT_CALLER = "about_caller"
        private const val ABOUT_FFA = "about_ffa"
        private const val SET_CALLER = "set_caller"
        private const val NOTHING = "nothing"
    }

    init {
        npcOperate("Talk-to", "calladin") {
            clear(LAST_MENU_CHOICE)
            npc<Happy>("Hiya. Do you want me to tell you about Clan Wars or do you want me to set your caller?")
            menu()
        }

        npcOperate("Set-caller", "calladin") {
            setCaller()
        }
    }

    private suspend fun Player.menu() {
        val lastChoice = get(LAST_MENU_CHOICE, "")
        choice {
            rememberedOptionInline<Quiz>(lastChoice, ABOUT_CLAN_WARS, "Tell me about Clan Wars.") {
                npc<Happy>("Clans come here to fight each other, army against army, battling for supremacy in our arenas.")
                npc<Happy>("If your rank in your Clan is captain or higher, you may challenge a senior member of another Clan group to a war.")
                npc<Neutral>("The two of you must choose the rules for the war from the setup menu. When you're satisfied, choose 'Accept' and wait for your opponent to do the same.")
                npc<Happy>("The two of you will immediately be taken to an arena, where the rest of your Clan may join you via the portal at the southern end of this Challenge Hall.")
                npc<Bored>("The two armies are initially separated by a barrier. After two minutes, the barrier will sink into the ground and the war begins.")
                npc<Neutral>("The winning Clan will be chosen according to the rules that were set at the beginning.")
                npc<Happy>("Remember: if you leave the arena or log out in there, it counts as a kill for the opposing team.")
                player<Quiz>("What happens if I die?")
                npc<Shifty>("If the rules state that items are dropped on death, you'll drop everything you're carrying. You won't be able to get it back.")
                npc<Bored>("Alternatively, if the rules state that you can keep your items when you die, then you'll keep them.")
                npc<Happy>("You'll respawn in a viewing area where you may watch the war. Depending on the rules, you may bbe allowed to re-enter the fight by passing through the portal at the southern end of this Challenge Hall.")
                npc<Happy>("What can I do for you next?")
                menu()
            }
            rememberedOptionInline<Quiz>(lastChoice, ABOUT_CALLER, "What's a caller?") {
                npc<Neutral>("During a war, you might need to listen for instructions from a senior member of your Clan. Unfortunately, it can be a bit tricky to spot their chat in the middle of all the chaos.")
                npc<Happy>("I can help you set that person as your caller. During the war, all the messages you receive from that person will be copied to your heads-up display.")
                npc<Bored>("You can change your caller during the war by clicking on your heads-up display, but it's more convenient to do it here with me.")
                npc<Shifty>("Alternatively, you might find it useful to set someone in the enemy clan as your caller - heh heh heh.")
                npc<Quiz>("Anything else?")
                menu()
            }
            rememberedOptionInline<Quiz>(lastChoice, ABOUT_FFA, "What are the 'free-for-all' portals?") {
                npc<Neutral>("If you fancy a bit of casual mayhem, just wander through one of those portals. They lead to arenas where there are no teams or rules.")
                npc<Laugh>("You can fight how you like against anyone you meet, making your own entertainment.")
                player<Shifty>("What happens if I die?")
                npc<Shifty>("There are two portals...")
                npc<Bored>("The western portal leads to an arena where you keep your items when you die. You can teleport out at any time.")
                npc<Shifty>("The eastern portal leads to an arena where you drop ALL your items on death, and you won't be able to get them back. You won't be allowed to teleport out of that arena.")
                npc<Quiz>("Anything else?")
                menu()
            }
            rememberedOptionInline<Neutral>(lastChoice, SET_CALLER, "I want to set my caller.") {
                setCaller()
            }
            rememberedOptionInline<Neutral>(lastChoice, NOTHING, "No, I'm fine.")
        }
    }

    private suspend fun Player.setCaller() {
        val caller = nameEntry("Enter the name of your caller:")
        set("clan_wars_caller", caller)
        message("Caller set: $caller")
    }

    private inline fun <reified E : Expression> ChoiceOption.rememberedOptionInline(lastChoice: String, id: String, text: String, noinline block: suspend Player.() -> Unit = {}) {
        if (lastChoice == id) {
            return
        }
        option<E>(text) {
            set(LAST_MENU_CHOICE, id)
            block()
        }
    }
}
