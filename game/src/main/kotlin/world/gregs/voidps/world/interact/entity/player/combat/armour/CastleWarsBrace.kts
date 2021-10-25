import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

val areas: Areas by inject()
val area = areas.getValue("castle_wars").area

fun isFlagHolder(target: Character?): Boolean = target is Player && (target.equipped(EquipSlot.Weapon).id == "zamorak_flag" || target.equipped(EquipSlot.Weapon).id == "saradomin_flag")

on<HitDamageModifier>(
    { player -> player.tile in area && player.equipped(EquipSlot.Hands).id.startsWith("castle_wars_brace") && isFlagHolder(target) },
    priority = Priority.LOWER
) { _: Player ->
    damage = floor(damage * 1.2)
}