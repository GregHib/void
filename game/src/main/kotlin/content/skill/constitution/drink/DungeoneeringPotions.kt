package content.skill.constitution.drink

import content.entity.effect.toxin.antiPoison
import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class DungeoneeringPotions : Script {

    init {
        consumed("weak_melee_potion") { _, _ ->
            levels.boost(Skill.Attack, 2, 0.07)
            levels.boost(Skill.Strength, 2, 0.07)
        }

        consumed("weak_magic_potion") { _, _ ->
            levels.boost(Skill.Magic, 2, 0.07)
        }

        consumed("weak_range_potion") { _, _ ->
            levels.boost(Skill.Ranged, 2, 0.07)
        }

        consumed("weak_defence_potion") { _, _ ->
            levels.boost(Skill.Defence, 2, 0.07)
        }

        consumed("weak_stat_restore_potion") { _, _ ->
            Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
                levels.restore(skill, 5, 0.12)
            }
        }

        consumed("antipoison_dungeoneering") { _, _ ->
            antiPoison(1)
        }

        consumed("weak_cure_potion") { _, _ ->
            antiPoison(5)
            antifire(5)
        }

        consumed("weak_rejuvenation_potion") { _, _ ->
            levels.restore(Skill.Prayer, 4, 0.08)
            levels.restore(Skill.Summoning, 4, 0.08)
        }

        consumed("weak_gatherers_potion") { _, _ ->
            levels.boost(Skill.Woodcutting, 3, 0.02)
            levels.boost(Skill.Mining, 3, 0.02)
            levels.boost(Skill.Fishing, 3, 0.02)
        }

        consumed("weak_artisans_potion") { _, _ ->
            levels.boost(Skill.Smithing, 3, 0.02)
            levels.boost(Skill.Crafting, 3, 0.02)
            levels.boost(Skill.Fletching, 3, 0.02)
            levels.boost(Skill.Construction, 3, 0.02)
            levels.boost(Skill.Firemaking, 3, 0.02)
        }

        consumed("weak_naturalists_potion") { _, _ ->
            levels.boost(Skill.Cooking, 3, 0.02)
            levels.boost(Skill.Farming, 3, 0.02)
            levels.boost(Skill.Herblore, 3, 0.02)
            levels.boost(Skill.Runecrafting, 3, 0.02)
        }

        consumed("weak_survivalists_potion") { _, _ ->
            levels.boost(Skill.Agility, 3, 0.02)
            levels.boost(Skill.Hunter, 3, 0.02)
            levels.boost(Skill.Thieving, 3, 0.02)
            levels.boost(Skill.Slayer, 3, 0.02)
        }

        consumed("melee_potion") { _, _ ->
            levels.boost(Skill.Attack, 3, 0.11)
            levels.boost(Skill.Strength, 3, 0.11)
        }

        consumed("magic_potion_dungeoneering") { _, _ ->
            levels.boost(Skill.Magic, 3, 0.11)
        }

        consumed("ranged_potion_dungeoneering") { _, _ ->
            levels.boost(Skill.Ranged, 3, 0.11)
        }

        consumed("defence_potion_dungeoneering") { _, _ ->
            levels.boost(Skill.Defence, 3, 0.11)
        }

        consumed("stat_restore_potion_dungeoneering") { _, _ ->
            Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
                levels.restore(skill, 7, 0.17)
            }
        }

        consumed("cure_potion") { _, _ ->
            antiPoison(10)
            superAntifire(10)
        }

        consumed("rejuvenation_potion") { _, _ ->
            levels.restore(Skill.Prayer, 7, 0.15)
            levels.restore(Skill.Summoning, 7, 0.15)
        }

        consumed("gatherers_potion") { _, _ ->
            levels.boost(Skill.Woodcutting, 4, 0.04)
            levels.boost(Skill.Mining, 4, 0.04)
            levels.boost(Skill.Fishing, 4, 0.04)
        }

        consumed("artisans_potion") { _, _ ->
            levels.boost(Skill.Smithing, 4, 0.04)
            levels.boost(Skill.Crafting, 4, 0.04)
            levels.boost(Skill.Fletching, 4, 0.04)
            levels.boost(Skill.Construction, 4, 0.04)
            levels.boost(Skill.Firemaking, 4, 0.04)
        }

        consumed("naturalists_potion") { _, _ ->
            levels.boost(Skill.Cooking, 4, 0.04)
            levels.boost(Skill.Farming, 4, 0.04)
            levels.boost(Skill.Herblore, 4, 0.04)
            levels.boost(Skill.Runecrafting, 4, 0.04)
        }

        consumed("survivalists_potion") { _, _ ->
            levels.boost(Skill.Agility, 4, 0.04)
            levels.boost(Skill.Hunter, 4, 0.04)
            levels.boost(Skill.Thieving, 4, 0.04)
            levels.boost(Skill.Slayer, 4, 0.04)
        }

        consumed("strong_melee_potion") { _, _ ->
            levels.boost(Skill.Attack, 6, 0.2)
            levels.boost(Skill.Strength, 6, 0.2)
        }

        consumed("strong_magic_potion") { _, _ ->
            levels.boost(Skill.Magic, 6, 0.2)
        }

        consumed("strong_ranged_potion") { _, _ ->
            levels.boost(Skill.Ranged, 6, 0.2)
        }

        consumed("strong_defence_potion") { _, _ ->
            levels.boost(Skill.Defence, 6, 0.2)
        }

        consumed("strong_stat_restore_potion") { _, _ ->
            Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
                levels.restore(skill, 10, 0.24)
            }
        }

        consumed("strong_cure_potion") { _, _ ->
            antiPoison(20)
            superAntifire(20)
        }

        consumed("strong_rejuvenation_potion") { _, _ ->
            levels.restore(Skill.Prayer, 10, 0.22)
            levels.restore(Skill.Summoning, 10, 0.22)
        }

        consumed("strong_gatherers_potion") { _, _ ->
            levels.boost(Skill.Woodcutting, 6, 0.06)
            levels.boost(Skill.Mining, 6, 0.06)
            levels.boost(Skill.Fishing, 6, 0.06)
        }

        consumed("strong_artisans_potion") { _, _ ->
            levels.boost(Skill.Smithing, 6, 0.06)
            levels.boost(Skill.Crafting, 6, 0.06)
            levels.boost(Skill.Fletching, 6, 0.06)
            levels.boost(Skill.Construction, 6, 0.06)
            levels.boost(Skill.Firemaking, 6, 0.06)
        }

        consumed("strong_naturalists_potion") { _, _ ->
            levels.boost(Skill.Cooking, 6, 0.06)
            levels.boost(Skill.Farming, 6, 0.06)
            levels.boost(Skill.Herblore, 6, 0.06)
            levels.boost(Skill.Runecrafting, 6, 0.06)
        }

        consumed("strong_survivalists_potion") { _, _ ->
            levels.boost(Skill.Agility, 6, 0.06)
            levels.boost(Skill.Hunter, 6, 0.06)
            levels.boost(Skill.Thieving, 6, 0.06)
            levels.boost(Skill.Slayer, 6, 0.06)
        }
    }
}
