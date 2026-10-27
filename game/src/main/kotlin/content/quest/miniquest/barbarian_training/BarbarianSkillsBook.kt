package content.quest.miniquest.barbarian_training

import content.entity.player.modal.book.Books
import content.quest.miniquest.barbarian_training.BarbarianTraining.REPORTED
import content.quest.miniquest.barbarian_training.BarbarianTraining.STARTED
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * The journal fills in with each of Otto's lessons as they're told, and their follow-up once reported back.
 */
class BarbarianSkillsBook(books: Books) : Script {

    // Section, heading, instructions and what Otto says once it's done
    val entries = listOf(
        Triple("rod", "Otto's words on Fishing with rods", "While you civilised folk use small, weak fishing rods, we barbarians are skilled with heavier tackle. We fish in the lake nearby. Take the rod from under my bed in my dwelling and fish in the lake. When you have caught a few fish I am sure you will be able to talk more with me. You will know when you are ready since my inspiration will fill your mind. We do not use these fish quite as you might expect. When you return from Fishing we can speak of this matter.") to "Your mind is as clear as the waters you have fished. This is good. These are fish that are fat with eggs rather than fat with flesh; this is what we will make use of.",
        Triple("barehand", "Otto's words on Fishing without harpoons", "First you must know more of harpoons with fish that are usually caught with such a device. You must catch fish which are usually harpooned, without a harpoon. You will be using your skill and strength. Use your arm as bait. Wriggle you fingers as if they are a tasty snack and hungry tuna and swordfish will throng to be caught by you. The method also works with shark - though in this case the action must be more of a frenzied thrashing of the arm than wiggling.") to "I sense you have more understanding of spears through your studies.",
        Triple("bow", "Otto's words on Firemaking", "The first point in your progression is that of lighting fires without a tinderbox. For this process you will require a strung bow. You use the bow to quickly rotate pieces of wood against one another. As you rub the wood becomes hot, eventually springing into flame. The spirits will aid you, the power they supply you will guide your hands. Go benefit from their guidance upon an oaken log.") to "Firemaking with your bow worked - fine news indeed, secrets our spirit boats now await your attention.",
        Triple("herblore", "Otto's words on potion enhancement", "If you use a knife upon a fat fish, several new fishy items will be produced. Fish parts can be used as bait; the roe or caviar is more useful for us. Mixing these items with two dose potions is what should be performed. This results in nutritious slop, perfect for healing as well as imparting the effect of the potion. Roe can be used for some more easily combined mixes, while caviar may be used for any mixes of which I am aware. You will discover you will be able to decant a four dose potion into an empty vial, thus giving two potions of two doses. This will aid you in the process. In this case I in fact require a potion for my own stocks. Bring a lesser attack potion combined with fish roe.") to "I see you have my potion. I will say no more than that I am eternally grateful.",
        Triple("spear", "Otto's words on Smithing spears", "Many warriors complain that spears are difficult to find, we barbarians thus forge our own. If you use our special barbarian anvil here, you will find it ideal. Other forges are not sturdy enough or for the forging work involved. Make any of our spears and return. Note well that you will require wood for the spear shafts, the quality of the wood must be similar to that of the metal involved.") to "The manufacture of spears is now yours as a speciality. Use your skill well. In addition, I am ready to reveal more spear-related crafts.",
        Triple("pyre", "Otto's words on Crafting pyre ships", "The next step is quite complex so listen well. In order to send our ancestors into the spirit world, their mortal remains must be burned with due ceremony. This can only be performed close to the water on the shore of the lake, just to our north-east. You will recognise the correct places by the ashes to be seen there. You will need to construct a small ship with logs in this area, the add the bones of a long dead barbarian hero. From the caverns beneath this lake. Many of our ancestors travelled to these caverns in order to hunt for glory and only found death. Their bones must still lie inside, their spirits trapped in torment. The spirit will ascend to glory, the pyre will send the earthly remains to the depths. You will also obtain a closer link to the spirit world. During this heightened contact, any bones you bury will have increased importance to the gods. The number of bones buried, before the link fades, is increased with the difficulty of obtaining the wood which you use. I have little knowledge of the caverns, they are blocked from the sight of spirits with whom I commune. I can only suspect that whatever slew barbarians heroes is indeed mighty. I also suggest that these bones may be very uncommon, since heroes are not found in vast numbers. Good luck. Dive into the whirlpool in the lake to the east. The spirits will use their abilities to ensure you arrive in the correct location. Though their influence fades so you must find your own way out.") to "",
        Triple("hasta", "Otto's words on one-handed spears", "The next step up is to manufacture a one handed version of a spear, suitable for combat. Such a spear is known as a hasta. As you might suspect, our ways require greater understanding than is gained by looking at a weapon. It is also the case that the process involves a differently balanced spear. Before you use such a weapon in anger you must make an example. Only then will you understand the poise of the techniques involved. You may use our special anvil for this spear type too. As the ways of black and dragon are beyond our knowledge, however, these spears may not be created.") to "I see you have constructed your hasta, and are approaching readiness to live life to its fullest - that you may be a peaceful spirit when your time ends.",
    )

    init {
        // TODO confirm the title, the long book and each lesson starting a new page (from 2009scape and rsmod)
        books.generate("barbarian_skills", "What Otto told me.", type = "long") { player -> pages(player) }
    }

    fun pages(player: Player): List<List<String>> {
        val lines = mutableListOf<String>()
        lines.addAll(wrap("I have noted down what Otto has told me in this journal, so that I may not forget my tasks. His instructions are thus faithfully recorded for posterity."))
        for ((entry, done) in entries) {
            val (section, heading, instructions) = entry
            val stage = player.barbarianTraining(section)
            if (stage < STARTED) {
                continue
            }
            // Each lesson starts on a fresh page
            while (lines.size % LINES_PER_SIDE != 0) {
                lines.add("")
            }
            lines.addAll(wrap(heading).map { "<navy>$it" })
            lines.addAll(wrap(instructions))
            if (stage == REPORTED && done.isNotEmpty()) {
                lines.add("")
                lines.addAll(wrap(done))
            }
        }
        return lines.chunked(LINES_PER_SIDE * 2)
    }

    companion object {
        private const val LINES_PER_SIDE = 15
        private const val LINE_LENGTH = 31

        fun wrap(text: String): List<String> {
            val lines = mutableListOf<String>()
            val line = StringBuilder()
            for (word in text.split(' ')) {
                if (line.isNotEmpty() && line.length + 1 + word.length > LINE_LENGTH) {
                    lines.add(line.toString())
                    line.clear()
                }
                if (line.isNotEmpty()) {
                    line.append(' ')
                }
                line.append(word)
            }
            if (line.isNotEmpty()) {
                lines.add(line.toString())
            }
            return lines
        }
    }
}
