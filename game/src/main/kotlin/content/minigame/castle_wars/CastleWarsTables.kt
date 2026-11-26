package content.minigame.castle_wars

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.addToLimit
import world.gregs.voidps.engine.inv.inventory

class CastleWarsTables : Script {
    init {
        objectOperate("Take-1", "castle_wars_counter_bandages_*") {
            give("bandages_castle_wars", 1)
        }

        objectOperate("Take-5", "castle_wars_counter_bandages_*") {
            give("bandages_castle_wars", 5)
        }

        // TODO can you take from the enemies tables?
        // TODO item limits per person
        objectOperate("Take-1", "castle_wars_counter_pickaxes_*") {
            give("bronze_pickaxe", 1)
        }

        objectOperate("Take-1", "castle_wars_counter_rocks_*") {
            give("rock_castle_wars", 1)
        }

        objectOperate("Take-5", "castle_wars_counter_rocks_*") {
            give("rock_castle_wars", 5)
        }

        objectOperate("Take-1", "castle_wars_counter_flare_*") {
            give("flare", 1)
        }

        objectOperate("Take-5", "castle_wars_counter_flare_*") {
            give("flare", 5)
        }

        objectOperate("Take-1", "castle_wars_counter_rope_*") {
            give("climbing_rope", 1)
        }

        objectOperate("Take-5", "castle_wars_counter_rope_*") {
            give("climbing_rope", 5)
        }

        objectOperate("Take-1", "castle_wars_counter_potions_*") {
            give("explosive_potion", 1)
        }

        objectOperate("Take-5", "castle_wars_counter_potions_*") {
            give("explosive_potion", 5)
        }

        objectOperate("Take-1", "castle_wars_counter_barricades_*") {
            give("barricade_castle_wars", 1)
        }

        objectOperate("Take-5", "castle_wars_counter_barricades_*") {
            give("barricade_castle_wars", 5)
        }

        objectOperate("Take-1", "castle_wars_counter_toolbox_*") {
            give("toolkit_castle_wars", 1)
        }
    }

    private fun Player.give(item: String, amount: Int) {
        inventory.addToLimit(item, amount)
    }

}