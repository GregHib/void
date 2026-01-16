package content.area.karamja.tzhaar_city

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill

object TzHaar {
    fun ChoiceOption.whatDidYouCallMe(target: NPC) {
        option<Angry>("What did you call me?") {
            val caste = caste(this)
            npc<Confused>("Are you not JalYt-$caste-$name?")
            choice {
                option<Quiz>("What's a 'JalYt-$caste'?") {
                    npc<Confused>(
                        "That what you are... you ${
                            when (caste) {
                                "Mej" -> "user of mystic powers"
                                "Ket" -> "tough and strong"
                                "Xil" -> "agile and quick"
                                "Hur" -> "skilled at making things"
                                else -> "mysterious"
                            }
                        } no?",
                    )
                    player<Confused>("Well yes I suppose I am...")
                    npc<Happy>("Then you JalYt-$caste!")
                    choice {
                        option<Quiz>("What are you then?") {
                            when {
                                target.id.startsWith("tzhaar_hur") -> {
                                    npc<Happy>("Silly JalYt, I am TzHaar-Hur, one of the crafters for this city.")
                                    npc<Neutral>("There are the wise TzHaar-Mej who guide us, the mighty TzHaar-Ket who guard us, and the swift TzHaar-Xil who hunt for our food.")
                                }
                                target.id.startsWith("tzhaar_ket") -> {
                                    npc<Happy>("Daft JalYt, I am TzHaar-Ket, one of the guardians of our city.")
                                    npc<Neutral>("There are the wise TzHaar-Mej who guide us, the swift TzHaar-Xil who hunt for our food, and the skilled TzHaar-Hur who craft our homes and tools.")
                                }
                                else -> {
                                    npc<Happy>("Foolish JalYt, I am TzHaar-Mej, one of the mystics of this city.")
                                    choice {
                                        option<Quiz>("What other types are there?") {
                                            npc<Neutral>("There are the mighty TzHaar-Ket who guard us, the swift TzHaar-Xil who hunt for our food, and the skilled TzHaar-Hur who craft our homes and tools.")
                                        }
                                        option<Neutral>("Ah ok then.")
                                    }
                                }
                            }
                        }
                        option<Sad>("Thanks for explaining it.")
                    }
                }
                option<Confused>("I guess so...") {
                    npc<Neutral>("Well then, no problems.")
                }
                option<Angry>("No I'm not!") {
                    npc<Shifty>("What ever you say, crazy JalYt!")
                }
            }
        }
    }

    fun caste(player: Player): String {
        val skills = listOf(
            Triple("Mej", Skill.Magic, Skill.Prayer),
            Triple("Ket", Skill.Strength, Skill.Defence),
            Triple("Xil", Skill.Attack, Skill.Agility),
            Triple("Hur", Skill.Crafting, Skill.Smithing),
        )
        var highest = 0
        var caste = ""
        for (skill in skills) {
            val value = player.levels.getMax(skill.second) + player.levels.getMax(skill.third)
            if (value > highest) {
                highest = value
                caste = skill.first
            }
        }
        return caste
    }
}
