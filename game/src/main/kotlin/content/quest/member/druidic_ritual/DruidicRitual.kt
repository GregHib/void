package content.quest.member.druidic_ritual

import content.entity.player.modal.tab.questJournalOpen
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.event.Script
@Script
class DruidicRitual {

    init {
        questJournalOpen("druidic_ritual") {
            val lines = when (player.quest("druidic_ritual")) {
                "completed" -> listOf(
                    "<str>I told Kaqemeex I would help them prepare their ceremony",
                    "<str>The ceremony required various meats being placed in the",
                    "<str>Cauldron of Thunder. I did this and gave them to Sanfew.",
                    "<str>Kaqemeex then tought me the basics of the skill Herblore.",
                    "",
                    "<red>QUEST COMPLETE!",
                    "",
                )
                "started" -> listOf(
                    "<str>I told Kaqemeex I would help them prepare their ceremony",
                    "<navy>I should speak to <maroon>Sanfew <navy>in the village to the <maroon>South",
                )
                "cauldron" -> listOf(
                    "<str>I told Kaqemeex I would help them prepare their ceremony",
                    "",
                    "<maroon>Sanfew <navy>told me for the ritual they would need me to place",
                    "<maroon>raw bear meat<navy>, <maroon>raw chicken<navy>, <maroon>raw rat meat <navy>and <maroon>raw beef <navy>in",
                    "<navy>the <maroon>Cauldron of Thunder <navy>in the <maroon>dungeon South <navy>of <maroon>Taverley",
                )
                "kaqemeex" -> listOf(
                    "<str>I told Kaqemeex I would help them prepare their ceremony",
                    "<str>The ceremony required various meats being placed in the",
                    "<str>Cauldron of Thunder. I did this and gave them to Sanfew.",
                    "<navy>I should speak to <maroon>Kaqemeex <navy>again and claim my <maroon>reward",
                )
                else -> listOf(
                    "<navy>I can start this quest by talking to <maroon>Kaqemeex <navy>at the<maroon> Taverley Stone Circle.",
                )
            }
            player.questJournal("Druidic Ritual", lines)
        }

    }

}
