package world.gregs.voidps.tools.definition.item.pipe.extra

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.tools.convert.ItemDecoder718

/**
 * Determines items [EquipType]
 */
class ItemTypes(val decoder: Array<ItemDefinition>) {

    val slots = ItemDecoder718.equipSlots
    private val types = ItemDecoder718.equipTypes

    private val sleeveless = arrayOf(
        "chainbody",
        "leather body",
        "hardleather body",
        "studded body",
        "d'hide body",
        "dragonhide body",
    )

    private val sleevelessExceptions = arrayOf(
        "dragon chainbody",
        "morrigan's leather body",
        "novite chainbody",
        "bathus chainbody",
        "marmaros chainbody",
        "kratonite chainbody",
        "fractite chainbody",
        "zephyrium chainbody",
        "argonite chainbody",
        "katagon chainbody",
        "gorgonite chainbody",
        "promethium chainbody",
    )

    private val fullFaceNames = arrayOf(
        "full",
        "decorative",
        "verac",
        "guthan",
        "fishbowl",
        "heraldic",
        "lunar",
        "tyras",
        "slayer",
        "cyclopean",
        "wildstalker",
        "trickster",
        "vanguard",
        "h'ween mask",
        "gas mask",
        "torag's helm",
        "frog mask",
        "zombie mask",
        "sallet",
        "jack lantern mask",
        "grim reaper hood",
        "acrobat hood",
        "christmas ghost hood",
        "ancient ceremonial",
        "constructor's hat",
        "pernix cowl",
        "headdress",
        "dervish head wrap",
        "saxon ringlet",
        "eastern knot",
        "samba headdress",
        "black ibis mask",
        "theatrical hat",
        "pharaoh nemes",
        "hati head",
        "changshan",
        "pagri",
        "megaleather coif",
        "tyrannoleather coif",
        "sagittarian coif",
        "salve hood",
        "wildercress hood",
        "blightleaf hood",
        "roseblood hood",
        "bryll hood",
        "duskweed hood",
        "soulbell hood",
        "ectohood",
        "runic hood",
        "spiritbloom hood",
        "celestial hood",
        "gorilla mask",
        "bedsheet",
        "spiny helmet",
        "desert disguise",
        "beret mask",
        "lizard skull",
        "fox mask",
        "unicorn mask",
        "dragon mask",
        "virtus mask",
    )

    private val fullFaceExceptions = arrayOf(
        "third-age",
        "statius",
    )

    private val maskNames = arrayOf(
        "masked earmuffs",
        "face mask",
        "fake beard",
        "fake moustache and nose",
        "fake monocle, moustache and nose",
    )

    private val hairNames = arrayOf(
        "coif",
        "helm",
        "hood",
        "afro",
        "bandana",
        "leather cowl",
        "wizard hat",
        "pink hat",
        "green hat",
        "blue hat",
        "cream hat",
        "turquoise hat",
        "blue hat",
        "snelm",
        "mystic hat",
        "h.a.m. hood",
        "bearhead",
        "lederhosen hat",
        "menap headgear",
        "mudskipper hat",
        "bobble hat",
        "jester hat",
        "tri-jester hat",
        "woolly hat",
        "progress hat",
        "camel mask",
        "enchanted hat",
        "hat and eyepatch",
        "pirate hat",
        "tricorn hat",
        "skeleton mask",
        "bomber cap",
        "cap and goggles",
        "kyatt hat",
        "larupia hat",
        "third-age mage hat",
        "wig",
        "sleeping cap",
        "mitre",
        "reindeer hat",
        "healer hat",
        "fighter hat",
        "rogue mask",
        "hard hat",
        "lumberjack hat",
        "chicken head",
        "cavalier and mask",
        "antlers",
        "davy kebbit hat",
        "sailor's hat",
        "sheep mask",
        "penguin mask",
        "bat mask",
        "cat mask",
        "wolf mask",
        "customs hat",
        "clown hat",
        "puffer",
        "octopus",
        "monkfish",
        "ray",
        "avalani's hat",
        "runecrafter hat",
    )

    private val hairExceptions = arrayOf(
        "robin hood",
        "cosmic helmet",
        "chaos helmet",
        "elemental helmet",
        "mind helmet",
        "body helmet",
        "ram skull helm",
        "camo helmet",
    )

    private fun isHair(name: String) = name.containsAny(hairNames) && !name.containsAny(hairExceptions)

    private fun isFullFace(name: String) = name.containsAny(fullFaceNames) && !name.containsAny(fullFaceExceptions)

    private fun isMask(name: String) = name.containsAny(maskNames)

    fun getHeadEquipType(name: String) = when {
        isFullFace(name) -> EquipType.FullFace
        isMask(name) -> EquipType.Mask
        isHair(name) -> EquipType.Hair
        else -> EquipType.None
    }

    fun isSleeveless(name: String) = name.containsAny(sleeveless) && !name.containsAny(sleevelessExceptions)

    private fun String.containsAny(names: Array<String>): Boolean = names.any { contains(it) }

    fun getEquipType(id: Int): EquipType {
        val def = decoder.getOrNull(id) ?: return EquipType.None
        val name = def.name.lowercase()
        val slot = slots[id]
        return when {
            slot == EquipSlot.Weapon.index && types[id] == 5 -> EquipType.TwoHanded
            slot == EquipSlot.Chest.index && isSleeveless(name) -> EquipType.Sleeveless
            slot == EquipSlot.Hat.index -> getHeadEquipType(name)
            else -> EquipType.None
        }
    }
}
