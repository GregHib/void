package world.gregs.voidps.world.interact.entity.combat

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile

val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") ?: ShootProjectile.DEFAULT_HEIGHT

fun Character.hit(
    target: Character,
    weapon: Item = this.weapon,
    type: String = Weapon.type(this, weapon),
    delay: Int = if (type == "melee") 0 else 2,
    spell: String = this.spell,
    special: Boolean = (this as? Player)?.specialAttack ?: false,
    damage: Int = Damage.roll(this, target, type, weapon, spell)
): Int {
    val damage = damage.coerceAtMost(target.levels.get(Skill.Constitution))
    events.emit(CombatAttack(target, type, damage, weapon, spell, special, TICKS.toClientTicks(delay)))
    val delay = delay
    if (target is Player) {
        target.strongQueue("hit", delay) {
            splat(this@hit, target, damage, type, weapon, spell, special)
        }
    } else if (target is NPC) {
        target.strongQueue("hit", delay) {
            splat(this@hit, target, damage, type, weapon, spell, special)
        }
    }
    return damage
}

fun Character.damage(damage: Int, delay: Int = 0, type: String = "damage") {
    if (this is Player) {
        strongQueue("hit", delay) {
            splat(source = this@damage, target = this@damage, damage, type)
        }
    } else if (this is NPC) {
        strongQueue("hit", delay) {
            splat(source = this@damage, target = this@damage, damage, type)
        }
    }
}

fun splat(source: Character, target: Character, damage: Int, type: String = "damage", weapon: Item? = null, spell: String = "", special: Boolean = false) {
    if (source.dead) {
        return
    }
    target.events.emit(CombatHit(source, type, damage, weapon, spell, special))
}

var Character.attackers: MutableList<Character>
    get() = getOrPut("attackers") { ObjectArrayList() }
    set(value) = set("attackers", value)

var Character.damageDealers: MutableMap<Character, Int>
    get() = getOrPut("damage_dealers") { Object2IntOpenHashMap() }
    set(value) = set("damage_dealers", value)

// E.g "accurate"
val Character.attackStyle: String
    get() = get("attack_style", "")

// E.g "flick"
val Character.attackType: String
    get() = get("attack_type", "")

// E.g "crush"
val Character.combatStyle: String
    get() = get("combat_style", "")

var Character.spell: String
    get() = get("spell", get("autocast_spell", ""))
    set(value) = set("spell", value)

var Character.dead: Boolean
    get() = get("dead", false)
    set(value) {
        if (value) {
            set("dead", true)
        } else {
            clear("dead")
        }
    }

var Character.attackRange: Int
    get() = get("attack_range", if (this is NPC) def["attack_range", 1] else 1)
    set(value) = set("attack_range", value)

val Player.spellBook: String
    get() = interfaces.get("spellbook_tab") ?: "unknown_spellbook"