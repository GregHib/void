import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.hit
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import kotlin.collections.set
import kotlin.math.floor

val definitions: SpellDefinitions by inject()

on<CombatHit>({ damage >= 0 && !(type == "magic" && definitions.get(spell).maxHit == -1) }) { character: Character ->
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
    val dealers = character.get<MutableMap<Character, Int>>("damage_dealers")
    dealers[source] = dealers.getOrDefault(source, 0) + damage
    character.hit(
        source = source,
        amount = damage,
        mark = when (type) {
            "range" -> Hit.Mark.Range
            "melee" -> Hit.Mark.Melee
            "magic" -> Hit.Mark.Magic
            "poison" -> Hit.Mark.Poison
            "dragonfire", "damage" -> Hit.Mark.Regular
            "deflect" -> Hit.Mark.Reflected
            else -> Hit.Mark.Missed
        },
        critical = (type == "melee" || type == "magic" || type == "range") && damage > 10 && damage > (source["max_hit", 0] * 0.9),
        soak = soak
    )
    character.levels.drain(Skill.Constitution, damage)
}

on<CombatHit>({ damage < 0 }) { character: Character ->
    character.hit(
        source = source,
        amount = 0,
        mark = Hit.Mark.Missed
    )
}