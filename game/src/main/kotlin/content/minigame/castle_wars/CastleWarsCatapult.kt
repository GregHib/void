package content.minigame.castle_wars

import content.entity.gfx.areaGfx
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class CastleWarsCatapult : Script {
    init {
        objectOperate("Operate", "castle_wars_catapult_saradomin") {
            if (equipped(EquipSlot.Cape).id != "saradomin_team_cape") {
                message("This is not your teams catapult")// TODO proper message
                return@objectOperate
            }
            // TODO one person at a time?
            open("castle_wars_catapult_controls")
        }

        objectOperate("Operate", "castle_wars_catapult_zamorak") {
            if (equipped(EquipSlot.Cape).id != "zamorak_team_cape") {
                message("This is not your teams catapult")// TODO proper message
                return@objectOperate
            }
            // TODO one person at a time?
            open("castle_wars_catapult_controls")
        }

        interfaceOpened("castle_wars_catapult_controls") {
            // TODO anim?
            walkTrigger {
                close("castle_wars_catapult_controls")
            }
        }

        interfaceOption("Close", "castle_wars_catapult_controls:close") {
            close("castle_wars_catapult_controls")
        }

        interfaceOption("Fire", "castle_wars_catapult_controls:fire") {
            val catapult = GameObjects.getLayer(Tile(2384, 3117), ObjectLayer.GROUND) ?: return@interfaceOption
            catapult.anim("castle_wars_catapult_release")
        }

        itemOnObjectOperate("rock_castle_wars", "castle_wars_catapult_zamorak") { (target) ->
            target.anim("castle_wars_catapult_ready")
        }

        itemOption("Ignite", "flare") {
            if (inventory.remove("flare")) {
                anim("castle_wars_player_place_flare")
                areaGfx("${equipped(EquipSlot.Cape).id.removeSuffix("_team_cape")}_flare", tile, delay = 30)
            }
        }
        // https://youtu.be/gvDzJzAOnH8?t=270
        // message("The catapult fires and...")
        // message("It hit some enemy defences!.")
        // message("It didn't hit anyone.")
        // message("It hit <name> for <dmg> damage!")
        // message("It missed <name> - their decorative armour protected them.)
    }
}