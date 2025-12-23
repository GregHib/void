package world.gregs.voidps.cache.type.decode

import world.gregs.voidps.cache.definition.data.ObjectDefinition.Companion.PROJECTILE
import world.gregs.voidps.cache.definition.data.ObjectDefinition.Companion.ROUTE
import world.gregs.voidps.cache.type.TypeDecoder
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.ValueField
import world.gregs.voidps.cache.type.types.ObjectType

class ObjectTypeDecoder(
    val isMembers: Boolean = true,
    val lowDetail: Boolean = false,
    count: Int,
) : TypeDecoder<ObjectType>(count) {
    // Custom values
    override val id = int("id", default = -1, opcode = 250)
    override val stringId = string("[section]", default = "", opcode = 251)

    // Original values
    val models: ValueField<IntArray> = TODO("")
    val name = string("name", default = "null", opcode = 2)
    val sizeX = ubyte("size_x", default = 1, opcode = 14)
    val sizeY = ubyte("size_y", default = 1, opcode = 15)
    val blocks = byte("blocks", default = (ObjectType.PROJECTILE or ObjectType.ROUTE).toByte(), literal = ObjectType.PROJECTILE.toByte(), opcode = 17)

    val opcode17 = bool(default = false, literal = true, opcode = 17)
    val opcode18 = bool(default = false, literal = true, opcode = 18)

    val blocksSky = bool("blocks_sky", default = true)
    val block = int("block", default = ObjectType.PROJECTILE or ObjectType.ROUTE)

    val interactive = ubyte("interactive", default = -1, opcode = 19)
    val contouredGround = byte("contoured_ground", default = 0, literal = 1, opcode = 21)
    val delayShading = bool("delay_shading", default = false, literal = true, opcode = 22)
    val culling = byte("culling", default = -1, literal = 1, opcode = 23)
    val animationsLength = short("animation_length", default = 0, opcode = 24)
    val solid = byte("solid", default = 2, literal = 1, opcode = 27)
    val offsetMultiplier = ubyte("offset_multiplier", default = 64, opcode = 28)
    val brightness = byte("brightness", default = 0, opcode = 29)
    val options = stringArray("options", opcode = 30) // TODO null by default?
    init {
        registerField(31, options)
        registerField(32, options)
        registerField(33, options)
        registerField(34, options)
    }
    val contrast = byte("contrast", default = 0, opcode = 39)
    val colours = shortArrays("original_colours", "modified_colours", opcode = 40)
    val textures = shortArrays("original_textures", "modified_textures", opcode = 41)
    val recolourPalette = byteArray("recolour_palette", opcode = 42)
    val mirrored = bool("mirrored", default = false, literal = true, opcode = 62)
    val castsShadow = bool("casts_shadow", default = true, literal = false, opcode = 64)
    val modelSizeX = short("model_size_x", default = 128, opcode = 65)
    val modelSizeZ = short("model_size_Z", default = 128, opcode = 66)
    val modelSizeY = short("model_size_Y", default = 128, opcode = 67)
    val blockFlag = ubyte("block_flag", default = 0, opcode = 69)
    val offsetX = ushort("offset_x", default = 0, opcode = 70)
    val offsetZ = ushort("offset_z", default = 0, opcode = 71)
    val offsetY = ushort("offset_y", default = 0, opcode = 72)
    val ignoreOnRoute = bool("ignore_on_route", default = false, literal = true, opcode = 74)
    val supportItems = ubyte("support_items", default = -1, opcode = 75)
    val transforms = transforms(firstOpcode = 77, lastOpcode = 92)
    val unknownPair = pair(short("op_78_short", default = -1), ubyte("op_78_byte", default = 0), opcode = 78)
    val unknownQuad = quad(short("op_79_short_1", default = 0), short("op_79_short_2", default = 0), ubyte("op_79_byte", default = 0), shortArray("op_79_short_array"), opcode = 79)
    val unknownByte = ubyte("op_81_byte", default = -1, opcode = 81)
    val hideMinimap = bool("hide_minimap", default = false, literal = true, opcode = 82)
    val unknownBool = bool("op_88_bool", default = true, literal = false, opcode = 88)
    val animateInstantly = bool("animate_instantly", default = true, literal = false, opcode = 89)
    val members = bool("members", default = false, literal = true, opcode = 89)
    val unknownShort = short("op_93_short", default = -1, opcode = 93) // FIXME these are all anInt3023
    val contouredGround2 = short("controuted_ground", default = -1, literal = 4, opcode = 94) // FIXME these are all contouredGround
    val unknownUShort = short("op_95_short", default = -1, opcode = 95) // FIXME these are all anInt3023
    val unknownBoolean2 = bool("op_97_bool", default = false, literal = true, opcode = 97)
    val unknownBoolean3 = bool("op_98_bool", default = false, literal = true, opcode = 98)
    val unknownPair2 = pair(ubyte("op_99_ubyte", default = -1), short("op_99_short", default = -1), opcode = 99) // TODO primary cursor?
    val unknownPair3 = pair(ubyte("op_100_ubyte", default = -1), short("op_100_short", default = -1), opcode = 100)
    val unknownUByte = ubyte("op_101_ubyte", default = 0, opcode = 101)
    val mapScene = ubyte("map_scene", default = -1, opcode = 102)
    val culling2 = byte("culling", default = -1, literal = 0, opcode = 103) // FIXME
    val unknownUByte2 = ubyte("op_104_ubyte", default = 255, opcode = 104)
    val invertMapScene = bool("invert_map_scene", default = false, literal = true, opcode = 105)
    val animationPercents = intArray("animations", opcode = 106)// TODO
    val mapType = short("map_type", default = -1, opcode = 107)

    init {
        register(options, opcodes = 150..154)
    }

    val unknownShortArray = shortArray("op_160_short_array", opcode = 160)
    val unknownInt = int("op_162_int", default = -1, opcode = 162) // FIXME contouredGround
    val unknownQuad2 = quad(
        byte("op_163_byte_1", default = 0),
        byte("op_163_byte_2", default = 0),
        byte("op_163_byte_3", default = 0),
        byte("op_163_byte_4", default = 0),
        opcode = 163,
    )
    val unknownUShort2 = ushort("op_164_ushort", default = -1, opcode = 164)
    val unknownUShort3 = ushort("op_165_ushort", default = -1, opcode = 165)
    val unknownUShort4 = ushort("op_166_ushort", default = -1, opcode = 166)
    val unknownShort2 = ushort("op_167_short", default = -1, opcode = 167)
    val unknownBool2 = bool("op_168_bool", default = false, literal = true, opcode = 168)
    val unknownBool3 = bool("op_169_bool", default = false, literal = true, opcode = 169)
    val unknownSmart = smart("op_170_smart", default = 960, opcode = 170)
    val unknownSmart2 = smart("op_170_smart", default = -1, opcode = 171)
    val unknownShortPair = pair(
        short("op_173_short_1", default = 256),
        short("op_173_short_2", default = 256),
        opcode = 173
    )
    val unknownBool4 = bool("op_177_bool", default = false, literal = true, opcode = 177)
    val unknownUByte3 = ubyte("op_178_ubyte", default = 0, opcode = 178)
    val params = params(opcode = 249) {
        // TODO
    }

    override val active: Set<Field>
        get() = super.active

    override fun create(index: Int): ObjectType {
        var block = block.get(index)
        if (opcode17.get(index) || opcode18.get(index)) {
            block = block and PROJECTILE.inv()
        }
        if (ignoreOnRoute.get(index)) {
            block = block and ROUTE.inv()
        }
        return ObjectType(
            id = index,
            name = name.get(index),
            sizeX = sizeX.get(index),
            sizeY = sizeY.get(index),
            interactive = interactive.get(index),
            solid = if (opcode17.get(index)) 0 else solid.get(index).toInt(),
            block = block,
            //        options = options.get(index), FIXME
            mirrored = mirrored.get(index),
            blockFlag = blockFlag.get(index),
            varbit = transforms.getVarbit(index),
            varp = transforms.getVarp(index),
            transforms = transforms.getTransforms(index),
            stringId = stringId.get(index),
            extras = params.getMap(index),
        )
    }

    override fun load(type: ObjectType) {
        name.set(type.id, type.name)
        sizeX.set(type.id, type.sizeX)
        sizeY.set(type.id, type.sizeY)
        if (solid.get(type.id) == 0.toByte()) {
            opcode17.set(type.id, true)
        } else if (block.get(type.id) and ObjectType.PROJECTILE != 0) { // TODO check
            opcode18.set(type.id, true)
        }
        solid.set(type.id, type.solid.toByte())
        interactive.set(type.id, type.interactive)
//        options.set(type.id, type.options) FIXME
        mirrored.set(type.id, type.mirrored)
        blockFlag.set(type.id, type.blockFlag)
        transforms.setVarbit(type.id, type.varbit)
        transforms.setVarp(type.id, type.varp)
        transforms.setTransforms(type.id, type.transforms)
        stringId.set(type.id, type.stringId)
        params.setMap(type.id, type.extras)
        blocks.set(type.id, type.block.toByte())
    }
}