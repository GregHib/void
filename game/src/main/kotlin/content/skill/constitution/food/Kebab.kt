package content.skill.constitution.food

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class Kebab : Script {

    val phrases = listOf("Lovely!", "Scrummy!", "Delicious!", "Yum!")

    init {
        consumed("kebab") { _, _ ->
            val random = random.nextDouble(100.0)
            when {
                random < 66 -> {
                    levels.restore(Skill.Constitution, multiplier = 0.10)
                    message("It heals some health.", ChatType.Filter)
                }
                random < 87 -> {
                    levels.restore(Skill.Constitution, amount = (100..200).random())
                    message("That was a good kebab. You feel a lot better.", ChatType.Filter)
                }
                random < 96 -> message("That kebab didn't seem to do a lot.", ChatType.Filter)
                else -> {
                    levels.restore(Skill.Constitution, amount = (100..300).random())
                    levels.boost(Skill.Attack, (2..3).random())
                    levels.boost(Skill.Strength, (2..3).random())
                    levels.boost(Skill.Defence, (2..3).random())
                    message("Wow, that was an amazing kebab! You feel really invigorated.", ChatType.Filter)
                }
            }
        }

        consumed("super_kebab") { _, _ ->
            if (random.nextInt(8) < 5) {
                levels.restore(Skill.Constitution, 30, 0.07)
            }
            if (random.nextInt(32) < 1) {
                val skill = Skill.all.filterNot { it == Skill.Constitution }.random()
                levels.drain(skill, multiplier = 0.05)
                message("That tasted very dodgy. You feel very ill.", ChatType.Filter)
                message("<red>Eating the kebab has done damage to some of your stats.")
            }
        }

        consumed("ugthanki_kebab") { _, _ ->
            if (levels.get(Skill.Constitution) != levels.getMax(Skill.Constitution)) {
                say(phrases.random())
            }
        }
    }
}
