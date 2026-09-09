package content.skill.constitution.drink

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class PoisonChalice : Script {

    init {
        consumed("poison_chalice") { _, _ ->
            when (random.nextInt(7)) {
                0 -> message("It has a slight taste of apricot.", ChatType.Filter)
                1 -> {
                    val amount = random.nextInt(1, 5)
                    levels.drain(Skill.Attack, amount)
                    levels.drain(Skill.Strength, amount)
                    levels.drain(Skill.Defence, amount)
                    levels.boost(Skill.Crafting, 1)
                    message("You feel a little better.", ChatType.Filter)
                }
                2 -> {
                    levels.drain(Skill.Attack, 1)
                    levels.drain(Skill.Strength, 1)
                    levels.drain(Skill.Defence, 1)
                    levels.boost(Skill.Thieving, 1)
                    message("You feel a little strange.", ChatType.Filter)
                }
                3 -> {
                    levels.restore(Skill.Constitution, heal(this, 10, 0.07))
                    message("It heals some health.", ChatType.Filter)
                }
                4 -> {
                    levels.restore(Skill.Constitution, heal(this, 20, 0.14))
                    levels.boost(Skill.Thieving, 1)
                    message("You feel a lot better!", ChatType.Filter)
                }
                5 -> {
                    levels.boost(Skill.Attack, 4)
                    levels.boost(Skill.Strength, 4)
                    levels.boost(Skill.Defence, 4)
                    levels.boost(Skill.Thieving, 1)
                    message("Wow! That was amazing! You feel really invigorated.", ChatType.Filter)
                }
                else -> {
                    val amount = random.nextInt(1, 3)
                    levels.boost(Skill.Attack, amount)
                    levels.boost(Skill.Strength, amount)
                    levels.boost(Skill.Defence, amount)
                    message("That tasted a bit dodgy. You feel a bit ill.", ChatType.Filter)
                    directHit(random.nextInt(1, 50))
                }
            }
        }
    }

    /**
     * Scales with the drinker's maximum life points, which are ten times their level.
     */
    private fun heal(player: Player, base: Int, factor: Double): Int {
        val maximum = player.levels.getMax(Skill.Constitution)
        return base + ((maximum - 100) * factor).toInt()
    }
}
