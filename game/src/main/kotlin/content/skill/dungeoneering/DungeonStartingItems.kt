package content.skill.dungeoneering

import world.gregs.voidps.engine.entity.character.player.skill.Skill

object DungeonStartingItems {

    fun spawn(dungeon: DungeonMap, complexity: Int, skills: Map<Skill, Int>) {
        // Bound weapons - equip first weapon bound https://web.archive.org/web/20150406211914/http://www.xp-waste.com/weapon-wielded-at-the-start-of-a-floor-t2478.html
        // Equip type of ring of kinship
        if (complexity == 1) {
            // Add armour and spells of all styles to inventory
        }
    }
}
