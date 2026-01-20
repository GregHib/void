package world.gregs.voidps.cache.type

import world.gregs.voidps.cache.definition.data.NPCDefinitionFull

class NpcType(override val id: Int = -1) : Type, Params {
    var name: String = "null"
        internal set
    var size: Int = 1
        internal set
    var options: Array<String?> = arrayOf(null, null, null, null, null, "Examine")
        internal set
    var combat: Int = -1
        internal set
    var varbit: Int = -1
        internal set
    var varp: Int = -1
        internal set
    var transforms: IntArray? = null
        internal set
    var walkMode: Byte = 0
        internal set
    var renderEmote: Int = -1
        internal set
    var idleSound: Int = -1
        internal set
    var crawlSound: Int = -1
        internal set
    var walkSound: Int = -1
        internal set
    var runSound: Int = -1
        internal set
    var soundDistance: Int = 0
        internal set
    override var stringId: String = ""
    override var params: Map<Int, Any>? = null

    constructor(definition: NPCDefinitionFull) : this(definition.id) {
        name = definition.name
        size = definition.size
        options = definition.options
        combat = definition.combat
        varbit = definition.varbit
        varp = definition.varp
        transforms = definition.transforms
        walkMode = definition.walkMode
        renderEmote = definition.renderEmote
        idleSound = definition.idleSound
        crawlSound = definition.crawlSound
        walkSound = definition.walkSound
        runSound = definition.runSound
        soundDistance = definition.soundDistance
    }

    override fun toString(): String {
        return "NpcType(id=$id, name='$name', size=$size, options=${options.contentToString()}, combat=$combat, varbit=$varbit, varp=$varp, transforms=${transforms.contentToString()}, walkMode=$walkMode, renderEmote=$renderEmote, idleSound=$idleSound, crawlSound=$crawlSound, walkSound=$walkSound, runSound=$runSound, soundDistance=$soundDistance, stringId='$stringId', params=$params)"
    }
}