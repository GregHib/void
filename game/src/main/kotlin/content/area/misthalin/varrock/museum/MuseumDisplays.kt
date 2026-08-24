package content.area.misthalin.varrock.museum

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Tile

class MuseumDisplays : Script {
    init {
        objectOperate("Study", "vm_plaque_*") { (target) ->
            when (target.id) {
                "vm_plaque_lizard" -> message("The Giant Lizard (Lacertilia Giganteus)")
                "vm_plaque_battle_tortoise" -> message("The Giant Tortoise (Testudines Giganteus)")
                "vm_plaque_dragon" -> message("The Dragon (Draconis Rex)")
                "vm_plaque_wyvern" -> message("The Wyvern (Draconis Ossis)")
                "vm_plaque_camel" -> message("The Camel (Camelus Bactrian)")
                "vm_plaque_leech" -> message("The Giant Leech (Hirudinea Acidia)")
                "vm_plaque_mole" -> message("The Giant Mole (Talpidae Wysonian)")
                "vm_plaque_penguine" -> message("The Penguin (Spheniscidae Callidus)")
                "vm_plaque_snail" -> message("The Giant Snail (Achatina Acidia Giganteus)")
                "vm_plaque_snake" -> message("The Snake (Serpentes Fellis)")
                "vm_plaque_monkey" -> message("The Monkey (Simiiformes Karamjan)")
                "vm_plaque_seaslug" -> message("The Sea Slug (Opisthobranchia Alucinor)")
                "vm_plaque_terrorbird" -> message("The Terrorbird (Aves Terror)")
                "vm_plaque_kalphite_queen" -> message("The Kalphite Queen (Kalphiscarabeinae Pasha)")
            }
        }

        objectOperate("Push", "vm_button_*") { (target) ->
            when (target.id) {
                "vm_button_terrorbird" -> {
                    target.replace("vm_button_1x1", ticks = 7)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1757, 4942), height = 375, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1756, 4941), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val bird = NPCs.findBySpawn(Tile(1752, 4938), "terrorbird_display")
                    bird.anim("vm_natural_history_display_case_terror_bird")
                    val cogs = GameObjects.find(Tile(1751, 4938), "vm_cogs_terrorbird")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_terrorbird", tile = target.tile.add(-1, 0), radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_kalphite_queen" -> {
                    target.replace("vm_button_1x1", ticks = 7)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1759, 4946), height = 700, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1764, 4939), height = 100, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val queen = GameObjects.find(Tile(1763, 4937), "vm_nat_his_kalphite_queen")
                    queen.anim("vm_natural_history_display_case_kalphite_queen")
                    val cogs = GameObjects.find(Tile(1768, 4938), "vm_cogs_kalphite_queen")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_kalphite", tile = target.tile.add(1, 0), radius = 10)
                    delay(7)
                    queen.anim("vm_natural_history_display_case_kalphite_queen_fixed")
                    delay(2)
                    clearCamera()
                }
            }
        }
    }
}