package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.config.CombatDefinition
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
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val section = section()
                        if (section.contains(".")) {
                            attack(section, definitions)
                        } else {
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
                                    "clone" -> {
                                        val name = string()
                                        val clone = definitions[name]
                                        require(clone != null) { "Unable to find combat definition '$name' to clone. ${exception()}" }
                                        if (clone.attackSpeed != 4) attackSpeed = clone.attackSpeed
                                        if (clone.attackRange != 1) attackRange = clone.attackRange
                                        if (clone.retreatRange != 8) retreatRange = clone.retreatRange
                                        if (clone.defendAnim != "") defendAnim = clone.defendAnim
                                        if (clone.deathAnim != "") deathAnim = clone.deathAnim
                                        if (clone.defendSound != null) defendSound = clone.defendSound
                                        if (clone.deathSound != null) deathSound = clone.deathSound
                                    }
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
                    }
                }
            }
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

    private fun ConfigReader.attack(section: String, definitions: MutableMap<String, CombatDefinition>) {
        val (stringId, id) = section.split(".")
        var chance = 0
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
        var targetMultiple = false
        var impactRegardless = false
        var freeze = 0
        var poison = 0
        var message = ""
        val definition = definitions.getOrPut(stringId) { CombatDefinition(npc = stringId) }
        val attacks = definition.attacks as MutableMap<String, CombatDefinition.CombatAttack>
        while (nextPair()) {
            when (val key = key()) {
                "clone" -> {
                    val name = string()
                    val clone = if (name.contains(".")) {
                        val (id, att) = name.split(".")
                        definitions[id]?.attacks?.get(att)
                    } else {
                        attacks[name]
                    }
                    require(clone != null) { "Unable to find attack definition '$name' to clone from npc '$stringId'. ${exception()}" }
                    if (clone.chance != 0) chance = clone.chance
                    if (clone.range != 1) range = clone.range
                    if (clone.condition != "") condition = clone.condition
                    if (clone.say != "") anim = clone.say
                    if (clone.anim != "") anim = clone.anim
                    if (clone.gfx.isNotEmpty()) graphics.addAll(clone.gfx)
                    if (clone.sounds.isNotEmpty()) sounds.addAll(clone.sounds)
                    if (clone.projectileOrigin != Origin.Entity) origin = clone.projectileOrigin
                    if (clone.projectiles.isNotEmpty()) projectiles.addAll(clone.projectiles)
                    if (clone.targetAnim != "") targetAnim = clone.targetAnim
                    if (clone.targetGfx.isNotEmpty()) targetGraphics.addAll(clone.targetGfx)
                    if (clone.targetSounds.isNotEmpty()) targetSounds.addAll(clone.targetSounds)
                    if (clone.targetHits.isNotEmpty()) targetHits.addAll(clone.targetHits)
                    if (clone.targetMultiple) targetMultiple = clone.targetMultiple
                    if (clone.impactAnim != "") impactAnim = clone.impactAnim
                    if (clone.impactGfx.isNotEmpty()) impactGraphics.addAll(clone.impactGfx)
                    if (clone.impactSounds.isNotEmpty()) impactSounds.addAll(clone.impactSounds)
                    if (clone.missGfx.isNotEmpty()) missGraphics.addAll(clone.missGfx)
                    if (clone.missSounds.isNotEmpty()) missSounds.addAll(clone.missSounds)
                    if (clone.impactDrainSkills.isNotEmpty()) drainSkills.addAll(clone.impactDrainSkills)
                    if (clone.impactRegardless) impactRegardless = clone.impactRegardless
                    if (clone.impactFreeze != 0) freeze = clone.impactFreeze
                    if (clone.impactPoison != 0) poison = clone.impactPoison
                    if (clone.impactMessage != "") message = clone.impactMessage
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
                "target_multiple" -> targetMultiple = boolean()
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
                "impact_sounds" -> sound(impactSounds)
                "impact_drain" -> drain(drainSkills)
                "impact_drains" -> drains(drainSkills)
                "impact_regardless" -> impactRegardless = boolean()
                "impact_freeze" -> freeze = int()
                "impact_poison" -> poison = int()
                "impact_message" -> message = string()
                "miss_gfx" -> graphic(missGraphics)
                "miss_gfxs" -> graphics(missGraphics)
                "miss_sound" -> sound(missSounds)
                "miss_sounds" -> sound(missSounds)
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
            targetMultiple = targetMultiple,
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
        while (nextEntry()) {
            when (val key = key()) {
                "offense" -> {
                    offense = string()
                    require(offense != "ranged") { "Invalid offensive type 'ranged' only 'range' is valid. ${exception()}"}
                    require(offense != "mage") { "Invalid offensive type 'mage' only 'magic' is valid. ${exception()}"}
                }
                "defence" -> {
                    defence = string()
                    require(defence != "ranged") { "Invalid defensive type 'ranged' only 'range' is valid. ${exception()}"}
                    require(defence != "mage") { "Invalid defensive type 'mage' only 'magic' is valid. ${exception()}"}
                }
                "special" -> special = boolean()
                "min" -> min = int()
                "max" -> max = int()
                else -> throw IllegalArgumentException("Unknown key '$key' in hit definition. ${exception()}")
            }
        }
        list.add(CombatHit(offense, defence ?: offense, special, min, max))
    }

    private fun ConfigReader.sounds(list: MutableList<CombatDefinition.CombatSound>) {
        list.clear()
        while (nextElement()) {
            sound(list)
        }
    }

    private fun ConfigReader.sound(list: MutableList<CombatDefinition.CombatSound>) {
        if (peek == '"') {
            list.add(CombatDefinition.CombatSound(string()))
            return
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
        list.clear()
        while (nextElement()) {
            graphic(list)
        }
    }

    private fun ConfigReader.graphic(list: MutableList<CombatDefinition.CombatGfx>) {
        if (peek == '"') {
            list.add(CombatDefinition.CombatGfx(string()))
            return
        } else if (peek != '[') {
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

}
