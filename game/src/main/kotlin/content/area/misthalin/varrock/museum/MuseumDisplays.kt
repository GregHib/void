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
                    target.replace("vm_button_1x1", ticks = 14)
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
                    target.replace("vm_button_1x1", ticks = 14)
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
                "vm_button_monkey" -> {
                    target.replace("vm_button_1x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1778, 4958), height = 550, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1776, 4956), height = 325, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val monkey = NPCs.findBySpawn(Tile(1774, 4954), "monkey_display")
                    monkey.anim("vm_natural_history_display_case_monkey")
                    val cogs = GameObjects.find(Tile(1774, 4953), "vm_cogs_monkey")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_monkey", tile = target.tile.add(-1, 0), repeat = 2, radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_seaslug" -> {
                    target.replace("vm_button_1x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1782, 4959), height = 350, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1782, 4958), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val slug = NPCs.findBySpawn(Tile(1781, 4954), "sea_slugs_display")
                    slug.anim("vm_natural_history_display_case_seaslug")
                    val cogs = GameObjects.find(Tile(1781, 4953), "vm_cogs_sea_slug")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_seaslug", tile = target.tile.add(-1, 0), repeat = 4, radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_snake" -> {
                    target.replace("vm_button_1x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1779, 4962), height = 350, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1781, 4964), height = 325, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = GameObjects.find(Tile(1781, 4964), "vm_nat_his_snake")
                    snake.anim("vm_natural_history_display_case_snake")
                    val cogs = GameObjects.find(Tile(1781, 4967), "vm_cogs_snake")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_seaslug", tile = target.tile.add(1, 0), repeat = 4, radius = 10)
                    delay(7)
                    snake.anim("vm_natural_history_display_case_snake_fixed")
                    delay(2)
                    clearCamera()
                }
                "vm_button_snail" -> {
                    target.replace("vm_button_1x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1778, 4960), height = 425, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1775, 4965), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = NPCs.findBySpawn(Tile(1774, 4964), "snail_display")
                    snake.anim("vm_natural_history_display_case_snail")
                    val cogs = GameObjects.find(Tile(1774, 4967), "vm_cogs_snail")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_snail", tile = target.tile.add(1, 0), repeat = 3, radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_penguine" -> {
                    target.replace("vm_button_1x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1749, 4959), height = 500, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1743, 4956), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = NPCs.findBySpawn(Tile(1742, 4954), "penguin_display")
                    snake.anim("vm_natural_history_display_case_penguin")
                    val cogs = GameObjects.find(Tile(1742, 4953), "vm_cogs_penguine")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_penguin", tile = target.tile.add(0, -1), radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_mole" -> {
                    target.replace("vm_button_1x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1733, 4959), height = 375, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1734, 4958), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = NPCs.findBySpawn(Tile(1735, 4954), "mole_display")
                    snake.anim("vm_natural_history_display_case_mole")
                    val cogs = GameObjects.find(Tile(1735, 4953), "vm_cogs_mole")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_mole_burrow_up", tile = target.tile.add(0, -1), radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_camel" -> {
                    target.replace("vm_button_1x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1732, 4958), height = 500, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1735, 4962), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = NPCs.findBySpawn(Tile(1735, 4964), "camel_display")
                    snake.anim("vm_natural_history_display_case_camel")
                    val cogs = GameObjects.find(Tile(1735, 4967), "vm_cogs_camel")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_camel", tile = target.tile.add(0, 1), radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_leech" -> {
                    target.replace("vm_button_1x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1743, 4956), height = 650, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1743, 4962), height = 200, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = NPCs.findBySpawn(Tile(1742, 4964), "leech_display")
                    snake.anim("vm_natural_history_display_case_leech")
                    val cogs = GameObjects.find(Tile(1742, 4967), "vm_cogs_leech")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_leech", tile = target.tile.add(0, 1), radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_lizard" -> {
                    target.replace("vm_button_2x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1747, 4975), height = 525, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1742, 4980), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = NPCs.findBySpawn(Tile(1740, 4979), "lizard_display")
                    snake.anim("vm_natural_history_display_case_lizard")
                    val cogs = GameObjects.find(Tile(1740, 4983), "vm_cogs_lizard")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_lizard", tile = target.tile.add(0, 1), radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_battle_tortoise" -> {
                    target.replace("vm_button_2x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1756, 4976), height = 425, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1753, 4979), height = 250, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = NPCs.findBySpawn(Tile(1750, 4979), "battle_tortoise_display")
                    snake.anim("vm_natural_history_display_case_battle_tortoise")
                    val cogs = GameObjects.find(Tile(1750, 4984), "vm_cogs_battle_tortoise")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_tortoise", tile = target.tile.add(0, 1), radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_dragon" -> {
                    target.replace("vm_button_2x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1774, 4978), height = 925, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1766, 4981), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val snake = NPCs.findBySpawn(Tile(1765, 4979), "dragon_display")
                    snake.anim("vm_natural_history_display_case_dragon")
                    val cogs = GameObjects.find(Tile(1765, 4984), "vm_cogs_dragon")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_dragon", tile = target.tile.add(0, 1), radius = 10)
                    delay(9)
                    clearCamera()
                }
                "vm_button_wyvern" -> {
                    target.replace("vm_button_2x1", ticks = 14)
                    delay(1)
                    anim("vm_player_button_push")
                    moveCamera(tile = Tile(1781, 4977), height = 800, speed = 10, acceleration = 10)
                    turnCamera(tile = Tile(1776, 4980), height = 300, speed = 10, acceleration = 10)
                    sound("vm_push_button")
                    delay(4)
                    val wyvern = NPCs.findBySpawn(Tile(1775, 4979), "wyvern_display")
                    wyvern.anim("vm_natural_history_display_case_wyvern")
                    val cogs = GameObjects.find(Tile(1775, 4983), "vm_cogs_wyvern")
                    cogs.anim("vm_display_case_cogs")
                    areaSound("vm_wyvern", tile = target.tile.add(0, 1), radius = 10)
                    delay(9)
                    clearCamera()
                }
            }
        }
    }
}
