package content.minigame.sorceress_garden

import content.entity.obj.door.doorTarget
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.player
import content.skill.magic.jewellery.teleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.type.Tile

class SorceressGarden(val dropTables: DropTables) : Script {

    init {
        objectOperate("Drink-from", "sorceress_garden_fountain") {
            anim("osman_fountain_drink")
            delay(6)
            teleport(Tile(3321, 3141), "modern")
        }

        objectOperate("Open", "sorceress_gate_winter") { (target) ->
            enterGarden(target, this)
        }

        objectOperate("Open", "sorceress_gate_spring") { (target) ->
            if (levels.get(Skill.Thieving) < 25) {
                item("highwayman_mask", 145, "You need a Thieving level of 25 to pick the lock of this gate.")
            } else {
                enterGarden(target, this)
            }
        }

        objectOperate("Open", "sorceress_gate_autumn") { (target) ->
            if (levels.get(Skill.Thieving) < 45) {
                item("highwayman_mask", 145, "You need a Thieving level of 45 to pick the lock of this gate.")
            } else {
                enterGarden(target, this)
            }
        }

        objectOperate("Open", "sorceress_gate_summer") { (target) ->
            if (levels.get(Skill.Thieving) < 65) {
                item("highwayman_mask", 145, "You need a Thieving level of 65 to pick the lock of this gate.")
            } else {
                enterGarden(target, this)
            }
        }

        objectOperate("Pick-fruit", "sqirk_tree_summer,sqirk_tree_spring,sqirk_tree_autumn,sqirk_tree_winter") { (target) ->
            pickFruit(target.id.removePrefix("sqirk_tree_"))
        }

        objectOperate("Pick", "sorceress_herbs_spring,sorceress_herbs_summer,sorceress_herbs_autumn,sorceress_herbs_winter") { (target) ->
            pickHerb(target.id.removePrefix("sorceress_herbs_"))
        }
    }

    fun enterGarden(target: GameObject, player: Player) {
        val target = doorTarget(player, target) ?: return
        player.sound("gate_open")
        player.tele(target)
    }

    suspend fun Player.pickFruit(type: String) {
        if (!inventory.add("${type}_sqirk")) {
            player<Idle>("I cannot carry any more.")
            return
        }
        sound("osman_pick_high")
        animDelay("picking_high")
        when (type) {
            "summer" -> experience.add(Skill.Thieving, 60.0)
            "autumn" -> experience.add(Skill.Thieving, 50.0)
            "spring" -> experience.add(Skill.Thieving, 40.0)
            "winter" -> experience.add(Skill.Thieving, 30.0)
        }
        leave()
    }

    private suspend fun Player.leave() {
        delay(2)
        sound("smoke_puff")
        gfx("puff")
        open("fade_out")
        delay(2)
        tele(2911, 5470)
        open("fade_in")
    }

    suspend fun Player.pickHerb(type: String) {
        if (inventory.spaces < 2) {
            player<Idle>("I cannot carry any more.")
            return
        }
        val table = dropTables.get("${type}_herbs_drop_table")
        if (table != null) {
            val drops = mutableListOf<ItemDrop>()
            table.roll(list = drops)
            table.roll(list = drops)
            inventory.transaction {
                for (drop in drops) {
                    add(drop.id, 1)
                }
            }
            when (inventory.transaction.error) {
                is TransactionError.Full -> {
                    player<Idle>("I cannot carry any more.")
                    return
                }
                TransactionError.None -> {}
                else -> return
            }
        }
        sound("pick")
        animDelay("climb_down")
        experience.add(Skill.Farming, 25.0)
        leave()
    }
}
