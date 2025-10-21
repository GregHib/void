package content.area.kandarin.ardougne.west_ardougne

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

@Script
class Mourner {

    val npcs: NPCs by inject()
    val objects: GameObjects by inject()

    init {
        npcOperate("Talk-to", "mourner_elena_guard_vis") {
            if (player.holdsItem("warrant")) {
                player<Neutral>("I have a warrant from Bravek to enter here.")
                npc<Uncertain>("This is highly irregular. Please wait...")
                val otherGuard = npcs[if (target.tile.equals(2539, 3273)) Tile(2534, 3273) else Tile(2539, 3273)].first { it.id == "mourner_elena_guard_vis" }
                val faceDirection = if (target.tile.equals(2539, 3273)) Direction.EAST else Direction.WEST
                target.face(faceDirection)
                delay(1)
                target.say("Hey... I've got someone here with a warrant from Bravek, what should we do?")
                delay(2)
                otherGuard.face(faceDirection)
                delay(1)
                otherGuard.say("Well you can't let them in...")
                delay(1)
                val doorTile = if (target.tile.equals(2539, 3273)) Tile(2540, 3273) else Tile(2533, 3273)
                val door = objects[doorTile, "door_plague_city_closed"]!!
                enterDoor(door, delay = 2)
                statement("You wait until the mourner's back is turned and sneak into the building.")
                return@npcOperate
            }
            npc<Uncertain>("Hmmm, how did you get over here? You're not one of this rabble. Ah well, you'll have to stay. Can't risk you going back now.")
            choice {
                option<Quiz>("So what's a mourner?") {
                    npc<Neutral>("We're working for King Lathas of East Ardougne. He has tasked us with containing the accursed plague sweeping West Ardougne.")
                    npc<Neutral>("We also do our best to ease these people's suffering. We're nicknamed mourners because we spend a lot of time at plague victim funerals, no one else is allowed to risk attending.")
                    npc<Neutral>("It's a demanding job, and we get little thanks from the people here.")
                }
                option<Angry>("I haven't got the plague though...") {
                    npc<Neutral>("Can't risk you being a carrier. That protective clothing you have isn't regulation issue. It won't meet safety standards.")
                }
                option<Neutral>("I'm looking for a woman named Elena.") {
                    npc<Neutral>("Ah yes, I've heard of her. A healer I believe. She must be mad coming over here voluntarily.")
                    npc<Neutral>("I hear rumours she has probably caught the plague now. Very tragic, a stupid waste of life.")
                }
            }
        }
    }
}
