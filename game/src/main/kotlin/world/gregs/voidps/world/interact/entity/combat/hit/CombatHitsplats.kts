package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.hit
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.Hitsplat
import world.gregs.voidps.world.interact.entity.combat.damageDealers
import kotlin.collections.set
import kotlin.math.floor

val definitions: SpellDefinitions by inject()

characterCombatHit { character ->
    if (damage < 0 || type == "magic" && definitions.get(spell).maxHit == -1 || type == "healed") {
        return@characterCombatHit
    }
    var damage = damage
    var soak = 0
    if (Settings["combat.damageSoak", true] && damage > 200) {
        val percent = character["absorb_$type", 10] / 100.0
        soak = floor((damage - 200) * percent).toInt()
        damage -= soak
    }
    if (Settings["combat.showSoak", true] || soak <= 0) {
        soak = -1
    }
    val dealers = character.damageDealers
    dealers[source] = dealers.getOrDefault(source, 0) + damage
    val maxHit = source["max_hit", 0]
    val mark = when (type) {
        "range" -> Hitsplat.Mark.Range
        "melee", "scorch" -> Hitsplat.Mark.Melee
        "magic", "blaze" -> Hitsplat.Mark.Magic
        "poison" -> Hitsplat.Mark.Poison
        "disease" -> Hitsplat.Mark.Diseased
        "dragonfire", "damage" -> Hitsplat.Mark.Regular
        "deflect" -> Hitsplat.Mark.Reflected
        "healed" -> Hitsplat.Mark.Healed
        else -> Hitsplat.Mark.Missed
    }
    val critical = mark.id < 3 && damage > 10 && maxHit > 0 && damage > (maxHit * 0.9)
    character.hit(
        source = source,
        amount = damage,
        mark = mark,
        critical = critical,
        soak = soak
    )
    character.levels.drain(Skill.Constitution, damage)
}

characterCombatHit { character ->
    if (damage < 0) {
        character.hit(
            source = source,
            amount = 0,
            mark = Hitsplat.Mark.Missed
        )
    } else if (type == "healed") {
        character.hit(
            source = source,
            amount = damage,
            mark = Hitsplat.Mark.Healed
        )
        character.levels.restore(Skill.Constitution, damage)
    }
}