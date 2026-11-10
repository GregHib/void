package content.area.fremennik_province.iceberg

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/**
 * Jim the PBJ agent guards the penguin hunter area after Hunt for Red Raktuber.
 * Tuxedo-Time needs the clockwork penguin suit from Cold War which isn't implemented yet.
 * https://runescape.wiki/w/Transcript:Jim
 */
class Jim : Script {

    init {
        npcOperate("Talk-to", "jim_iceberg_3") {
            if (get("jim_hunting_ground_explained", false)) {
                handIn()
                return@npcOperate
            }
            explain()
        }

        npcOperate("Tickle", "jim_iceberg_3") { (target) ->
            target.say("Hee hee hee")
            message("You tickle the polar bear.")
        }
    }

    private suspend fun Player.explain() {
        npc<Shifty>("Pssst.")
        player<Quiz>("Oh, so you can talk now?")
        npc<Shifty>("I could always talk. But I didn't want to reveal myself to the enemy.")
        player<Quiz>("Well, what do you want?")
        npc<Sad>("I have heard of Larry's capture. I am sorry for your loss. Larry is very brave.")
        player<Sad>("Yeah. I hope he's okay. Where ever he is.")
        npc<Neutral>("That is why I wish to speak to you. I have been instructed to assist you in infiltrating the penguins and training for future assignments.")
        player<Quiz>("What? What future assignments?")
        npc<Neutral>("When you go to rescue Larry, of course. It is not a task to be approached unprepared.")
        player<Quiz>("So what exactly am I supposed to do?")
        npc<Neutral>("Should you wish to infiltrate the penguins, I have been granted access to the spell that puts you in your penguin suit.")
        npc<Neutral>("There is more than that, though. A new iceberg has floated down from the north, bringing with it reinforcements for the Penguin Outpost.")
        npc<Neutral>("To prepare you for the times ahead, we have provided some equipment for you to practise hunting and tracking penguins.")
        npc<Neutral>("In exchange for the experience you gain, we only ask that you hand over the captured KGP agents to us.")
        npc<Neutral>("Simply jump across the ice floes here.")
        player<Quiz>("Why KGP agents? What will you do with them?")
        npc<Shifty>("That is classified. You do not need to know.")
        set("jim_hunting_ground_explained", true)
        choice("What do you want to do?") {
            option("Press him further.") {
                player<Quiz>("That's not good enough? I'm tired of just taking orders. What will you do with the captured penguins?")
                npc<Neutral>("Very well, if you insist on knowing. We will interrogate them and keep them imprisoned. They may reveal valuable information about Larry's location. That is why we only want KGP agents, the other penguins know nothing of interest.")
            }
            option("Ignorance is bliss.") {
                player<Neutral>("Fine, I don't want to know.")
                npc<Neutral>("It is for the best.")
            }
        }
    }

    private suspend fun Player.handIn() {
        npc<Quiz>("Do you have any captured KGP agents you would like to hand over?")
        choice {
            option("Yes.") {
                player<Happy>("Yes, I do have some.")
                npc<Neutral>("Very good. Let me take a look.")
                val agents = inventory.count("kgp_agent")
                if (agents == 0 || !inventory.remove("kgp_agent", agents)) {
                    npc<Neutral>("You don't appear to have any with you.")
                    return@option
                }
                // 2 experience per agent https://runescape.wiki/w/Penguin_Hunter_area?oldid=4143804
                exp(Skill.Hunter, agents * 2.0)
                npc<Happy>("The PBJ thanks you for your help.")
            }
            option("No.") {
                player<Neutral>("No, I don't have any for you right now.")
            }
        }
    }
}
