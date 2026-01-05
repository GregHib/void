package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.config.CombatDefinition
import world.gregs.voidps.engine.data.config.CombatDefinition.CombatAttack
import world.gregs.voidps.engine.data.config.CombatDefinition.CombatHit
import world.gregs.voidps.engine.data.config.CombatDefinition.Projectile
import world.gregs.voidps.engine.data.config.CombatDefinition.Origin
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Delta

/**
 * NPC Combat definitions
 */
class CombatDefinitions {

    private lateinit var definitions: Map<String, CombatDefinition>

    fun get(key: String) = definitions[key] ?: CombatDefinition()

    fun getOrNull(key: String) = definitions[key]

    fun load(paths: List<String>): CombatDefinitions {
        timedLoad("combat definition") {
            val definitions = Object2ObjectOpenHashMap<String, CombatDefinition>()
            val attackClones = mutableListOf<Triple<CombatDefinition, String, String>>()
            val definitionClones = mutableListOf<Pair<String, String>>()
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val section = section()
                        if (section.contains(".")) {
                            attack(section, definitions, attackClones)
                        } else {
                            definition(definitions, section, definitionClones)
                        }
                    }
                }
            }
            for ((definition, attack, clone) in attackClones) {
                val original = definition.attacks[attack]!!
                val (id, att) = clone.split(".")
                val clone = definitions[id]?.attacks?.get(att) ?: throw IllegalArgumentException("Unable to find combat definition attack '$clone' to clone for '${definition.npc}.${attack}'.")
                (definition.attacks as MutableMap<String, CombatAttack>)[attack] = override(original, clone)
            }
            for ((name, clone) in definitionClones) {
                val original = definitions[name]!!
                val clone = definitions[clone] ?: throw IllegalArgumentException("Unable to find combat definition '$clone' to clone for '${original.npc}'.")
                definitions[name] = override(original, clone)
            }
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

    private fun ConfigReader.definition(definitions: Object2ObjectOpenHashMap<String, CombatDefinition>, section: String, clones: MutableList<Pair<String, String>>) {
        check(!definitions.containsKey(section)) { "Definition $section already exists. Make sure [npc_name] comes before [npc_name.attacks]." }
        var attackSpeed = 4
        var attackRange = 1
        var retreatRange = 8
        var defendAnim = ""
        var deathAnim = ""
        var defendSound: CombatDefinition.CombatSound? = null
        var deathSound: CombatDefinition.CombatSound? = null
        while (nextPair()) {
            when (val key = key()) {
                "attack_speed" -> attackSpeed = int()
                "attack_range" -> attackRange = int()
                "retreat_range" -> retreatRange = int()
                "defend_anim" -> defendAnim = string()
                "death_anim" -> deathAnim = string()
                "death_sound" -> deathSound = CombatDefinition.CombatSound(string())
                "defend_sound" -> defendSound = CombatDefinition.CombatSound(string())
                "clone" -> clones.add(Pair(section, string()))
                else -> throw UnsupportedOperationException("Unknown key '$key' in combat definition. ${exception()}")
            }
        }
        definitions[section] = CombatDefinition(
            npc = section,
            attackSpeed = attackSpeed,
            attackRange = attackRange,
            retreatRange = retreatRange,
            defendAnim = defendAnim,
            defendSound = defendSound,
            deathAnim = deathAnim,
            deathSound = deathSound,
        )
    }

    private fun ConfigReader.attack(section: String, definitions: MutableMap<String, CombatDefinition>, clones: MutableList<Triple<CombatDefinition, String, String>>) {
        val (stringId, id) = section.split(".")
        var chance = 1
        var range = 1
        var condition = ""

        var anim = ""
        var say = ""
        var targetAnim = ""
        var impactAnim = ""

        val sounds = mutableListOf<CombatDefinition.CombatSound>()
        val targetSounds = mutableListOf<CombatDefinition.CombatSound>()
        val impactSounds = mutableListOf<CombatDefinition.CombatSound>()
        val missSounds = mutableListOf<CombatDefinition.CombatSound>()

        val graphics = mutableListOf<CombatDefinition.CombatGfx>()
        val targetGraphics = mutableListOf<CombatDefinition.CombatGfx>()
        val impactGraphics = mutableListOf<CombatDefinition.CombatGfx>()
        val missGraphics = mutableListOf<CombatDefinition.CombatGfx>()

        var origin: Origin = Origin.Entity
        val projectiles = mutableListOf<Projectile>()
        val drainSkills = mutableListOf<CombatDefinition.Drain>()
        val targetHits = mutableListOf<CombatHit>()
        var targetArea = ""
        var impactRegardless = false
        var freeze = 0
        var poison = 0
        var message = ""
        val definition = definitions.getOrPut(stringId) { CombatDefinition(npc = stringId) }
        val attacks = definition.attacks as MutableMap<String, CombatAttack>
        while (nextPair()) {
            when (val key = key()) {
                "clone" -> {
                    var clone = string()
                    if (!clone.contains(".")) {
                        clone = "$stringId.$clone"
                    }
                    clones.add(Triple(definition, id, clone))
                }
                // Selection
                "chance" -> chance = int()
                "range" -> range = int()
                "condition" -> condition = string()
                // Attacker
                "say" -> say = string()
                "anim" -> anim = string()
                "gfx" -> graphic(graphics)
                "gfxs" -> graphics(graphics)
                "sound" -> sound(sounds)
                "sounds" -> sounds(sounds)
                // Target
                "target_anim" -> targetAnim = string()
                "target_gfx" -> graphic(targetGraphics)
                "target_gfxs" -> graphics(targetGraphics)
                "target_sound" -> sound(targetSounds)
                "target_sounds" -> sounds(targetSounds)
                "multi_target_area" -> targetArea = string()
                // Damage
                "projectile" -> projectile(projectiles)
                "projectiles" -> projectiles(projectiles)
                "projectile_origin" -> origin = when (val key = string()) {
                    "entity" -> Origin.Entity
                    "tile" -> Origin.Tile
                    "centre" -> Origin.Centre
                    else -> throw IllegalArgumentException("Unknown projectile origin '$key'. ${exception()}")
                }
                "target_hit" -> hit(targetHits)
                "target_hits" -> hits(targetHits)
                // Impact
                "impact_anim" -> impactAnim = string()
                "impact_gfx" -> graphic(impactGraphics)
                "impact_gfxs" -> graphics(impactGraphics)
                "impact_sound" -> sound(impactSounds)
                "impact_sounds" -> sounds(impactSounds)
                "impact_drain" -> drain(drainSkills)
                "impact_drains" -> drains(drainSkills)
                "impact_regardless" -> impactRegardless = boolean()
                "impact_freeze" -> freeze = int()
                "impact_poison" -> poison = int()
                "impact_message" -> message = string()
                "miss_gfx" -> graphic(missGraphics)
                "miss_gfxs" -> graphics(missGraphics)
                "miss_sound" -> sound(missSounds)
                "miss_sounds" -> sounds(missSounds)
                else -> throw UnsupportedOperationException("Unknown key '$key' in combat definition. ${exception()}")
            }
        }
        attacks[id] = CombatDefinition.CombatAttack(
            id = id,
            chance = chance,
            range = range,
            condition = condition,
            say = say,
            anim = anim,
            gfx = graphics,
            sounds = sounds,
            projectileOrigin = origin,
            projectiles = projectiles,
            targetGfx = targetGraphics,
            targetAnim = targetAnim,
            targetSounds = targetSounds,
            targetHits = targetHits,
            multiTargetArea = targetArea,
            impactAnim = impactAnim,
            missGfx = missGraphics,
            impactGfx = impactGraphics,
            missSounds = missSounds,
            impactSounds = impactSounds,
            impactRegardless = impactRegardless,
            impactDrainSkills = drainSkills,
            impactFreeze = freeze,
            impactPoison = poison,
            impactMessage = message,
        )
    }

    private fun ConfigReader.drains(list: MutableList<CombatDefinition.Drain>) {
        if (peek != '[') {
            throw IllegalArgumentException("List expected but found literal '${peek}'. ${exception()}")
        }
        list.clear()
        while (nextElement()) {
            drain(list)
        }
    }

    private fun ConfigReader.drain(drainSkills: MutableList<CombatDefinition.Drain>) {
        if (peek != '{') {
            throw IllegalArgumentException("Expected { skill = \"\", amount = 2 } found literal '${peek}'. ${exception()}")
        }
        var skill = ""
        var min = 0
        var max = 0
        var multiplier = 0.0
        while (nextEntry()) {
            when (val key = key()) {
                "skill" -> skill = string()
                "amount" -> {
                    min = int()
                    max = min
                }
                "min" -> min = int()
                "max" -> max = int()
                "multiplier" -> multiplier = double()
                else -> throw IllegalArgumentException("Unknown key '$key' in drain definition. ${exception()}")
            }
        }
        drainSkills.add(CombatDefinition.Drain(skill, min, max, multiplier))
    }

    private fun ConfigReader.projectiles(list: MutableList<Projectile>) {
        if (peek != '[') {
            throw IllegalArgumentException("List expected but found literal '${peek}'. ${exception()}")
        }
        list.clear()
        while (nextElement()) {
            projectile(list)
        }
    }

    private fun ConfigReader.projectile(list: MutableList<Projectile>) {
        if (peek == '"') {
            list.add(Projectile(string()))
            return
        } else if (peek != '{') {
            throw IllegalArgumentException("Map expected but found literal '${peek}'. ${exception()}")
        }
        var id = ""
        var delay: Int? = null
        var curveMin: Int? = null
        var curveMax: Int? = null
        var endHeight: Int? = null
        while (nextEntry()) {
            when (val key = key()) {
                "id" -> id = string()
                "delay" -> delay = int()
                "curve" -> {
                    curveMin = int()
                    curveMax = curveMin
                }
                "curve_min" -> curveMin = int()
                "curve_max" -> curveMax = int()
                "end_height" -> endHeight = int()
                else -> throw IllegalArgumentException("Unknown key '$key' in projectile definition. ${exception()}")
            }
        }
        val curve = if (curveMin != null && curveMax != null) curveMin..curveMax else null
        list.add(Projectile(id = id, delay = delay, curve = curve, endHeight = endHeight))
    }

    private fun ConfigReader.hits(list: MutableList<CombatHit>) {
        if (peek != '[') {
            throw IllegalArgumentException("List expected but found literal '${peek}'. ${exception()}")
        }
        list.clear()
        while (nextElement()) {
            hit(list)
        }
    }

    private fun ConfigReader.hit(list: MutableList<CombatHit>) {
        if (peek != '{') {
            throw IllegalArgumentException("Expected { offense = \"\", max = 100 } found literal '${peek}'. ${exception()}")
        }
        var offense = ""
        var defence: String? = null
        var special = false
        var min = 0
        var max = 0
        var delay: Int? = null
        while (nextEntry()) {
            when (val key = key()) {
                "offense" -> {
                    offense = string()
                    require(offense != "ranged") { "Invalid offensive type 'ranged' only 'range' is valid. ${exception()}" }
                    require(offense != "mage") { "Invalid offensive type 'mage' only 'magic' is valid. ${exception()}" }
                }
                "defence" -> {
                    defence = string()
                    require(defence != "ranged") { "Invalid defensive type 'ranged' only 'range' is valid. ${exception()}" }
                    require(defence != "mage") { "Invalid defensive type 'mage' only 'magic' is valid. ${exception()}" }
                }
                "special" -> special = boolean()
                "min" -> min = int()
                "max" -> max = int()
                "delay" -> delay = int()
                else -> throw IllegalArgumentException("Unknown key '$key' in hit definition. ${exception()}")
            }
        }
        list.add(CombatHit(offense, defence ?: offense, special, min, max, delay))
    }

    private fun ConfigReader.sounds(list: MutableList<CombatDefinition.CombatSound>) {
        if (peek != '[') {
            throw IllegalArgumentException("List expected but found literal '${peek}'. ${exception()}")
        }
        list.clear()
        while (nextElement()) {
            sound(list)
        }
    }

    private fun ConfigReader.sound(list: MutableList<CombatDefinition.CombatSound>) {
        if (peek == '"') {
            list.add(CombatDefinition.CombatSound(string()))
            return
        } else if (peek != '{') {
            throw IllegalArgumentException("Map expected but found literal '${peek}'. ${exception()}")
        }
        var id = ""
        var delay = 0
        var radius = 0
        var offset: Delta? = null
        while (nextEntry()) {
            when (val key = key()) {
                "id" -> id = string()
                "delay" -> delay = int()
                "radius" -> radius = int()
                "offset_x" -> {
                    val x = int()
                    offset = offset?.copy(x = x) ?: Delta(x = x, y = 0, level = 0)
                }
                "offset_y" -> {
                    val y = int()
                    offset = offset?.copy(y = y) ?: Delta(x = 0, y = y, level = 0)
                }
                else -> throw IllegalArgumentException("Unknown key '$key' in sound definition. ${exception()}")
            }
        }
        list.add(CombatDefinition.CombatSound(id, delay, radius, offset))
    }

    private fun ConfigReader.graphics(list: MutableList<CombatDefinition.CombatGfx>) {
        if (peek != '[') {
            throw IllegalArgumentException("List expected but found literal '${peek}'. ${exception()}")
        }
        list.clear()
        while (nextElement()) {
            graphic(list)
        }
    }

    private fun ConfigReader.graphic(list: MutableList<CombatDefinition.CombatGfx>) {
        if (peek == '"') {
            list.add(CombatDefinition.CombatGfx(string()))
            return
        } else if (peek != '{') {
            throw IllegalArgumentException("Map expected but found literal '${peek}'. ${exception()}")
        }
        var id = ""
        var delay: Int? = null
        var height: Int? = null
        var area = false
        var offset: Delta? = null
        while (nextEntry()) {
            when (val key = key()) {
                "id" -> id = string()
                "delay" -> delay = int()
                "height" -> height = int()
                "area" -> area = boolean()
                "offset_x" -> {
                    val x = int()
                    offset = offset?.copy(x = x) ?: Delta(x = x, y = 0, level = 0)
                }
                "offset_y" -> {
                    val y = int()
                    offset = offset?.copy(y = y) ?: Delta(x = 0, y = y, level = 0)
                }
                else -> throw IllegalArgumentException("Unknown key '$key' in gfx definition. ${exception()}")
            }
        }
        list.add(CombatDefinition.CombatGfx(id, delay, height, area, offset))
    }

    private fun override(original: CombatAttack, clone: CombatAttack) = original.copy(
        chance = if (clone.chance != 0) clone.chance else original.chance,
        range = if (clone.range != 1) clone.range else original.range,
        condition = if (clone.condition != "") clone.condition else original.condition,
        say = if (clone.say != "") clone.say else original.say,
        anim = if (clone.anim != "") clone.anim else original.anim,
        gfx = clone.gfx.ifEmpty { original.gfx },
        sounds = clone.sounds.ifEmpty { original.sounds },
        projectileOrigin = if (clone.projectileOrigin != Origin.Entity) clone.projectileOrigin else original.projectileOrigin,
        projectiles = clone.projectiles.ifEmpty { original.projectiles },
        targetAnim = if (clone.targetAnim != "") clone.targetAnim else original.targetAnim,
        targetGfx = clone.targetGfx.ifEmpty { original.targetGfx },
        targetSounds = clone.targetSounds.ifEmpty { original.targetSounds },
        targetHits = clone.targetHits.ifEmpty { original.targetHits },
        multiTargetArea = if (clone.multiTargetArea != "") clone.multiTargetArea else original.multiTargetArea,
        impactAnim = if (clone.impactAnim != "") clone.impactAnim else original.impactAnim,
        impactGfx = clone.impactGfx.ifEmpty { original.impactGfx },
        impactSounds = clone.impactSounds.ifEmpty { original.impactSounds },
        missGfx = clone.missGfx.ifEmpty { original.missGfx },
        missSounds = clone.missSounds.ifEmpty { original.missSounds },
        impactDrainSkills = clone.impactDrainSkills.ifEmpty { original.impactDrainSkills },
        impactRegardless = if (clone.impactRegardless) true else original.impactRegardless,
        impactFreeze = if (clone.impactFreeze != 0) clone.impactFreeze else original.impactFreeze,
        impactPoison = if (clone.impactPoison != 0) clone.impactPoison else original.impactPoison,
        impactMessage = if (clone.impactMessage != "") clone.impactMessage else original.impactMessage,
    )

    private fun override(original: CombatDefinition, clone: CombatDefinition) = original.copy(
        attackSpeed = if (clone.attackSpeed != 4) clone.attackSpeed else original.attackSpeed,
        attackRange = if (clone.attackRange != 1) clone.attackRange else original.attackRange,
        retreatRange = if (clone.retreatRange != 8) clone.retreatRange else original.retreatRange,
        defendAnim = if (clone.defendAnim != "") clone.defendAnim else original.defendAnim,
        deathAnim = if (clone.deathAnim != "") clone.deathAnim else original.deathAnim,
        defendSound = clone.defendSound ?: original.defendSound,
        deathSound = clone.deathSound ?: original.deathSound,
    )

}
