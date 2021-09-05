package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.update.task.viewport.Spiral
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.algorithm.BresenhamsLine
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import kotlin.random.Random

fun isKorasisSword(item: Item?) = item != null && item.name == "korasis_sword"

on<Registered>({ isKorasisSword(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isKorasisSword(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 1
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && isKorasisSword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("korasis_sword_${
        when (player.attackType) {
            "chop" -> "chop"
            else -> "slash"
        }
    }")
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isKorasisSword(weapon) }) { player: Player ->
    player.setAnimation("korasis_sword_block")
}

// Special attack

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: BresenhamsLine by inject()

on<HitChanceModifier>({ type == "magic" && special && isKorasisSword(weapon) }, Priority.HIGHEST) { _: Player ->
    chance = 1.0
}

on<CombatSwing>({ !swung() && it.specialAttack && isKorasisSword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, 600)) {
        delay = -1
        return@on
    }
    player["korasi_chain"] = mutableSetOf(target.index)
    player.setAnimation("disrupt")
    player.setGraphic("disrupt")
    val maxHit = getMaximumHit(player, target, "melee", player.weapon, special = true)
    val hit = Random.nextInt(maxHit / 2, (maxHit * 1.5).toInt())
    player.hit(target, hit, type = "magic", delay = 0)
    delay = 5
}

on<CombatHit>({ special && isKorasisSword(weapon) }) { character: Character ->
    character.setGraphic("disrupt_hit")
}

on<CombatDamage>({ special && isKorasisSword(weapon) && target.inMultiCombat }) { player: Player ->
    val chain: MutableSet<Int> = player["korasi_chain"]
    if (chain.size >= 3) {
        return@on
    }
    Spiral.spiral(target.tile, 4) { tile ->
        (if (target is Player) players[tile] else npcs[tile])?.forEach { character ->
            if (character == null || character == target || chain.contains(character.index) || !canAttack(player, character)) {
                return@forEach
            }
            if (!lineOfSight.withinSight(target.tile, character.tile)) {
                return@forEach
            }
            chain.add(character.index)
            val hit = damage / when (chain.size) {
                2 -> 2
                3 -> 4
                else -> return@on
            }
            player.hit(character, hit, weapon, type, special = true)
            return@on
        }
    }
}