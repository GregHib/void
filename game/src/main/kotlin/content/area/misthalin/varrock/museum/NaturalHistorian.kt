package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.modal.Tab
import content.quest.Cutscene
import content.quest.closeTabs
import content.quest.openTabs
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.type.Delta
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
                    cutscene(Tile(1736, 4961), Direction.SOUTH, Delta(2, -5), npcs = { cutscene ->
                        listOf(NPCs.add("camel_display", cutscene.tile(1735, 4964)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_camel_hump")
                        npc<Happy>("The term camel refers to either of the two known species of camelid.")
                        npc<Happy>("There is the bactrian, which has two humps and can be found throughout Al Kharid. It also refers to the Ugthanki, or one-humped camel.")
                        historian.anim("vm_natural_historian_camel_hump")
                        npc<Happy>("Camels are ill-tempered beasts at the best of times, requiring a great deal of time and effort to tame.")
                        npc<Happy>("This would explain why used camel salesmen are in such a hurry to be rid of them.")
                        historian.anim("vm_natural_historian_camel_hump")
                        npc<Happy>("In the wild, camels have been known to lie in wait and ambush hapless travellers, before devouring them.")
                        npc<Happy>("This is quite surprising, as most domestic camels are happy eating nothing but vegetables.")
                        historian.anim("vm_natural_historian_camel_hump")
                        npc<Happy>("Camel milk is much more nutritious than cow milk and goes well in the strong desert drink akin to tea.")
                        npc<Happy>("Another useful camel by-product is dung. Their dung is very dry, due to the highly efficient metabolism of the camel.")
                        historian.anim("vm_natural_historian_camel_hump")
                        npc<Happy>("Scientific research has also shown that chilli has a disastrous effect on a camel's digestive system, which produces toxic dung.")
                        npc<Happy>("And this concludes my short lecture on camels. I hope you've enjoyed yourselves.")
                    }
                }
                option("Tell me about leeches.") {
                    npc<Happy>("Ahh leeches, the haemophagic parasites.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1743, 4961), Direction.SOUTH, Delta(-2, -5), npcs = { cutscene ->
                        listOf(NPCs.add("leech_display", cutscene.tile(1742, 4964)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_leech_twist")
                        npc<Happy>("Leeches are fascinating creatures and are very similar to worms in most respects.")
                        npc<Happy>("They like to inhabit streams, rivers and seas, but their preference is for stagnant pools of water.")
                        historian.anim("vm_natural_historian_leech_twist")
                        npc<Happy>("One of the most common misconceptions is that all leeches feed on blood.")
                        npc<Happy>("In fact, very, very few leeches are parasitic bloodsucking animals.")
                        historian.anim("vm_natural_historian_leech_twist")
                        npc<Happy>("Most leeches are meat eaters, feeding on a variety of invertebrates such as worms, snails, insect larvae and snails.")
                        npc<Happy>("Those that do feed on blood have developed an amazing method of doing so. Firstly they latch onto the skin using a ring of tiny teeth, before injecting their prey with an anaesthetic.")
                        npc<Happy>("Then they bite into the skin using a Y-shaped mouthpiece and introducing a chemical that stops the blood from clotting.")
                        historian.anim("vm_natural_historian_leech_twist")
                        npc<Happy>("They will then feed until they are completely full, sometimes doubling in size!")
                        npc<Happy>("Most leeches are very small, measuring no more than the length of your middle finger. An exception to these are the leeches of Morytania, which can reach the size of a dog.")
                        historian.anim("vm_natural_historian_leech_twist")
                        npc<Happy>("They are much more mobile than their smaller cousins and are able to jump rather high when attacking.")
                        npc<Happy>("Quite how these leeches came to be so big is something of a mystery.")
                        historian.anim("vm_natural_historian_leech_twist")
                        npc<Happy>("All we can assume is that there is some kind of environmental influence, which has governed their immense growth.")
                        npc<Happy>("And this concludes my short lecture on leeches. I hope you've enjoyed yourselves.")
                    }
                }
                option("Tell me about moles.") {
                    npc<Happy>("Ahh moles, the mammalian mountain makers.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1736, 4959), Direction.NORTH, Delta(2, 5), npcs = { cutscene ->
                        listOf(NPCs.add("mole_display", cutscene.tile(1735, 4954)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_mole_tunnel")
                        npc<Happy>("Now, moles are small mammals of the talpidae family. These subterranean burrowers mainly live on a diet of slugs, snails and insects.")
                        npc<Happy>("They vary greatly in habitat and can be found in almost every part of Gielinor. Some species have even known to be aquatic!")
                        historian.anim("vm_natural_historian_mole_tunnel")
                        npc<Happy>("Male moles are known as boars with the females called sows. Should you come across a group of moles, you would call them a labour.")
                        npc<Happy>("Moles are considered to be an agricultural pest in most places, digging up the ground and leaving molehills all over the place.")
                        historian.anim("vm_natural_historian_mole_tunnel")
                        npc<Happy>("This has been highlighted in Falador by Wyson the Gardener who, after using some Malignus Mortifer's Super Ultra Flora Growth Potion, managed to create Gielinor's only known species of giant mole.")
                        npc<Happy>("This fearsome beast has huge claws, wicked teeth and a penchant for shiny objects. It is a very tough animal with a thick protective hide and an ill temperament.")
                        historian.anim("vm_natural_historian_mole_tunnel")
                        npc<Happy>("That said, they do benefit the soil by aerating and tilling it, adding to its fertility. Contrary to popular belief, moles don't eat plant roots.")
                        npc<Happy>("And this concludes my short lecture on moles. I hope you've enjoyed yourselves.")
                    }
                }
                option("Tell me about penguins.") {
                    npc<Happy>("Ahh penguins, the cunning birds of the sea.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1743, 4959), Direction.NORTH, Delta(-2, 5), npcs = { cutscene ->
                        listOf(NPCs.add("penguin_display", cutscene.tile(1742, 4954)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_penguin_waddle")
                        npc<Happy>("This often-maligned aquatic bird is known to be native to the ice fields of Etceteria, although they have been known to live as far as the fields of Lumbridge.")
                        npc<Happy>("These aquatic birds seem to spend the majority of their time eating and fostering their young.")
                        historian.anim("vm_natural_historian_penguin_waddle")
                        npc<Happy>("Unlike most animals that prefer cold climes, these sphenisciformes work very well together in large groups, by watching for predators and caring for each other's young.")
                        npc<Happy>("For creatures with such small brains, they do seem to have a disproportionate capacity for forward thinking and planning. As this serves no natural purpose, scholars are divided as to how this evolved.")
                        historian.anim("vm_natural_historian_penguin_waddle")
                        npc<Happy>("Their diet consists mainly of fish, squid and a small shrimp-like creature called krill. However, some have developed a taste for the mushrooms that grow around fairy rings.")
                        npc<Happy>("Penguins primarily rely on their vision while hunting. What we don't know is how penguins locate prey in the darkness, or at great depths.")
                        npc<Happy>("Some theories suggest that penguins are helped by some sort of extra sensory perception; perhaps even precognition.")
                        historian.anim("vm_natural_historian_penguin_waddle")
                        npc<Happy>("Penguins spend a long time going without food when they are breeding. In fact, they won't even leave their nests if they can help it.")
                        npc<Happy>("Fortunately, most penguins build up a layer of fat to keep them warm and provide energy until the moult is over.")
                        historian.anim("vm_natural_historian_penguin_waddle")
                        npc<Happy>("And this concludes my short lecture on penguins. I hope you've enjoyed yourselves.")
                    }
                }
                enoughEducation()
            }
        }

        npcOperate("Talk-to", "natural_historian_north") {
            npc<Happy>("Hello again, sir, how can I help you on this fine day?")
            player<Happy>("I was hoping you could tell me about something.")
            choice {
                option<Neutral>("Tell me about lizards.") {
                    npc<Happy>("Ahh lizards, the scaly carnivores.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1741, 4976), Direction.SOUTH, Delta(6, 0), "vm_button_2x1", npcs = { cutscene ->
                        listOf(NPCs.add("lizard_display", cutscene.tile(1740, 4979)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_lizard_hop")
                        npc<Happy>("Contrary to popular belief, even though most lizards have a yellow belly, they are not in fact cowardly. Most Gielinor lizards will not shy away from a good fight and can be very tough.")
                        npc<Happy>("This is due to a very thick and tough hide, made from thousands of thick scales. Most people who claim to be hunters have a very hard time trying to dispatch lizards.")
                        historian.anim("vm_natural_historian_lizard_hop")
                        npc<Happy>("In fact, the only people to successfully discern how to kill these tough little squamatas are the five legendary Slayer masters, although we assume they must have some kind of natural predator.")
                        npc<Happy>("Interestingly enough, these scales are made from the same substance that your hair is comprised of. This substance is called keratin.")
                        historian.anim("vm_natural_historian_lizard_hop")
                        npc<Happy>("Lizards have a very well developed sense of vision and hearing. Some people think that some lizards have a third eye!")
                        npc<Happy>("A tiny, light-sensitive, transparent structure on top of the head that helps them regulate how long they stay in the sun.")
                        historian.anim("vm_natural_historian_lizard_hop")
                        npc<Happy>("This is vital for the cold-blooded lizards who have no means to regulate their internal temperature.")
                        npc<Happy>("Like many cold-blooded creatures, if they are subjected to a sudden decrease in temperature, they will become sluggish and sleepy.")
                        npc<Happy>("And this concludes my short lecture on lizards. I hope you've enjoyed yourselves.")
                    }
                }
                option("Tell me about tortoises.") {
                    player<Neutral>("Tell me about the tortoises.")
                    npc<Happy>("Ahh tortoises, the armoured ancients.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1751, 4976), Direction.SOUTH, Delta(5, -1), "vm_button_2x1", npcs = { cutscene ->
                        listOf(NPCs.add("battle_tortoise_display", cutscene.tile(1750, 4979)), NPCs.add("lizard_display", cutscene.tile(1740, 4979)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("The tortoise is a very well defended beast. It uses an armoured shell just like its aquatic cousins the terrapin and the turtle.")
                        npc<Happy>("Tortoises can vary in size from about as long as your hand to as big as a unicorn. Most land tortoises eat nothing but plants in the wild.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("Did you know you can tell how old a tortoise is by the number of rings in its shell, just like a tree.")
                        npc<Happy>("Most land-based tortoises eat plants, feeding on grazing grasses, weeds, leafy greens, flowers, and cabbages.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("Tortoises generally live as long as people, and some individual ones are known to have lived longer than 300 years.")
                        npc<Happy>("Because of this, they symbolise longevity within some cultures, such as gnomes who also breed them for battle.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("The oldest tortoise ever recorded was Mibbiwocket, who was presented to the King Healthorg the Great, by the famous explorer Admiral Bake, shortly after its birth.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("Mibbiwocket is still in the care of the gnomish royal family.")
                        npc<Happy>("And this concludes my short lecture on tortoises. I hope you've enjoyed yourselves.")
                    }
                }
                option<Neutral>("Tell me about dragons.") {
                    npc<Happy>("Ahh dragons, the mighty hunters of the sky.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1766, 4976), Direction.SOUTH, Delta(-5, 0), "vm_button_2x1", npcs = { cutscene ->
                        listOf(NPCs.add("dragon_display", cutscene.tile(1765, 4979)), NPCs.add("wyvern_display", cutscene.tile(1775, 4979)))
                    }) { historian ->
                        npc<Happy>("The dragons of Gielinor are a most confusing species. Standing at approximately twelve feet tall, these imposing predators strike fear into the hearts and minds of most sane folk.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("However, if you delve a little deeper into their history and lifestyle, a few things stick out as very unusual.")
                        npc<Happy>("With most species there is a line, an ancestry if you will, whereby you can track how a creature has come to be in its present form.")
                        npc<Happy>("For instance, you can trace the ancestry of the common house cat back to the same creature that became the sabre-toothed kyatt.")
                        npc<Happy>("However, with the dragon, no such root ancestor can be found.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("There are many forms of dragon, such as the common coloured and the metallic, or ferrous, dragon.")
                        npc<Happy>("They colonise many areas of Gielinor, though most notably, sites of ancient battles and small dank caves.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("Eating habits tend to vary, with the majority of their food being meat. However, it has also been noted that they can consume metals just as easily, with runite being thought of as a delicacy.")
                        npc<Happy>("Throughout history, dragons have appeared in myth and legend as fearsome adversaries and cunning creatures.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("However, modern evidence does not support this. Most young dragons are largely creatures of instinct with a strong vicious streak.")
                        npc<Happy>("The lifespan of the common dragon is as yet unknown, as no dragon has ever been observed dying of old age.")
                        historian.anim("vm_natural_historian_battle_tortoise")
                        npc<Happy>("Although, it has been mooted that spontaneous combustion could be considered a natural cause of death for this species.")
                        npc<Happy>("And this concludes my short lecture on dragons. I hope you've enjoyed yourselves.")
                    }
                }
                option("Tell me about wyverns.") {
                    player<Neutral>("Tell me about the wyverns.")
                    npc<Happy>("Ahh, wyverns. The extinct lizards.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1776, 4976), Direction.SOUTH, Delta(-5, -1), "vm_button_2x1", npcs = { cutscene ->
                        listOf(NPCs.add("wyvern_display", cutscene.tile(1775, 4979)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_wyvern")
                        npc<Happy>("The wyverns' tale is a sad one. This now extinct species is presumed in some way to be related to the dragon.")
                        npc<Happy>("In that they are both large flying reptiles. The wyvern stands much shorter than the adult dragon and only has two legs as opposed to the dragon's four.")
                        historian.anim("vm_natural_historian_wyvern")
                        npc<Happy>("Much of the evidence for wyvern behaviour comes from the reconstruction of old bones in the icy mountains of Asgarnia.")
                        npc<Happy>("As most lizards cannot maintain their own body temperature, two theories as to how they managed to survive have been proposed.")
                        historian.anim("vm_natural_historian_wyvern")
                        npc<Happy>("One is that Asgarnia was at one time a much more temperate climate than it is now. The other is that wyverns could generate fire internally in much the same way as dragons.")
                        npc<Happy>("If they follow the dragon paradigm, then they would have been carnivores, feeding on cows, sheep and other livestock animals.")
                        historian.anim("vm_natural_historian_wyvern")
                        npc<Happy>("How and why the wyverns became extinct is something of a mystery. Though if you consider the theory that the climate of Asgarnia changed suddenly, then this could provide an explanation.")
                        npc<Happy>("There are some inconsistencies in the findings we have for the wyverns, such as the odd wear patterns of some of bones, which really could only have happened after the creature died.")
                        historian.anim("vm_natural_historian_wyvern")
                        npc<Happy>("Also the bones we have collected remain a little below room temperature wherever they are kept.")
                        npc<Happy>("They have also been shown to radiate a very weak magical aura.")
                        npc<Happy>("I'm sure that in due time, these mysteries will be solved.")
                        npc<Happy>("And this concludes my short lecture on wyverns. I hope you've enjoyed yourselves.")
                    }
                }
                enoughEducation()
            }
        }

        npcOperate("Talk-to", "natural_historian_east") {
            npc<Happy>("Hello again, sir, how can I help you on this fine day?")
            player<Happy>("I was hoping you could tell me about something.")
            choice {
                option<Neutral>("Tell me about snails.") {
                    npc<Happy>("Ahh snails, the gelatinous gastropods.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1775, 4961), Direction.SOUTH, Delta(2, -5), npcs = { cutscene ->
                        listOf(NPCs.add("snail_display", cutscene.tile(1774, 4964)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_snail_antenna")
                        npc<Happy>("Snails move like worms by squishing up and stretching out, very, very slowly. They also make a lot of slime, in order to aid moving by reducing friction.")
                        npc<Happy>("They also use slime for protection. For instance, snails can use their slime to crawl over razor-blades without being hurt. It also helps keep away dangerous insects like ants.")
                        historian.anim("vm_natural_historian_snail_antenna")
                        npc<Happy>("When they hide in their shells, snails secrete a special type of slime, which dries to cover the entrance of their shells like a 'trapdoor'. This is called an operculum.")
                        npc<Happy>("The snails of Morytania are the most malignant molluscs ever to have been studied.")
                        historian.anim("vm_natural_historian_snail_antenna")
                        npc<Happy>("They are broken down into two distinct species: achatina acidia and achatina acidia giganteus or, as they are more commonly known, the acid-spitting snail and the giant acid-spitting snail. ")
                        npc<Happy>("Both of these varieties are voracious carnivores, using their mutated mouthpieces to spit a glob of powerful acid to kill their foe.")
                        historian.anim("vm_natural_historian_snail_antenna")
                        npc<Happy>("They then simply have to wait, whilst the digestive juices make short work of the poor creature. Then, they simply slurp up what remains.")
                        npc<Happy>("How these strange creatures came to be is still something of a mystery. The most prevalent theory suggests that they mutated, as a reaction to an 'as yet unknown' pollutant that has appeared in the swamps.")
                        historian.anim("vm_natural_historian_snail_antenna")
                        npc<Happy>("The local populace has capitalised on the appearance of these strange species, using their shells to fashion a rudimentary helm that is fairly resistant to the snails acid.")
                        npc<Happy>("Other known uses of snail by-products include a tasty local delicacy and a fireproof oil.")
                        npc<Happy>("And this concludes my short lecture on snails. I hope you've enjoyed yourselves.")
                    }
                }
                option<Neutral>("Tell me about monkeys.") {
                    npc<Happy>("Ahh monkeys, the simian collective.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1775, 4959), Direction.NORTH, Delta(2, 4), npcs = { cutscene ->
                        listOf(NPCs.add("monkey_display", cutscene.tile(1774, 4954)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_monkey_hop")
                        npc<Happy>("A monkey is a member of either of two known groupings of simian primates. These two known groupings are the Karamja monkeys and the 'harmless' monkeys.")
                        npc<Happy>("Because of their similarity to monkeys, apes such as chimpanzees and gibbons are often called monkeys by accident.")
                        historian.anim("vm_natural_historian_monkey_hop")
                        npc<Happy>("Though some natural historians don't consider them to be monkeys. Also, a few monkey species have the word 'ape' in their common name.")
                        npc<Happy>("The Karamja monkeys are rumoured to be fairly cunning and intelligent creatures, although rumours that they have learned human speech is anecdotal at best.")
                        historian.anim("vm_natural_historian_monkey_hop")
                        npc<Happy>("In appearance, they stand much shorter than a human and tend to move in a hunched fashion. Karamja monkeys also sport a red mohawk, though it is unknown whether this is an affectation or not.")
                        npc<Happy>("They are very fond of bananas and bitternuts, eating them in huge quantities whenever they can get their paws on them.")
                        historian.anim("vm_natural_historian_monkey_hop")
                        npc<Happy>("The harmless monkeys of Mos Le'Harmless are a very similar, but in some ways entirely different, breed. They stand roughly the same size but are a lighter colour.")
                        npc<Happy>("Interestingly, Karamaja monkeys have a deep dislike of seaweed, though this may stem from the actions of a number of irresponsible people.")
                        npc<Happy>("And this concludes my short lecture on monkeys. I hope you've enjoyed yourselves.")
                    }
                }
                option("Tell me about sea slugs.") {
                    player<Neutral>("Tell me about the sea slugs.")
                    npc<Happy>("Ahh sea slugs, the cute crustaceans.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1782, 4959), Direction.NORTH, Delta(-3, 6), npcs = { cutscene ->
                        listOf(NPCs.add("sea_slugs_display", cutscene.tile(1781, 4954)))
                    }) { historian ->
                        historian.anim("vm_natural_historian_seaslug_twist")
                        npc<Happy>("The term 'sea slug' is something of a misnomer. Whilst these small creatures have a soft body like that of a slug, they also possess a very hard shell like a snail.")
                        npc<Happy>("Very little is actually known about the sea slug, as we have, as yet, been unable to procure a sample for observation and study.")
                        historian.anim("vm_natural_historian_seaslug_twist")
                        npc<Happy>("For some reason all expeditions we have sent have either vanished mysteriously, or those on the expedition have sent letters back announcing their desire to leave the Museum and go on to other things.")
                        npc<Happy>("It is presumed that the species is native to the very deep waters around the eastern Ardougne coastline.")
                        historian.anim("vm_natural_historian_seaslug_twist")
                        npc<Happy>("There must be some natural resource in the area that the sea slugs are using, as the underwater habitat there is much the same around many coastal areas on Gielinor.")
                        npc<Happy>("Through looking at similar species we have determined that the sea slug is a harmless little creature. It spends much of its life grazing on seaweed and other plant life.")
                        historian.anim("vm_natural_historian_seaslug_twist")
                        npc<Happy>("There are reports that these reclusive animals have two large fangs at their front, though this is assumed to be either for decorative or defensive purposes.")
                        npc<Happy>("If they do follow the same pattern as other similar creatures, the shell will be nigh on impervious to most attacks. The exposed soft skin may have a number of nematocysts, or stinging organs, similar to jellyfish.")
                        historian.anim("vm_natural_historian_seaslug_twist")
                        npc<Happy>("It is typical of prey animals such as these to develop some kind of unique defence mechanism that allows them to survive.")
                        npc<Happy>("If only we could acquire one for study. I'm sure we would find this mechanism to be truly unique.")
                        npc<Happy>("And this concludes my short lecture on sea slugs. I hope you've enjoyed yourselves.")
                    }
                }
                option<Neutral>("Tell me about snakes.") {
                    npc<Happy>("Ahh snakes, the slithering squamata.")
                    npc<Happy>("If you just follow me to the display case I shall explain all about them.")
                    cutscene(Tile(1782, 4961), Direction.SOUTH, Delta(-3, -5)) { historian ->
                        historian.anim("vm_natural_historian_snake_twist")
                        npc<Happy>("Serpentes, or ophidia, is the suborder under squamata that snakes belong to, like a big family.")
                        npc<Happy>("Unlike lizards, snakes have no limbs whatsoever; not that this limits them. Snakes live in or near every habitat in the world.")
                        historian.anim("vm_natural_historian_snake_twist")
                        npc<Happy>("Found in nice tropical forests, temperate lattitudes and even in the ocean - some have adapted specialised stomach juices that they inject into their prey as venom, while others prefer to grab and crush their food.")
                        npc<Happy>("While others still are fast hunters who use speed and strength to overcome their prey.")
                        historian.anim("vm_natural_historian_snake_twist")
                        npc<Happy>("The sense of smell in snakes has been enhanced in an amazing way. With most animals, like you and me, tiny particles are filtered through the nose.")
                        npc<Happy>("However, instead of using just their nose, these animals use their tongues as well. When a lizard or a snake wants to smell it's surroundings, it will wave its tongue around and pick up the particles in the air.")
                        historian.anim("vm_natural_historian_snake_twist")
                        npc<Happy>("The tongue then returns to the mouth and the tips of the tongue are pushed up against two tiny pits in the roof of the snake's mouth.")
                        npc<Happy>("Since these pits are split apart from each other, the tongue itself also has to split. This is why snakes have forked tongues.")
                        historian.anim("vm_natural_historian_snake_twist")
                        npc<Happy>("So the next time you see a snake sticking it's tongue out at you, remember, it's sniffing the air, not trying to bite you.")
                        npc<Happy>("And this concludes my short lecture on snakes. I hope you've enjoyed yourselves.")
                    }
                }
                enoughEducation()
            }
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
                    cutscene(Tile(1757, 4939), Direction.EAST, Delta(5, 5), npcs = { cutscene ->
                        listOf(NPCs.add("terrorbird_display", cutscene.tile(1752, 4938)))
                    }) { historian ->
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
                    cutscene(Tile(1760, 4939), Direction.WEST, Delta(-5, 5)) { historian ->
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
                enoughEducation()
            }
        }
    }

    private fun ChoiceOption.enoughEducation() {
        option<Bored>("That's enough education for one day.") {
            npc<Happy>("Nonsense! There's always room for more.")
            npc<Happy>("And remember, science isn't dull!")
        }
    }

    private suspend fun Player.cutscene(tile: Tile, direction: Direction, cameraOffset: Delta, button: String = "vm_button_1x1", npcs: (Cutscene) -> List<NPC> = { emptyList() }, objects: (Cutscene) -> List<GameObject> = { emptyList() }, block: suspend Player.(NPC) -> Unit) {
        open("fade_out")
        delay(3)
        minimap(Minimap.HideMap)
        closeTabs(Tab.Options)
        val cutscene = startCutscene("historian", Region(6989))
        val button = GameObjects.add(button, cutscene.convert(tile.add(direction.inverse()).add(direction.inverse())))
        val npcs = npcs.invoke(cutscene)
        val objects = objects.invoke(cutscene)
        var plaqueTile = tile.add(direction.inverse()).add(direction.inverse()).add(direction.rotate(-2))
        if (button.id == "vm_button_2x1") {
            plaqueTile = plaqueTile.add(direction.rotate(-2))
        }
        val plaque = GameObjects.add("vm_plaque_inactive", cutscene.convert(plaqueTile), rotation = direction.inverse().rotation())
        val historian = NPCs.add("vm_natural_historian_cutscene", cutscene.convert(tile.add(direction.inverse())))
        val north = NPCs.add(randomVisitor(), cutscene.convert(tile.add(direction).add(direction.rotate(2))))
        val south = NPCs.add(randomVisitor(north.id), cutscene.convert(tile.add(direction).add(direction.rotate(-2))))
        north.mode = PauseMode
        south.mode = PauseMode
        val start = this.tile
        cutscene.onEnd {
            tele(start)
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
        moveCamera(tile = historian.tile.add(cameraOffset.x, cameraOffset.y), height = 500, speed = 100, acceleration = 100)
        turnCamera(tile = historian.tile, height = 300, speed = 100, acceleration = 100)
        block(historian)
        open("fade_out")
        delay(3)
        cutscene.end()
        tele(tile)
    }

    private fun randomVisitor(exception: String? = null): String {
        val set = mutableSetOf("teacher_and_pupil", "schoolboy", "schoolgirl", "teacher_and_pupil_2")
        set.remove(exception)
        return set.random(random) // TODO unknown npcs without options
    }
}
