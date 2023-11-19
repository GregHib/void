package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.hit
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.Hit
import kotlin.collections.set
import kotlin.math.floor

val definitions: SpellDefinitions by inject()

on<CombatHit>({ damage >= 0 && !(type == "magic" && definitions.get(spell).maxHit == -1) && type != "healed" }) { character: Character ->
    var damage = damage
    var soak = 0
    if (damage > 200) {
        val percent = character["absorb_$type", 0] / 100.0
        soak = floor((damage - 200) * percent).toInt()
        damage -= soak
    }
    if (soak <= 0) {
        soak = -1
    }
    val dealers = character.damageDealers
    dealers[source] = dealers.getOrDefault(source, 0) + damage
    character.hit(
        source = source,
        amount = damage,
        mark = when (type) {
            "range" -> Hit.Mark.Range
            "melee" -> Hit.Mark.Melee
            "magic" -> Hit.Mark.Magic
            "poison" -> Hit.Mark.Poison
            "disease" -> Hit.Mark.Diseased
            "dragonfire", "damage" -> Hit.Mark.Regular
            "deflect" -> Hit.Mark.Reflected
            "healed" -> Hit.Mark.Healed
            else -> Hit.Mark.Missed
        },
        critical = (type == "melee" || type == "magic" || type == "range") && damage > 10 && damage > (source["max_hit", 0] * 0.9),
        soak = soak
    )
    character.levels.drain(Skill.Constitution, damage)
}

on<CombatHit>({ damage >= 0 && type == "healed" }) { character: Character ->
    character.hit(
        source = source,
        amount = damage,
        mark = Hit.Mark.Healed
    )
    character.levels.restore(Skill.Constitution, damage)
}

on<CombatHit>({ damage < 0 }) { character: Character ->
    character.hit(
        source = source,
        amount = 0,
        mark = Hit.Mark.Missed
    )
}