package content.quest.free.ernest_the_chicken

import content.entity.combat.hit.damage
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.player
import content.quest.free.ernest_the_chicken.ErnestTheChicken.Companion.ownsErnestPart
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class DraynorFountain : Script {
    init {
        objectOperate("Search", "fountain") { (target) -> search(target) }

        itemOnObjectOperate("poisoned_fish_food", "fountain") { (target) -> poison(target) }

        itemOnObjectOperate("fish_food", "fountain") { (target) -> feed(target) }

        itemOnObjectOperate("*", "fountain") { (target, item) -> useItem(target, item) }
    }

    private suspend fun Player.search(target: GameObject) {
        if (target.tile != Tile(3087, 3334)) {
            return
        }
        if (!get("ernest_fountain_poisoned", false)) {
            player<Neutral>("There seems to be a pressure gauge in here...")
            message("Something in the water bites you.")
            damage(10)
            player<Neutral>("Ow!")
            player<Neutral>("... and a lot of piranhas! I can't get the gauge out.")
        } else if (ownsErnestPart("pressure_gauge")) {
            player<Neutral>("It's full of dead fish!")
        } else {
            player<Neutral>("There seems to be a pressure gauge in here...")
            player<Neutral>("... and a lot of dead fish.")
            if (inventory.add("pressure_gauge")) {
                message("You get the pressure gauge from the fountain.")
            } else {
                message("You don't have enough inventory space to take the pressure gauge.")
            }
        }
    }

    private fun Player.poison(target: GameObject) {
        if (target.tile != Tile(3087, 3334)) {
            return
        }
        if (inventory.remove("poisoned_fish_food")) {
            set("ernest_fountain_poisoned", true)
            message("You pour the poisoned fish food into the fountain.")
            message("The piranhas start eating the food...")
            message("... then die and float to the surface.")
        }
    }

    private fun Player.feed(target: GameObject) {
        if (target.tile != Tile(3087, 3334)) {
            return
        }
        if (inventory.remove("fish_food")) {
            message("You pour the fish food into the fountain.")
            message("The piranhas start eating the food...")
            message("Now they seem hungrier than ever!")
        }
    }

    private suspend fun Player.useItem(target: GameObject, item: Item) {
        if (target.tile != Tile(3087, 3334) || item.id in setOf("fish_food", "poisoned_fish_food")) {
            return
        }
        if (get("ernest_fountain_poisoned", false)) {
            if (item.def.contains("full")) {
                player<Neutral>("I don't think I want to use that water any more.")
            }
        } else {
            message("Something in the water bites you.")
            damage(10)
            player<Neutral>("Ow!")
        }
    }
}
