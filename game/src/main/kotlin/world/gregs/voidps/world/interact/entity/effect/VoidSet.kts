import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelModifier
import kotlin.math.floor

on<ItemChanged>({ container == "worn_equipment" && isSetSlot(index) && it.hasEffect("void_set") && !isVoid(item) }) { player: Player ->
    player.stop("void_set")
}

on<ItemChanged>({ container == "worn_equipment" && isSetSlot(index) && !it.hasEffect("void_set") && isVoid(item) && it.hasFullSet("") }) { player: Player ->
    player.start("void_set")
}

on<ItemChanged>({ container == "worn_equipment" && isSetSlot(index) && it.hasEffect("elite_void_set") && !isEliteVoid(item) }) { player: Player ->
    player.stop("elite_void_set")
}

on<ItemChanged>({ container == "worn_equipment" && isSetSlot(index) && !it.hasEffect("elite_void_set") && isEliteVoid(item) && it.hasFullSet("elite_") }) { player: Player ->
    player.start("elite_void_set")
}

fun isVoid(item: Item) = item.name.startsWith("void_")

fun isEliteVoid(item: Item) = item.name.startsWith("elite_void_") || item.name == "void_knight_gloves" || isHelm(item)

fun isSetSlot(index: Int) = index == EquipSlot.Hat.index || index == EquipSlot.Chest.index || index == EquipSlot.Legs.index || index == EquipSlot.Hands.index

fun Player.hasFullSet(prefix: String): Boolean {
    return equipped(EquipSlot.Chest).name.startsWith("${prefix}void_knight_top") &&
            equipped(EquipSlot.Legs).name.startsWith("${prefix}void_knight_robe") &&
            equipped(EquipSlot.Hands).name.startsWith("void_knight_gloves") &&
            isHelm(equipped(EquipSlot.Hat))
}

fun isHelm(item: Item): Boolean = when (item.name) {
    "void_ranger_helm", "void_melee_helm", "void_mage_helm" -> true
    else -> false
}

on<HitEffectiveLevelModifier>({ it.hasEffect("void_set") || it.hasEffect("elite_void_set") }, priority = Priority.LOW) { player: Player ->
    val mage = accuracy && skill == Skill.Magic && player.equipped(EquipSlot.Hat).name == "void_mage_helm"
    level = floor(level * if (mage) 1.3 else 1.1)
}