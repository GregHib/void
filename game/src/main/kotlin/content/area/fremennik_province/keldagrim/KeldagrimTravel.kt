package content.area.fremennik_province.keldagrim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class KeldagrimTravel : Script {

    init {
        objTeleportLand("Go-through", "keldagrim_cave_entrance,keldagrim_entrance") { _, _ ->
            message("You're just about able to squeeze through.")
        }

        npcOperate("Talk-to", "dwarven_boatman_keldagrim_2") {
            if (get("keldagrim_visited", false)) {
                npc<Happy>("Hello again, $name! Want to go back to Keldagrim?")
                choice {
                    option<Happy>("Yes, please take me.") {
                        boat(Tile(2892, 10225))
                    }
                    option<Neutral>("What, on your ship? No way.")
                }
                return@npcOperate
            }
            npc<Happy>("Ho there, human!")
            player<Happy>("$name.")
            npc<Happy>("Ho there, $name! Want to take a ride with me?")
            player<Quiz>("Where are you going? Across the river?")
            npc<Happy>("No no, that's what the ferryman is for! I'm going to Keldagrim, my home!")
            player<Quiz>("How much will that cost me then?")
            npc<Happy>("For a human like you, I can do it for free!")
            choice {
                option<Happy>("That's a deal!") {
                    npc<Happy>("Excellent! I'm just waiting for my ship to arrive and then we can go.")
                    npc<Happy>("Mind, this trip could take a few minutes! Are you sure you're ready to go as well?")
                    choice {
                        option<Happy>("Yes, I'm ready and don't mind it taking a few minutes.") {
                            npc<Happy>("Well, let's not waste any more time then!")
                            boat(Tile(2892, 10225))
                            set("keldagrim_visited", true)
                        }
                        option<Neutral>("No, I don't have time right now, I'll be back later.") {
                            npc<Happy>("Sure, sure! Come back when you need to!")
                        }
                    }
                }
                option<Neutral>("I prefer the mines to the city.") {
                    npc<Happy>("Well, suit yourself then!")
                }
            }
        }

        npcOperate("Talk-to", "dwarven_boatman_keldagrim_3") {
            npc<Happy>("Want me to take you back to the mines?")
            choice {
                option<Happy>("Yes, please take me.") {
                    boat(Tile(2838, 10127))
                }
                option<Angry>("What, on your ship! No thanks!") {
                    npc<Neutral>("Hey now, it was only a slight accident!")
                    player<Angry>("A slight accident?? Have you any idea how much time I spent rebuilding that statue?")
                    npc<Neutral>("Calm down, calm down! You got what you paid for the trip, didn't you?")
                }
            }
        }

        npcOperate("Talk-to", "dwarven_ferryman_keldagrim*") { (target) ->
            npc<Neutral>("Hello there, want a ride?")
            player<Quiz>("A ride, where to?")
            npc<Neutral>("Across the river, across the river! It's just a short ride!")
            player<Quiz>("How much will that cost me?")
            npc<Happy>("Haha, you understand us dwarves well, human!")
            npc<Neutral>("It's just 2 gold pieces! Want to go?")
            choice {
                option<Quiz>("I'd like to go further downstream.") {
                    npc<Quiz>("You mean to the city of Keldagrim?")
                    player<Neutral>("Ehm, yes, I think so.")
                    npc<Neutral>("Sorry, I'm just a simple ferryman, I only go across.")
                    npc<Neutral>("But talk to my friend here, he sometimes goes to the city in his ship.")
                    player<Happy>("Thanks.")
                }
                option<Happy>("Yes please.") {
                    crossRiver(target)
                }
                option<Neutral>("No thanks.")
            }
        }

        npcOperate("Cross-river", "dwarven_ferryman_keldagrim*") { (target) ->
            crossRiver(target)
        }

        objectOperate("Ride", "train_cart_dwarven_mine,train_cart_white_wolf_mountain") {
            // TODO require keldagrim_visited once The Giant Dwarf is added
            choice {
                option("Travel to Keldagrim") {
                    toKeldagrim()
                }
                option("Stay here.")
            }
        }

        objectOperate("Open", "hidden_trapdoor_grand_exchange") {
            // TODO require keldagrim_visited once The Giant Dwarf is added
            statement("This trapdoor leads to a small dwarven mine cart station.<br>The mine cart will take you to Keldagrim.")
            choice {
                option("Travel to Keldagrim") {
                    toKeldagrim()
                }
                option("Stay here.")
            }
        }

        objectOperate("Ride", "train_cart_keldagrim") {
            // TODO require keldagrim_visited once The Giant Dwarf is added
            choice {
                option("To the Grand Exchange") {
                    fromKeldagrim(Tile(3140, 3507))
                }
                option("To Ice Mountain") {
                    fromKeldagrim(Tile(2997, 9837))
                }
                if (questCompleted("fishing_contest")) {
                    option("To White Wolf Mountain") {
                        fromKeldagrim(Tile(2875, 9871))
                    }
                }
                option("Stay here.")
            }
        }
    }

    private suspend fun Player.crossRiver(ferryman: NPC) {
        if (!inventory.remove("coins", 2)) {
            npc<Neutral>("You don't even have 2 gold coins, humans!")
            return
        }
        tele(if (ferryman.id == "dwarven_ferryman_keldagrim_2") Tile(2864, 10133) else Tile(2836, 10143))
    }

    private fun Player.rideCart(ride: Boolean) {
        if (ride) {
            renderEmote("mine_cart")
            body.kit(BodyPart.RightHand, 82)
            body.kit(BodyPart.LeftHand, -1)
        } else {
            clearRenderEmote()
            body.kit(BodyPart.RightHand, null)
            body.kit(BodyPart.LeftHand, null)
        }
        flagAppearance()
    }

    private suspend fun Player.boat(tile: Tile) {
        open("fade_out")
        delay(3)
        tele(tile)
        delay(1)
        open("fade_in")
    }

    private suspend fun Player.toKeldagrim() {
        if (!questCompleted("the_giant_dwarf")) {
            message("You must have completed The Giant Dwarf quest.")
            return
        }
        open("fade_out")
        minimap(Minimap.HideMap)
        delay(5)
        // Turn while the screen is black so the cart doesn't visibly rotate on arrival
        face(Direction.WEST)
        delay(1)
        tele(2943, 10170)
        sound("cart_away", delay = 30)
        rideCart(true)
        open("fade_in")
        // Follow the track around the bend into the station
        sound("forget_cart_turn")
        exactMoveDelay(Tile(2941, 10170), delay = 30, direction = Direction.WEST)
        sound("forget_cart_turn")
        exactMoveDelay(Tile(2939, 10173), delay = 30, direction = Direction.NORTH_WEST)
        sound("forget_cart_turn")
        exactMoveDelay(Tile(2909, 10173), delay = 288, direction = Direction.WEST)
        rideCart(false)
        clearMinimap()
        // Client ignores a teleport that lands while its exact move is still playing
        delay(1)
        tele(2909, 10174)
    }

    private suspend fun Player.fromKeldagrim(destination: Tile) {
        if (!questCompleted("the_giant_dwarf")) {
            message("You must have completed The Giant Dwarf quest.")
            return
        }
        open("fade_out")
        minimap(Minimap.HideMap)
        delay(3)
        face(Direction.EAST)
        delay(1)
        tele(2911, 10171)
        rideCart(true)
        open("fade_in")
        exactMoveDelay(Tile(2937, 10171), delay = 240, direction = Direction.EAST)
        // Follow the track around the bends out of the station and into the tunnel
        exactMoveDelay(Tile(2938, 10169), delay = 30, direction = Direction.SOUTH_EAST)
        exactMoveDelay(Tile(2938, 10165), delay = 30, direction = Direction.SOUTH)
        exactMoveDelay(Tile(2940, 10164), delay = 30, direction = Direction.EAST)
        exactMoveDelay(Tile(2944, 10164), delay = 30, direction = Direction.EAST)
        open("fade_out")
        delay(4)
        rideCart(false)
        tele(destination)
        delay(2)
        open("fade_in")
        clearMinimap()
    }
}
