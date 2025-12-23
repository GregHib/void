package world.gregs.voidps.cache.type.decode

import world.gregs.voidps.cache.type.TypeDecoder
import world.gregs.voidps.cache.type.types.NPCType

class NPCTypeDecoder(count: Int, val members: Boolean = true) : TypeDecoder<NPCType>(count) {
    // Custom values
    override val id = int("id", default = -1, opcode = 250)
    override val stringId = string("[section]", default = "", opcode = 251)
    // Original values
    val modelIds = ushortArray("model_ids", opcode = 1)
    val name = string("name", default = "null", opcode = 2)
    val size = ubyte("size", default = 1, opcode = 12)
    val options = indexedStringArray("options", default = arrayOf(null, null, null, null, null, "Examine"), opcodes = 30..34)
    val colours = shortArrays("original_colours", "modified_colours", opcode = 40)
    val textures = shortArrays("original_textures", "modified_textures", opcode = 41)
    val recolourPalette = byteArray("recolour_palette", opcode = 42)
    val dialogueModels = ushortArray("dialogue_models", opcode = 60)
    val drawMinimapDot = bool("draw_minimap_dot", default = true, literal = false, opcode = 93)
    val combat = short("combat", default = -1, opcode = 95)
    val scaleXY = short("scale_xy", default = 128, opcode = 97)
    val scaleZ = short("scale_z", default = 128, opcode = 98)
    val priorityRender = bool("priority_render", default = false, literal = true, opcode = 99)
    val lightModifier = byte("light_modifier", default = 0, opcode = 100)
    val shadowModifier = byte("shadow_modifier", default = 0, opcode = 101)
    val headIcon = short("head_icon", default = -1, opcode = 102)
    val rotation = short("rotation", default = 32, opcode = 103)
    val transforms = transforms(firstOpcode = 106, lastOpcode = 118)
    val clickable = bool("clickable", default = true, literal = false, opcode = 107)
    val slowWalk = bool("slow_walk", default = true, literal = false, opcode = 109)
    val animateIdle = bool("animate_idle", default = true, literal = false, opcode = 111)
    val shadowColour = pair(short("primary_shadow_colour", default = 0), short("secondary_shadow_colour", default = 0), opcode = 113)
    val shadowModifiers = pair(byte("primary_shadow_modifier", default = 0), byte("secondary_shadow_modifier", default = 0), opcode = 114)
    val walkMode = byte("walk_mode", default = 0, opcode = 119)
    val translations = translate("translations", modelIds, opcode = 121)
    val hitBarSprite = short("hitbar_sprite", default = -1, opcode = 122)
    val height = short("height", default = -1, opcode = 123)
    val respawnDirect = byte("respawn_direction", default = 4, opcode = 125)
    val renderEmote = byte("render_emote", default = -1, opcode = 127)
    val opcode128 = ubyte("opcode_128", default = -1, opcode = 128)
    val sounds = quin(
        short("idle_sound", default = -1),
        short("crawl_sound", default = -1),
        short("walk_sound", default = -1),
        short("run_sound", default = -1),
        ubyte("sound_distance", default = 0),
        opcode = 134
    )
    val primaryCursor = pair(ubyte("primary_cursor_opcode", default = -1), short("primary_cursor", default = -1), opcode = 127)
    val secondaryCursor = pair(ubyte("secondary_cursor_opcode", default = -1), short("secondary_cursor", default = -1), opcode = 128)
    val attackCursor = short("attack_cursor", default = -1, opcode = 137)
    val armyIcon = short("army_icon", default = -1, opcode = 138)
    val spriteId = short("sprite_id", default = -1, opcode = 138)
    val ambientSoundVolume = ubyte("ambient_sound_volume", default = 255, opcode = 140)
    val visiblePriority = bool("visible_priority", default = false, literal = true, opcode = 141)
    val mapFunction = short("map_function", default = -1, opcode = 142)
    val invisiblePriority = bool("insivible_priority", default = false, literal = true, opcode = 143)
    init {
        register(options, opcodes = 150..154)
    }
    val hsl = quad(
        byte("hue", default = 0),
        byte("saturation", default = 0),
        byte("lightness", default = 0),
        byte("opacity", default = 0),
        opcode = 155
    )
    val mainOptionIndex = byte("main_option_index", default = -1, literal = 1, opcode = 158)
    val mainOptionIndex2 = byte("main_option_index", default = -1, literal = 0, opcode = 159)
    val campaigns = shortArray("campaigns", opcode = 160)
    val vorbis = bool("vorbis", default = false, literal = true, opcode = 162)
    val slayerType = ubyte("slayer_type", default = -1, opcode = 163)
    val soundRate = pair(short("sound_rate_min", default = 256), short("sound_rate_max", default = 256), opcode = 164)
    val pickSizeShift = ubyte("pick_size_shift", default = 0, opcode = 165)
    val soundRangeMin = ubyte("sound_range_min", default = 0, opcode = 168)
    val params = params(opcode = 249) {
        // TODO
    }

    override val active = setOf(name, size, options, combat, transforms, walkMode, renderEmote, sounds, stringId, params)

    override fun create(index: Int) = NPCType(
        id = index,
        name = name.get(index),
        size = size.get(index),
        options = options.get(index),
        combat = combat.get(index).toInt(),
        varbit = transforms.getVarbit(index),
        varp = transforms.getVarp(index),
        transforms = transforms.getTransforms(index),
        walkMode = walkMode.get(index),
        renderEmote = renderEmote.get(index).toInt(),
        idleSound = sounds.first.get(index).toInt(),
        crawlSound = sounds.second.get(index).toInt(),
        walkSound = sounds.third.get(index).toInt(),
        runSound = sounds.fourth.get(index).toInt(),
        soundDistance = sounds.fifth.get(index),
        stringId = stringId.get(index),
        extras = params.getMap(index),
    )

    override fun load(type: NPCType) {
        name.set(type.id, type.name)
        size.set(type.id, type.size)
        options.set(type.id, type.options)
        combat.set(type.id, type.combat.toShort())
        walkMode.set(type.id, type.walkMode)
        renderEmote.set(type.id, type.renderEmote.toByte())
        sounds.first.set(type.id, type.idleSound.toShort())
        sounds.second.set(type.id, type.crawlSound.toShort())
        sounds.third.set(type.id, type.walkSound.toShort())
        sounds.fourth.set(type.id, type.runSound.toShort())
        sounds.fifth.set(type.id, type.soundDistance)
        stringId.set(type.id, type.stringId)
        params.setMap(type.id, type.extras)
    }

}