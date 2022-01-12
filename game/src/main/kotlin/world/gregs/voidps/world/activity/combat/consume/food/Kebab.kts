import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.forceChat
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume
import kotlin.random.Random

on<Consume>({ item.id == "kebab" }) { player: Player ->
    val random = Random.nextDouble(100.0)
    when {
        random < 66 -> {
            player.levels.restore(Skill.Constitution, multiplier = 0.10)
            player.message("It heals some health.", ChatType.GameFilter)
        }
        random < 87 -> {
            player.levels.restore(Skill.Constitution, amount = (100..200).random())
            player.message("That was a good kebab. You feel a lot better.", ChatType.GameFilter)
        }
        random < 96 -> player.message("That kebab didn't seem to do a lot.", ChatType.GameFilter)
        else -> {
            player.levels.restore(Skill.Constitution, amount = (100..300).random())
            player.levels.boost(Skill.Attack, (2..3).random())
            player.levels.boost(Skill.Strength, (2..3).random())
            player.levels.boost(Skill.Defence, (2..3).random())
            player.message("Wow, that was an amazing kebab! You feel really invigorated.", ChatType.GameFilter)
        }
    }
    cancel()
}

on<Consume>({ item.id == "super_kebab" }) { player: Player ->
    if (Random.nextInt(8) < 5) {
        player.levels.restore(Skill.Constitution, 30, 0.07)
    }
    if (Random.nextInt(32) < 1) {
        val skill = Skill.all.filterNot { it == Skill.Constitution }.random()
        player.levels.drain(skill, multiplier = 0.05)
        player.message("That tasted very dodgy. You feel very ill.", ChatType.GameFilter)
        player.message(Colour.Red { "world.gregs.voidps.world.activity.combat.consume.Eating the kebab has done damage to some of your stats." })
    }
    cancel()
}


val phrases = listOf("Lovely!", "Scrummy!", "Delicious!", "Yum!")

on<Consume>({ item.id == "ugthanki_kebab" }) { player: Player ->
    if (player.levels.get(Skill.Constitution) != player.levels.getMax(Skill.Constitution)) {
        player.forceChat = phrases.random()
    }
}
