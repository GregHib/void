package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class Kebab : Script {

    val phrases = listOf("Lovely!", "Scrummy!", "Delicious!", "Yum!")

    init {
        consume("kebab") { player ->
            val random = random.nextDouble(100.0)
            when {
                random < 66 -> {
                    player.levels.restore(Skill.Constitution, multiplier = 0.10)
                    player.message("It heals some health.", ChatType.Filter)
                }
                random < 87 -> {
                    player.levels.restore(Skill.Constitution, amount = (100..200).random())
                    player.message("That was a good kebab. You feel a lot better.", ChatType.Filter)
                }
                random < 96 -> player.message("That kebab didn't seem to do a lot.", ChatType.Filter)
                else -> {
                    player.levels.restore(Skill.Constitution, amount = (100..300).random())
                    player.levels.boost(Skill.Attack, (2..3).random())
                    player.levels.boost(Skill.Strength, (2..3).random())
                    player.levels.boost(Skill.Defence, (2..3).random())
                    player.message("Wow, that was an amazing kebab! You feel really invigorated.", ChatType.Filter)
                }
            }
            cancel()
        }

        consume("super_kebab") { player ->
            if (random.nextInt(8) < 5) {
                player.levels.restore(Skill.Constitution, 30, 0.07)
            }
            if (random.nextInt(32) < 1) {
                val skill = Skill.all.filterNot { it == Skill.Constitution }.random()
                player.levels.drain(skill, multiplier = 0.05)
                player.message("That tasted very dodgy. You feel very ill.", ChatType.Filter)
                player.message("<red>Eating the kebab has done damage to some of your stats.")
            }
            cancel()
        }

        consume("ugthanki_kebab") { player ->
            if (player.levels.get(Skill.Constitution) != player.levels.getMax(Skill.Constitution)) {
                player.say(phrases.random())
            }
        }
    }
}
