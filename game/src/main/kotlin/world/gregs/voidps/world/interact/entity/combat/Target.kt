package world.gregs.voidps.world.interact.entity.combat

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.skill.slayer.race

object Target {
    fun attackable(source: Character, target: Character): Boolean {
        if (target is NPC) {
            if (target.def.options[1] != "Attack") {
                return false
            }
            if (get<NPCs>().indexed(target.index) == null) {
                return false
            }
        }
        if (source.tile.level != target.tile.level) {
            return false
        }
        if (source.dead || target.dead || source["logged_out", false] || target["logged_out", false]) {
            return false
        }
        if (source is Player && target is Player) {
            if (!source.inWilderness) {
                source.message("You can only attack players in a player-vs-player area.")
                return false
            }
            if (!target.inWilderness) {
                source.message("That player is not in the wilderness.")
                return false
            }
            val range = Wilderness.combatRange(source)
            if (target.combatLevel !in range) {
                source.message("Your level difference is too great!")
                source.message("You need to move deeper into the Wilderness.")
                return false
            }
        }
        if (target.inSingleCombat && target.underAttack && !target.attackers.contains(source)) {
            if (target is NPC) {
                (source as? Player)?.message("Someone else is fighting that.")
            } else {
                (source as? Player)?.message("That player is already under attack.")
            }
            return false
        }
        if (source.inSingleCombat && source.underAttack && !source.attackers.contains(target)) {
            (source as? Player)?.message("You are already in combat.")
            return false
        }
        // PVP area, slayer requirements, in combat etc..
        return true
    }

    fun isDemon(target: Character) = target is NPC && target.race == "demon"

    fun isVampyre(target: Character) = target is NPC && target.race == "vampyre"

    fun isShade(target: Character): Boolean = target is NPC && target.race == "shade"

    fun isKalphite(target: Character): Boolean = target is NPC && target.race == "kalphite"

    fun isDragon(target: Character): Boolean = target is NPC && target.race == "dragon"

    fun isMetalDragon(target: Character): Boolean = target is NPC && (target.id == "bronze_dragon" || target.id == "iron_dragon" || target.id == "steel_dragon")

    // TODO other staves and npcs
    fun isFirey(target: Character): Boolean {
        if (target is Player) {
            return target.equipped(EquipSlot.Weapon).id == "staff_of_fire"
        } else if (target is NPC) {
            return target.race == "dragon" || target.id.startsWith("fire_elemental") || target.id.startsWith("fire_giant") || target.id.startsWith("pyrefiend")
        }
        return false
    }

    /**
     * Damage caps which Guthans doesn't work on
     * E.g. Kurask & Turoth
     */
    fun damageReductionModifiers(source: Character, target: Character, damage: Int): Int {
        return damage
    }

    /**
     * Limits maximum amount of damage on NPCs (while still allowing Guthans to work)
     */
    fun damageLimitModifiers(target: Character, damage: Int): Int {
        return if (target is NPC && target.def.contains("damage_cap")) {
            damage.coerceAtMost(target.def["damage_cap"])
        } else if (target is NPC && (target.id == "magic_dummy" || target.id == "melee_dummy")) {
            damage.coerceAtMost(target.levels.get(Skill.Constitution) - 1)
        } else {
            damage
        }
    }
}

internal var Character.target: Character?
    get() = get("target")
    set(value) {
        if (value != null) {
            set("target", value)
        } else {
            clear("target")
        }
    }

val Character.underAttack: Boolean
    get() = hasClock("under_attack")

var Character.attackers: MutableList<Character>
    get() = getOrPut("attackers") { ObjectArrayList() }
    set(value) = set("attackers", value)

var Character.damageDealers: MutableMap<Character, Int>
    get() = getOrPut("damage_dealers") { Object2IntOpenHashMap() }
    set(value) = set("damage_dealers", value)

var Character.dead: Boolean
    get() = get("dead", false)
    set(value) {
        if (value) {
            set("dead", true)
        } else {
            clear("dead")
        }
    }