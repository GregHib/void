package content.minigame.castle_wars

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.addToLimit
import world.gregs.voidps.engine.inv.inventory

class CastleWarsTables : Script {
    init {
        objectOperate("Take-1", "castle_wars_counter_bandages_*") {
            give("bandages_castle_wars", 1, "any more bandages")
        }

        objectOperate("Take-5", "castle_wars_counter_bandages_*") {
            give("bandages_castle_wars", 5, "any more bandages")
        }

        objectOperate("Take-1", "castle_wars_counter_pickaxes_*") {
            give("bronze_pickaxe", 1, "any more pickaxes")
        }

        objectOperate("Take-1", "castle_wars_counter_rocks_*") {
            give("rock_castle_wars", 1, "any more rocks")
        }

        objectOperate("Take-5", "castle_wars_counter_rocks_*") {
            give("rock_castle_wars", 5, "any more rocks")
        }

        objectOperate("Take-1", "castle_wars_counter_flare_*") {
            give("flare", 1, "any more flares")
        }

        objectOperate("Take-5", "castle_wars_counter_flare_*") {
            give("flare", 5, "any more flares")
        }

        objectOperate("Take-1", "castle_wars_counter_rope_*") {
            give("climbing_rope", 1, "any more climbing rope")
        }

        objectOperate("Take-5", "castle_wars_counter_rope_*") {
            give("climbing_rope", 5, "any more climbing rope")
        }

        objectOperate("Take-1", "castle_wars_counter_potions_*") {
            give("explosive_potion", 1, "any more explosive potions")
        }

        objectOperate("Take-5", "castle_wars_counter_potions_*") {
            give("explosive_potion", 5, "any more explosive potions")
        }

        objectOperate("Take-1", "castle_wars_counter_barricades_*") {
            give("barricade_castle_wars", 1, "any more barricades")
        }

        objectOperate("Take-5", "castle_wars_counter_barricades_*") {
            give("barricade_castle_wars", 5, "any more barricades")
        }

        objectOperate("Take-1", "castle_wars_counter_toolbox_*") {
            give("toolkit_castle_wars", 1, "a toolkit")
        }
    }

    private suspend fun Player.give(item: String, amount: Int, name: String) {
        if (inventory.isFull()) {
            statement("Your inventory is too full to hold $name.")
            return
        }
        inventory.addToLimit(item, amount)
    }

}