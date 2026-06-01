package content.quest.free.blood_pact

import content.entity.player.bank.bank
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory

class BloodPact : Script{

    init {
        questJournalOpen("blood_pact") {
            val reeseStatus = get<String>("blood_pact_reese")
            val caitlinStatus = get<String>("blood_pact_caitlin")
            val kayleStatus = get<String>("blood_pact_kayle")

            val lines = when (quest("blood_pact")) {
                "completed" -> listOf(
                    "<str>Xenia, an old adventurer, said she had seen some Zamorakian cultists entering the catacombs beneath Lumbridge Church. She asked me to go with her into the catacombs to deal with them.",
                    "<str>Inside the catacombs, Xenia and I overheard the cultists talking about a blood pact.",
                    "<str>The first cultist shot Xenia, wounding her badly. She will not be able to fight.",
                    //if else
                    "<str>I defeated the first cultist.",
                    //if else
                    "<str>I defeated the second cultist.",
                    //if else
                    "<str>I defeated the third cultist.",
                    "<str>The death of the third cultist completed the ritual. A tomb in the catacombs collapsed, revealing a staircase.",
                    "<str>I untied the prisoner and escaped from the catacombs.",
                    "<str>Xenia thanked me for my help.",
                    "",
                    "<red>QUEST COMPLETE!",
                )

                "untie_ilona" -> listOf()

                "reese" -> {
                    val list = mutableListOf(
                        "<str>Xenia, an old adventurer, said she had seen some Zamorakian cultists entering the catacombs beneath Lumbridge Church. She asked me to go with her into the catacombs to deal with them.",
                        "<str>Inside the catacombs, Xenia and I overheard the cultists talking about a blood pact.",
                        "<str>The first cultist shot Xenia, wounding her badly. She will not be able to fight.",
                        "<str>I defeated the first cultist."
                    )

                    when(kayleStatus) {
                        "spared" -> list.add("<str>I spared the first cultist.")
                        "killed" -> list.add("<str>I killed the first cultist.")
                    }

                    list.add("<str>I defeated the second cultist.")

                    when(caitlinStatus) {
                        "spared" -> list.add("<str>I spared the second cultist.")
                        "killed" -> list.add("<str>I killed the second cultist.")
                    }

                    when (reeseStatus) {
                        "spared" -> {
                            list.add("<navy>destroy <maroon>Delrith<navy> using <maroon>Silverlight<navy>!.")
                        }
                        "killed" -> {
                            list.add("")
                            list.add("<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.")
                        }
                        "defeated" -> {
                            list.add("<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.")
                        }
                        else -> {
                            list.add("<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.")
                        }
                    }
                    list
                }

                "caitlin" -> {
                    val list = mutableListOf(
                        "<str>Xenia, an old adventurer, said she had seen some Zamorakian cultists entering the catacombs beneath Lumbridge Church. She asked me to go with her into the catacombs to deal with them.",
                        "<str>Inside the catacombs, Xenia and I overheard the cultists talking about a blood pact.",
                        "<str>The first cultist shot Xenia, wounding her badly. She will not be able to fight.",
                        "<str>I defeated the first cultist."
                        )

                    when(kayleStatus) {
                        "spared" -> list.add("<str>I spared the first cultist.")
                        "killed" -> list.add("<str>I killed the first cultist.")
                    }

                    when (caitlinStatus) {
                        "spared" -> {
                            list.add("<str>I defeated the second cultist.")
                            list.add("<navy>destroy <maroon>Delrith<navy> using <maroon>Silverlight<navy>!.")
                        }
                        "killed" -> {
                            list.add("<str>I defeated the second cultist.")
                            list.add("<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.")
                        }
                        "defeated" -> {
                            list.add("<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.")
                        }
                        else -> {
                            list.add("<navy>I need to defeat the <maroon>second cultist<navy>.")
                        }
                    }
                    list
                }

                "kayle", "xenia_wounded" -> {
                    val list = mutableListOf(
                        "<str>Xenia, an old adventurer, said she had seen some Zamorakian cultists entering the catacombs beneath Lumbridge Church. She asked me to go with her into the catacombs to deal with them.",
                        "<str>Inside the catacombs, Xenia and I overheard the cultists talking about a blood pact.",
                        "<str>The first cultist shot Xenia, wounding her badly. She will not be able to fight.",
                    )
                    when (kayleStatus) {
                        "spared" -> {
                            list.add("<str>I defeated the first cultist.")
                            list.add("<str>I spared the first cultist.")
                            list.add("<navy>I need to defeat the <maroon>second cultist<navy>.")
                        }
                        "killed" -> {
                            list.add("<str>I defeated the first cultist.")
                            list.add("<str>I killed the first cultist.")
                            list.add("<navy>I need to defeat the <maroon>second cultist<navy>.")
                        }
                        "defeated" -> {
                            list.add("<str>I defeated the first cultist.")
                            list.add("<navy>I should either <maroon>kill <navy>or <maroon>spare <navy>the <maroon>first cultist<navy>.")
                        }
                        else -> {
                            list.add("<navy>I need to defeat the <maroon>first cultist<navy>.")
                        }
                    }
                    list
                }

                "watched_cutscene" -> listOf(
                    "<str>Xenia, an old adventurer, said she had seen some Zamorakian cultists entering the catacombs beneath Lumbridge Church. She asked me to go with her into the catacombs to deal with them."
                    "<str>Inside the catacombs, Xenia and I overheard the cultists talking about a blood pact."
                    "<navy>I should accompany Xenia to fight the <maroon>first cultist."
                )

                "started" -> listOf(
                    "<maroon>Xenia<navy>, an old adventurer, said she had seen some Zamorakian ",
                    "<navy>cultists entering the catacombs beneath Lumbridge Church. ",
                    "<navy>She asked me to  go with her into the <maroon>catacombs ",
                    "<navy>to deal with them.",
                    "",
                )

                else -> listOf(
                    "<navy> I can start this quest by speaking to <maroon>Xenia <navy>in the <marron>Lumbridge cemetery.",
                    "",
                )
            }
            questJournal("Blood Pact", lines)
        }
    }
}