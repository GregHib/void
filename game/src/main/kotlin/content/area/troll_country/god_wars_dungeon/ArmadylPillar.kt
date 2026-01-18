package content.area.troll_country.god_wars_dungeon

import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import content.skill.melee.weapon.Weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class ArmadylPillar(val objects: GameObjects) : Script {

    init {
        objectApproach("Grapple", "armadyl_pillar") { (target) ->
            steps.clear()
            val middle = Tile(2872, 5274, 2)
            val offset = if (tile.y > target.tile.y) 5 else -5
            val direction = if (tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
            walkToDelay(middle.addY(offset))
            if (!has(Skill.Ranged, 70, message = true)) {
                return@objectApproach
            }
            if (!Weapon.hasGrapple(this)) {
                return@objectApproach
            }
            delay()
            face(direction)
            message("You fire your grapple at the pillar...", type = ChatType.Filter)
            delay()
            sound("crossbow_grappling")
            anim("godwars_crossbow_swing", delay = 6)
            gfx("godwars_grapple_shoot", delay = 6)
            objects.findOrNull(Tile(2872, 5270, 2), "armadyl_pillar_grapple")?.anim(if (tile.y > target.tile.y) "godwars_grapple_swing_reverse" else "godwars_grapple_swing")
            delay(3)
            exactMoveDelay(middle, startDelay = 15, delay = 29, direction = direction)
            exactMoveDelay(middle.addY(-offset), delay = 21, direction = direction)
            message("...and swing safely to the other side.", type = ChatType.Filter)
            sound("land_flatter")
        }

        objectOperate("Search", "godwars_armadyl_crate") {
            val hasCrossbow = inventory.items.any { Weapon.crossbows.contains(it.id) } || equipment.items.any { Weapon.crossbows.contains(it.id) }
            val hasGrapple = holdsItem("mithril_grapple")
            if (!hasCrossbow || !hasGrapple) {
                if (inventory.add("bronze_crossbow", "mithril_grapple")) {
                    item("bronze_crossbow", 400, "Inside the crate you find a bronze crossbow and a grappling hook.")
                } else if (inventory.add("bronze_crossbow")) {
                    item("bronze_crossbow", 400, "Inside the crate is a crossbow and grappling hook; unfortunately, you are already carrying too much to pick them up.")
                } else {
                    statement("Inside the crate you find a bronze crossbow; there is also a grappling hook, but you don't have room to carry it.")
                }
            } else if (!hasCrossbow) {
                if (inventory.add("bronze_crossbow")) {
                    item("bronze_crossbow", 400, "You notice a bronze crossbow in the crate, which you add to the stuff in your backpack.")
                } else {
                    statement("Inside the crate is a bronze crossbow; unfortunately you have no room to carry it.")
                }
            } else if (!hasGrapple) {
                if (inventory.add("mithril_grapple")) {
                    item("mithril_grapple", 400, "You notice a grappling hook in the crate, which you add to the stuff in your backpack.")
                } else {
                    statement("Inside the crate is a grappling hook; unfortunately you have no room to carry it.")
                }
            } else {
                statement("There is nothing in the crate.")
            }
        }
    }
}
