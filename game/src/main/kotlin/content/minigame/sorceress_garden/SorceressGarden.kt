package content.minigame.sorceress_garden

import content.entity.obj.door.doorTarget
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.player
import content.entity.sound.sound
import content.skill.magic.jewellery.teleport
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Tile

@Script
class SorceressGarden : Api {
    val dropTables: DropTables by inject()

    init {
        objectOperate("Drink-from", "sorceress_garden_fountain") {
            player.anim("osman_fountain_drink")
            delay(6)
            player.teleport(Tile(3321, 3141), "modern")
        }

        objectOperate("Open", "sorceress_gate_winter") {
            enterGarden(target, player)
        }

        objectOperate("Open", "sorceress_gate_spring") {
            if (player.levels.get(Skill.Thieving) < 25) {
                item("highwayman_mask", 145, "You need a Thieving level of 25 to pick the lock of this gate.")
            } else {
                enterGarden(target, player)
            }
        }

        objectOperate("Open", "sorceress_gate_autumn") {
            if (player.levels.get(Skill.Thieving) < 45) {
                item("highwayman_mask", 145, "You need a Thieving level of 45 to pick the lock of this gate.")
            } else {
                enterGarden(target, player)
            }
        }

        objectOperate("Open", "sorceress_gate_summer") {
            if (player.levels.get(Skill.Thieving) < 65) {
                item("highwayman_mask", 145, "You need a Thieving level of 65 to pick the lock of this gate.")
            } else {
                enterGarden(target, player)
            }
        }

        objectOperate("Pick-fruit", "sqirk_tree_summer", "sqirk_tree_spring", "sqirk_tree_autumn", "sqirk_tree_winter") {
            pickFruit(target.id.removePrefix("sqirk_tree_"))
        }

        objectOperate("Pick", "sorceress_herbs_spring", "sorceress_herbs_summer", "sorceress_herbs_autumn", "sorceress_herbs_winter") {
            pickHerb(target.id.removePrefix("sorceress_herbs_"))
        }
    }

    fun enterGarden(target: GameObject, player: Player) {
        val target = doorTarget(player, target) ?: return
        player.sound("gate_open")
        player.tele(target)
    }

    suspend fun SuspendableContext<Player>.pickFruit(type: String) {
        if (!player.inventory.add("${type}_sqirk")) {
            player<Neutral>("I cannot carry any more.")
            return
        }
        player.sound("osman_pick_high")
        player.animDelay("picking_high")
        when (type) {
            "summer" -> player.experience.add(Skill.Thieving, 60.0)
            "autumn" -> player.experience.add(Skill.Thieving, 50.0)
            "spring" -> player.experience.add(Skill.Thieving, 40.0)
            "winter" -> player.experience.add(Skill.Thieving, 30.0)
        }
        leave()
    }

    private suspend fun SuspendableContext<Player>.leave() {
        delay(2)
        player.sound("smoke_puff")
        player.gfx("puff")
        player.open("fade_out")
        delay(2)
        player.tele(2911, 5470)
        player.open("fade_in")
    }

    suspend fun SuspendableContext<Player>.pickHerb(type: String) {
        if (player.inventory.spaces < 2) {
            player<Neutral>("I cannot carry any more.")
            return
        }
        val table = dropTables.get("${type}_herbs_drop_table")
        if (table != null) {
            val drops = mutableListOf<ItemDrop>()
            table.role(list = drops)
            table.role(list = drops)
            player.inventory.transaction {
                for (drop in drops) {
                    add(drop.id, 1)
                }
            }
            when (player.inventory.transaction.error) {
                is TransactionError.Full -> {
                    player<Neutral>("I cannot carry any more.")
                    return
                }
                TransactionError.None -> {}
                else -> return
            }
        }
        player.sound("pick")
        player.animDelay("climb_down")
        player.experience.add(Skill.Farming, 25.0)
        leave()
    }
}
