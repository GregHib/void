package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.config.CombatDefinition
import world.gregs.voidps.engine.data.config.CombatDefinition.CombatHit
import world.gregs.voidps.engine.data.config.CombatDefinition.Projectile
import world.gregs.voidps.engine.data.config.CombatDefinition.ProjectileOrigin
import world.gregs.voidps.engine.timedLoad

/**
 * NPC Combat definitions
 */
class CombatDefinitions {

    private lateinit var definitions: Map<String, List<CombatDefinition>>

    fun get(key: String) = definitions[key] ?: emptyList()

    fun load(paths: List<String>): CombatDefinitions {
        timedLoad("combat definition") {
            val definitions = Object2ObjectOpenHashMap<String, MutableList<CombatDefinition>>()
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val (stringId, id) = section().split(".")
                        var chance = 0
                        var range = 1
                        var condition = ""

                        var style = ""

                        var anim = ""
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

                        val projectiles = mutableListOf<Projectile>()
                        val drainSkills = mutableListOf<CombatDefinition.Drain>()
                        val targetHits = mutableListOf<CombatHit>()
                        var freeze = 0
                        var poison = 0
                        var message = ""
                        while (nextPair()) {
                            when (val key = key()) {
                                "clone" -> throw UnsupportedOperationException("Clone not supported for combat definitions.")
                                // Selection
                                "chance" -> chance = int()
                                "range" -> range = int()
                                "condition" -> condition = string()
                                // Attacker
                                "anim" -> anim = string()
                                "style" -> style = string()
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
                                // Damage
                                "projectile" -> projectile(projectiles)
                                "projectiles" -> projectiles(projectiles)
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
                                "impact_freeze" -> freeze = int()
                                "impact_poison" -> poison = int()
                                "impact_message" -> message = string()
                                "miss_gfx" -> graphic(missGraphics)
                                "miss_gfxs" -> graphics(missGraphics)
                                "miss_sound" -> sound(missSounds)
                                "miss_sounds" -> sound(missSounds)
                                else -> throw UnsupportedOperationException("Unknown key '$key' in combat definition.")
                            }
                        }
                        definitions.getOrPut(stringId) { mutableListOf() }.add(
                            CombatDefinition(
                                npc = stringId,
                                id = id,
                                chance = chance,
                                range = range,
                                condition = condition,
                                anim = anim,
                                gfx = graphics,
                                sounds = sounds,
                                style = style,
                                projectiles = projectiles,
                                targetGfx = targetGraphics,
                                targetAnim = targetAnim,
                                targetSounds = targetSounds,
                                targetHits = targetHits,
                                impactAnim = impactAnim,
                                missGfx = missGraphics,
                                impactGfx = impactGraphics,
                                missSounds = missSounds,
                                impactSounds = impactSounds,
                                impactDrainSkills = drainSkills,
                                impactFreeze = freeze,
                                impactPoison = poison,
                                impactMessage = message,
                            )
                        )
                    }
                }
            }
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

    private fun ConfigReader.drains(drainSkills: MutableList<CombatDefinition.Drain>) {
        while (nextElement()) {
            drain(drainSkills)
        }
    }

    private fun ConfigReader.drain(drainSkills: MutableList<CombatDefinition.Drain>) {
        if (peek == '"') {
            throw IllegalArgumentException("Expected { skill = \"\", amount = 2 } found string literal '${string()}'. ${exception()}")
        }
        var skill = ""
        var amount = 0
        var multiplier = 0.0
        while (nextEntry()) {
            when (val key = key()) {
                "skill" -> skill = string()
                "amount" -> amount = int()
                "multiplier" -> multiplier = double()
                else -> throw IllegalArgumentException("Unknown key '$key' in drain definition. ${exception()}")
            }
        }
        drainSkills.add(CombatDefinition.Drain(skill, amount, multiplier))
    }

    private fun ConfigReader.projectiles(projectiles: MutableList<Projectile>) {
        while (nextElement()) {
            projectile(projectiles)
        }
    }

    private fun ConfigReader.projectile(list: MutableList<Projectile>) {
        if (peek == '"') {
            list.add(Projectile(string()))
            return
        }
        var id = ""
        var delay: Int? = null
        var curve: Int? = null
        var endHeight: Int? = null
        var origin: ProjectileOrigin = ProjectileOrigin.Entity
        while (nextEntry()) {
            when (val key = key()) {
                "id" -> id = string()
                "delay" -> delay = int()
                "curve" -> curve = int()
                "end_height" -> endHeight = int()
                "origin" -> origin = when(val key = string()) {
                    "entity" -> ProjectileOrigin.Entity
                    "tile" -> ProjectileOrigin.Tile
                    "centre" -> ProjectileOrigin.Centre
                    else -> throw IllegalArgumentException("Unknown projectile origin '$key'. ${exception()}")
                }
                else -> throw IllegalArgumentException("Unknown key '$key' in projectile definition. ${exception()}")
            }
        }
        list.add(Projectile(id = id, delay = delay, curve = curve, endHeight = endHeight, origin = origin))
    }

    private fun ConfigReader.hits(list: MutableList<CombatHit>) {
        while (nextElement()) {
            hit(list)
        }
    }

    private fun ConfigReader.hit(list: MutableList<CombatHit>) {
        if (peek == '"') {
            throw IllegalArgumentException("Expected { offense = \"\", max = 100 } found string literal '${string()}'. ${exception()}")
        }
        var offense = ""
        var defence: String? = null
        var spell = ""
        var special = false
        var min = 0
        var max = 0
        while (nextEntry()) {
            when (val key = key()) {
                "offense" -> offense = string()
                "defence" -> defence = string()
                "spell" -> spell = string()
                "special" -> special = boolean()
                "min" -> min = int()
                "max" -> max = int()
                else -> throw IllegalArgumentException("Unknown key '$key' in hit definition. ${exception()}")
            }
        }
        list.add(CombatHit(offense, defence ?: offense, spell, special, min, max))
    }

    private fun ConfigReader.sounds(list: MutableList<CombatDefinition.CombatSound>) {
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
        while (nextEntry()) {
            when (val key = key()) {
                "id" -> id = string()
                "delay" -> delay = int()
                "radius" -> radius = int()
                else -> throw IllegalArgumentException("Unknown key '$key' in sound definition. ${exception()}")
            }
        }
        list.add(CombatDefinition.CombatSound(id, delay, radius))
    }

    private fun ConfigReader.graphics(list: MutableList<CombatDefinition.CombatGfx>){
        while (nextElement()) {
            graphic(list)
        }
    }

    private fun ConfigReader.graphic(list: MutableList<CombatDefinition.CombatGfx>) {
        if (peek == '"') {
            list.add(CombatDefinition.CombatGfx(string()))
            return
        }
        var id = ""
        var delay: Int? = null
        var area = false
        while (nextEntry()) {
            when (val key = key()) {
                "id" -> id = string()
                "delay" -> delay = int()
                "area" -> area = boolean()
                else -> throw IllegalArgumentException("Unknown key '$key' in gfx definition. ${exception()}")
            }
        }
        list.add(CombatDefinition.CombatGfx(id, delay, area))
    }

}
