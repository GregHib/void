package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.entity.player.modal.Tab
import content.quest.Cutscene
import content.quest.closeTabs
import content.quest.openTabs
import content.quest.startCutscene
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class NaturalHistorian : Script {
    init {
        npcOperate("Talk-to", "natural_historian_west") {
            npc<Happy>("Hello again, sir, how can I help you on this fine day?")
            player<Happy>("I was hoping you could tell me about something.")
            choice {
                option<Neutral>("Tell me about camels.") {
                    npc<Happy>("Ahh camels, the ships of the desert.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                }
                option("Tell me about leeches.")
                option("Tell me about moles.")
                option("Tell me about penguins.")
                option("That's enough education for one day.")
            }
        }

        npcOperate("Talk-to", "natural_historian_north") {

        }

        npcOperate("Talk-to", "natural_historian_east") {

        }

        npcOperate("Talk-to", "natural_historian_south") {
            npc<Happy>("Hello again, sir, how can I help you on this fine day?")
            player<Happy>("I was hoping you could tell me about something.")
            choice {
                option<Neutral>("Tell me about natural history.") {
                    npc<Happy>("Well, the field of natural history covers a wide range of sciences.")
                    npc<Happy>("So we use biology, the study of living things, botany, the study of plants and zoology, the study of animals.")
                    npc<Happy>("Though the field is growing all the time and we're also using techniques from magic, astrology and numerology.")
                    npc<Happy>("A person interested in natural history is known as a naturalist.")
                }
                option("Tell me about terrorbirds.") {
                    player<Neutral>("Tell me about the terrorbirds.")
                    npc<Happy>("Ahh terrorbirds, the fastest bird on two legs.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1757, 4939), Direction.EAST, npcs = { cutscene ->
                        listOf(NPCs.add("terrorbird_display", cutscene.tile(1752, 4938)))
                    }) { historian ->
                        moveCamera(tile = historian.tile.add(5, 5), height = 500, speed = 100, acceleration = 100)
                        turnCamera(tile = historian.tile, height = 300, speed = 100, acceleration = 100)
                        historian.anim("vm_natural_historian_terror_bird")
                        npc<Happy>("Terrorbirds live in nomadic groups of between five and fifty birds that often travel together with other grazing animals.")
                        npc<Happy>("They mainly feed on seeds and other plants. They also eat insects such as locusts if they can catch them.")
                        historian.anim("vm_natural_historian_terror_bird")
                        npc<Happy>("They have no teeth to chew with, so they swallow pebbles that help to grind the swallowed foods in the gizzard.")
                        npc<Happy>("They can go without water for a long time, exclusively living off the water in the plants. However, they enjoy water and frequently take baths when they can.")
                        historian.anim("vm_natural_historian_terror_bird")
                        npc<Happy>("Terrorbirds are known to eat almost anything, particularly in captivity, where opportunity is increased.")
                        npc<Happy>("Terrorbirds usually weigh a little less than a small unicorn. The feathers of adult males are mostly green, with some white on the wings and tail.")
                        historian.anim("vm_natural_historian_terror_bird")
                        npc<Happy>("There are claws on two of the wings' fingers and their strong legs have no feathers. The bird stands on two toes, with the bigger one resembling a hoof. Its feet have no claws.")
                        npc<Happy>("This is an adaptation unique to terrorbirds that appears to aid in running. Their legs are powerful enough to kill even large animals. ")
                        historian.anim("vm_natural_historian_terror_bird")
                        npc<Happy>("The gnomes in particular, prize the terrorbird for its fast running speed, using them as mounts whenever possible.")
                        npc<Happy>("There are a number of recorded incidents of people being attacked and killed. Big males can be very territorial and aggressive, and can attack and kick very powerfully with their legs.")
                        historian.anim("vm_natural_historian_terror_bird")
                        npc<Happy>("A terrorbird is so fast, it can easily outrun any human athlete.")
                        npc<Happy>("And this concludes my short lecture on terrorbirds. I hope you've enjoyed yourselves.")
                    }
                }
                option<Neutral>("Tell me about the Kalphite Queen.") {
                    npc<Happy>("Ahh kalphites, the insectoid eating machines.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1760, 4939), Direction.WEST) { historian ->
                        moveCamera(tile = historian.tile.add(-5, 5), height = 500, speed = 100, acceleration = 100)
                        turnCamera(tile = historian.tile, height = 300, speed = 100, acceleration = 100)
                        historian.anim("vm_natural_historian_kalphite_queen_pincers")
                        npc<Happy>("The kalphites, otherwise known as the kalphiscarabeinae, are perhaps the largest species of insect on the face of Gielinor. Their queen is called kalphiscarabeinae pasha.")
                        npc<Happy>("Most of the early documentation and research on this fearsome predatory species was performed by the noted bug hunter Iqbar Ali-Abdula.")
                        historian.anim("vm_natural_historian_kalphite_queen_pincers")
                        npc<Happy>("This, of course, was before he was driven insane by his research and ran off into the desert, screaming.")
                        npc<Happy>("Kalphites are related to beetles and scorpions; they are mainly green in colour. Some have remarkable antennae which can detect the slightest movement. Their carapace is composed of armoured plates called lamellae.")
                        historian.anim("vm_natural_historian_kalphite_queen_pincers")
                        npc<Happy>("This shell can be compressed into a ball or fanned out like leaves, in order to sense odours. The front legs are adapted for digging the enormous tunnel systems that serve as their nests.")
                        npc<Happy>("They exist in a caste-based society, with the soft shelled larvae at the bottom, up through the workers, soldiers and finally the queen.")
                        historian.anim("vm_natural_historian_kalphite_queen_pincers")
                        npc<Happy>("Voracious carnivores, a pack of adult workers can strip the flesh from a full grown camel in a matter of seconds, leaving nothing but a few bones and strips of fur for other scavengers to pick over.")
                        npc<Happy>("They typically live in large nests marked by the rock hard pillars found in hot, arid deserts, such as the one south-west of Al Kharid, which rise out of the sands like the tombs of desert pharaohs.")
                        historian.anim("vm_natural_historian_kalphite_queen_pincers")
                        npc<Happy>("Indeed, there is some relationship between the Kalphite Queen and the desert god Scabaras, but no one is really sure what.")
                        npc<Happy>("During the early part of the fourth age, Scabaras proclaimed himself omnipotent and outlawed worship of all other gods save him.")
                        npc<Happy>("When the people eventually revolted against his repressive rule and banished Scabaras, it is said his blood washed over the scarabs and transformed them into the kalphites we know today.")
                        historian.anim("vm_natural_historian_kalphite_queen_pincers")
                        npc<Happy>("Of course, any right-minded scientist discounts these myths as mere stories, with no historical basis in fact.")
                        npc<Happy>("And this concludes my short lecture on kalphites. I hope you've enjoyed yourselves.")
                    }
                }
                option<Bored>("That's enough education for one day.") {
                    npc<Happy>("Nonsense! There's always room for more.")
                    npc<Happy>("And remember, science isn't dull!")
                }
            }
        }

    }

    private suspend fun Player.cutscene(tile: Tile, direction: Direction, npcs: (Cutscene) -> List<NPC> = { emptyList() }, objects: (Cutscene) -> List<GameObject> = { emptyList() }, block: suspend Player.(NPC) -> Unit) {
        open("fade_out")
        delay(3)
        minimap(Minimap.HideMap)
        closeTabs(Tab.Options)
        val cutscene = startCutscene("historian", Region(6989))
        val button = GameObjects.add("vm_button_1x1", cutscene.convert(tile.add(direction.inverse()).add(direction.inverse())))
        val npcs = npcs.invoke(cutscene)
        val objects = objects.invoke(cutscene)
        val plaque = GameObjects.add("vm_plaque_inactive", cutscene.convert(tile.add(direction.inverse()).add(direction.inverse()).add(direction.rotate(-2))), rotation = direction.inverse().rotation())
        val historian = NPCs.add("vm_natural_historian_cutscene", cutscene.convert(tile.add(direction.inverse())))
        val north = NPCs.add(randomVisitor(), cutscene.convert(tile.add(direction).add(direction.rotate(2))))
        val south = NPCs.add(randomVisitor(north.id), cutscene.convert(tile.add(direction).add(direction.rotate(-2))))
        north.mode = PauseMode
        south.mode = PauseMode
        cutscene.onEnd {
            tele(tile)
            for (npc in npcs) {
                NPCs.remove(npc)
            }
            for (obj in objects) {
                obj.remove()
            }
            NPCs.remove(historian)
            NPCs.remove(north)
            NPCs.remove(south)
            button.remove()
            plaque.remove()
            open("fade_in")
            openTabs(Tab.Options)
            clearMinimap()
        }
        tele(cutscene.convert(tile.add(direction)), clearInterfaces = false)
        north.face(historian)
        south.face(historian)
        historian.face(direction)
        face(historian)
        open("fade_in")
        block(historian)
        open("fade_out")
        delay(3)
        cutscene.end()
    }

    private fun randomVisitor(exception: String? = null): String {
        val set = mutableSetOf("teacher_and_pupil", "schoolboy", "schoolgirl", "teacher_and_pupil_2")
        set.remove(exception)
        return set.random(random) // TODO unknown npcs without options
    }
}