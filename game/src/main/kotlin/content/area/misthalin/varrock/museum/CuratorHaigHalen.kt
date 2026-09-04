package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory

class CuratorHaigHalen : Script {

    init {
        npcOperate("Talk-to", "curator_haig_halen") {
            val hasGolemStatueProgress = get("golem_b", 0) >= 3
            val inGolemQuestStage = questStage("the_golem") in 2..3
            val missingStatuette = !inventory.contains("statuette_the_golem")
            val hasSeenUnderground = get("golem_seen_underground", false)

            npc<Neutral>("Welcome to the museum of Varrock.")
            choice {
                anyNews()
                findAnyTreasure()
                if (hasGolemStatueProgress && inGolemQuestStage && missingStatuette && hasSeenUnderground) {
                    lookingForStatuette()
                }
            }
        }

        npcOperate("Pickpocket", "curator_haig_halen") {
            if (get("golem_b", 0) >= 3 &&
                questStage("the_golem") in 2..3 &&
                !inventory.contains("display_cabinet_key") &&
                !inventory.contains("statuette_the_golem")
            ) {
                if (!has(Skill.Thieving, 25)) {
                    message("You need to be a level 25 thief to pickpocket the curator.")
                    return@npcOperate
                }
                addOrDrop("display_cabinet_key")
                item("display_cabinet_key", "You steal a tiny key.")
                return@npcOperate
            }
            message("The curator doesn't seem to have anything of value.")
        }
    }

    private fun ChoiceOption.anyNews() {
        option("Have you any interesting news?") {
            player<Neutral>("Have you any interesting news?")
            npc<Neutral>("Yes, we found a rather interesting island to the north of Morytania. We believe it may be of archaeological significance.")
            player<Quiz>("Oh? That sounds interesting.")
            npc<Neutral>("Indeed. I suspect we'll be looking for qualified archaeologists once we have built the transport we need to get there.")
            player<Quiz>("Would I qualify then?")
            npc<Happy>("You've certainly done a lot to help out Varrock Museum, so we'd be silly not to ask you for your expertise.")
            // TODO less than 100 kudos "Although it's not necessarily a prerequisite, you might also want to consider helping out more here at Varrock Museum."
            player<Happy>("Thank you. I'll look forward to it!")
        }
    }

    private fun ChoiceOption.findAnyTreasure() {
        option("Do you know where I could find any treasure?") {
            player<Quiz>("Do you know where I could find any treasure?")
            npc<Happy>("Look around you! This museum is full of treasures!")
            player<Angry>("No, I meant treasures for ME.")
            npc<Happy>("Any treasures this museum knows about it goes to great lengths to acquire.")
        }
    }

    private fun ChoiceOption.lookingForStatuette() {
        option("I'm looking for a statuette recovered from the city of Uzer.") {
            player<Neutral>("I'm looking for a statuette recovered from the city of Uzer.")
            set("the_golem", "find_statuette")
            refreshQuestJournal()
            npc<Happy>("Ah yes, a very impressive artefact. The people of that city were excellent sculptors.")
            if (get("golem_retrieved_statuette", false)) {
                set("golem_retrieved_statuette", false)
                npc<Neutral>("That statuette was stolen recently, but now it's been returned.")
                return@option
            }
            npc<Happy>("It's in the display case upstairs.")
            player<Neutral>("No, I need to take it away with me.")
            npc<Quiz>("What do you want it for?")
            choice {
                openAPortal()
                justWantIt()
            }
        }
    }

    private fun ChoiceOption.openAPortal() {
        option("I want to open a portal to the lair of an elder-demon.") {
            player<Neutral>("I want to open a portal to the lair of an elder-demon.")
            npc<Shock>("Good heavens! I'd never let you do such a dangerous<br>thing.")
        }
    }

    private fun ChoiceOption.justWantIt() {
        option("Well, I, er, just want it.") {
            player<Confused>("Well, I, er, just want it.")
            npc<Angry>("Well, you can't have it! This museum never lets go of its treasures.")
        }
    }
}
