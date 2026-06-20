@file:Suppress("unused")

package world.gregs.voidps.tools.photobooth.vendor

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import kotlin.collections.forEach
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Kris | 13/02/2023
 */
data class Model(
    val id: Int,
) {
    var vertexCount = 0
        private set
    var triangleCount = 0
        private set
    var textureTriangleCount = 0
        private set
    var version = UNVERSIONED
        private set
    var type: MeshType = MeshType.VersionedSkeletal
        private set
    var renderPriority = 0
        private set

    var triangleColors: ShortArray? = null
        private set
    var triangleAlphas: IntArray? = null
        private set
    var triangleSkins: IntArray? = null
        private set
    var triangleRenderTypes: IntArray? = null
        private set
    var triangleRenderPriorities: IntArray? = null
        private set
    var triangleVertex1: IntArray? = null
        private set
    var triangleVertex2: IntArray? = null
        private set
    var triangleVertex3: IntArray? = null
        private set

    var vertexPositionsX: IntArray? = null
        private set
    var vertexPositionsY: IntArray? = null
        private set
    var vertexPositionsZ: IntArray? = null
        private set
    var vertexSkins: IntArray? = null
        private set
    var vertexPartMasks: ShortArray? = null
        private set

    var triangleTextures: IntArray? = null
        private set
    var textureRenderTypes: IntArray? = null
        private set
    var textureTriangleVertex1: IntArray? = null
        private set
    var textureTriangleVertex2: IntArray? = null
        private set
    var textureTriangleVertex3: IntArray? = null
        private set
    var textureScaleX: IntArray? = null
        private set
    var textureScaleY: IntArray? = null
        private set
    var textureScaleZ: IntArray? = null
        private set
    var textureRotation: IntArray? = null
        private set
    var textureDirection: IntArray? = null
        private set
    var textureSpeed: IntArray? = null
        private set
    var textureTransU: IntArray? = null
        private set
    var textureTransV: IntArray? = null
        private set
    var textureCoordinates: IntArray? = null
        private set

    var skeletalBones: Array<IntArray?>? = null
        private set
    var skeletalScales: Array<IntArray?>? = null
        private set

    var emitters: Array<EmissiveTriangle>? = null
        private set
    var effectors: Array<EffectiveVertex>? = null
        private set

    var faceBillboards: Array<FaceBillboard>? = null
        private set

    fun version(): Int {
        return this.version
    }

    fun resetVersion() {
        this.version = DEFAULT_VERSION
    }

    fun type(): MeshType {
        return this.type
    }

    fun setType(type: MeshType) {
        this.type = type
    }

    fun forcePriority(priority: Int) {
        this.renderPriority = priority
        this.triangleRenderPriorities = null
    }

    fun setFaceColor(
        face: Int,
        colour: Int,
    ) {
        if (face !in 0 until triangleCount) return
        val colors = requireNotNull(this.triangleColors)
        colors[face] = colour.toShort()
    }

    fun removeFaceTexture(face: Int) {
        if (face !in 0 until triangleCount) return
        if (this.triangleTextures == null) return
        this.triangleTextures!![face] = -1
        if (this.textureCoordinates != null && face < this.textureCoordinates!!.size) {
            this.textureCoordinates!![face] = -1
        }
        retainTextures { true }
    }

    fun setFaceTexture(
        face: Int,
        textureId: Int,
    ) {
        if (face !in 0 until triangleCount) return
        if (triangleTextures == null) {
            triangleTextures = IntArray(triangleCount) { -1 }
        }
        if (textureCoordinates == null) {
            textureCoordinates = IntArray(triangleCount) { -1 }
        }
        triangleTextures!![face] = textureId
        if (textureCoordinates!![face] == -1) {
            val coordinate = textureTriangleCount
            textureCoordinates!![face] = coordinate
            textureTriangleCount++
            textureRenderTypes = append(textureRenderTypes, SIMPLE_TEXTURE)
            textureTriangleVertex1 = append(textureTriangleVertex1, triangleVertex1!![face])
            textureTriangleVertex2 = append(textureTriangleVertex2, triangleVertex2!![face])
            textureTriangleVertex3 = append(textureTriangleVertex3, triangleVertex3!![face])
        }
    }

    fun setFaceTextureDirectionAndSpeed(
        face: Int,
        direction: Int,
        speed: Int,
    ) {
        if (face !in 0 until triangleCount) return
        val coordinate = textureCoordinateForFace(face)
        if (coordinate < 0) return
        ensureComplexTextureArrays()
        textureRenderTypes!![coordinate] = CYLINDRICAL_TEXTURE
        textureDirection!![coordinate] = direction
        textureSpeed!![coordinate] = speed
        textureScaleX!![coordinate] = if (textureScaleX!![coordinate] == 0) 128 else textureScaleX!![coordinate]
        textureScaleY!![coordinate] = if (textureScaleY!![coordinate] == 0) 128 else textureScaleY!![coordinate]
        textureScaleZ!![coordinate] = if (textureScaleZ!![coordinate] == 0) 128 else textureScaleZ!![coordinate]
        textureTriangleVertex1!![coordinate] = triangleVertex1!![face]
        textureTriangleVertex2!![coordinate] = triangleVertex2!![face]
        textureTriangleVertex3!![coordinate] = triangleVertex3!![face]
    }

    fun replaceEmitters(emitters: Array<EmissiveTriangle>?) {
        this.emitters = emitters
    }

    fun replaceBillboards(billboards: Array<FaceBillboard>?) {
        this.faceBillboards = billboards
    }

    private fun append(
        array: IntArray?,
        value: Int,
    ): IntArray {
        val size = array?.size ?: 0
        val copy = IntArray(size + 1)
        if (array != null) {
            array.copyInto(copy, endIndex = size)
        }
        copy[size] = value
        return copy
    }

    private fun ensureComplexTextureArrays() {
        val count = textureTriangleCount
        if (textureScaleX == null || textureScaleX!!.size < count) textureScaleX = textureScaleX?.copyOf(count) ?: IntArray(count)
        if (textureScaleY == null || textureScaleY!!.size < count) textureScaleY = textureScaleY?.copyOf(count) ?: IntArray(count)
        if (textureScaleZ == null || textureScaleZ!!.size < count) textureScaleZ = textureScaleZ?.copyOf(count) ?: IntArray(count)
        if (textureRotation == null || textureRotation!!.size < count) textureRotation = textureRotation?.copyOf(count) ?: IntArray(count)
        if (textureDirection == null || textureDirection!!.size < count) textureDirection = textureDirection?.copyOf(count) ?: IntArray(count)
        if (textureSpeed == null || textureSpeed!!.size < count) textureSpeed = textureSpeed?.copyOf(count) ?: IntArray(count)
        if (textureTransU != null && textureTransU!!.size < count) textureTransU = textureTransU!!.copyOf(count)
        if (textureTransV != null && textureTransV!!.size < count) textureTransV = textureTransV!!.copyOf(count)
    }

    fun textureCoordinateForFace(face: Int): Int {
        if (face !in 0 until triangleCount) return -1
        val coordinates = textureCoordinates ?: return -1
        if (face >= coordinates.size) return -1
        val coordinate = coordinates[face]
        if (coordinate < 0) return -1
        val normalized = coordinate and 0xFF
        return if (normalized in 0 until textureTriangleCount) normalized else -1
    }

    fun position(index: Int): VertexPoint {
        val posX = requireNotNull(this.vertexPositionsX)
        val posY = requireNotNull(this.vertexPositionsY)
        val posZ = requireNotNull(this.vertexPositionsZ)
        return VertexPoint(posX[index], posY[index], posZ[index])
    }

    fun updatePriority(
        old: Int,
        new: Int,
    ) {
        val priorities = this.triangleRenderPriorities ?: return
        for (i in priorities.indices) {
            val pri = priorities[i]
            if (pri == old) {
                priorities[i] = new
            }
        }
    }

    private fun computeCoordinateToFacesMap(): Map<Int, List<TexturedFace>> {
        val map = mutableMapOf<Int, MutableList<TexturedFace>>()
        for (i in 0 until triangleCount) {
            val coordinate = textureCoordinates!![i]
            if (coordinate == -1) continue
            val unsignedCoordinate = coordinate and 0xFF
            val texture = this.triangleTextures!![i]
            map.getOrPut(unsignedCoordinate, ::mutableListOf).add(TexturedFace(i, texture))
        }
        return map
    }

    private fun getTexturedFaces(): List<Texture> {
        if (textureTriangleCount == 0) return emptyList()
        val textures = mutableListOf<Texture>()
        val coordinateToFacesMap = computeCoordinateToFacesMap()

        for (coordinate in 0 until textureTriangleCount) {
            val faces = coordinateToFacesMap[coordinate] ?: emptyList()
            val type = this.textureRenderTypes!![coordinate]
            val vertex1 = this.textureTriangleVertex1!![coordinate]
            val vertex2 = this.textureTriangleVertex2!![coordinate]
            val vertex3 = this.textureTriangleVertex3!![coordinate]
            if (type == SIMPLE_TEXTURE) {
                textures +=
                    SimpleTexture(
                        faces,
                        type,
                        coordinate,
                        vertex1,
                        vertex2,
                        vertex3,
                    )
                continue
            }
            val scaleX = this.textureScaleX!![coordinate]
            val scaleY = this.textureScaleY!![coordinate]
            val scaleZ = this.textureScaleZ!![coordinate]
            val rotation = this.textureRotation!![coordinate]
            val direction = this.textureDirection!![coordinate]
            val speed = this.textureSpeed!![coordinate]
            if (type == CYLINDRICAL_TEXTURE) {
                textures +=
                    CylindricalTexture(
                        faces,
                        type,
                        coordinate,
                        vertex1,
                        vertex2,
                        vertex3,
                        scaleX,
                        scaleY,
                        scaleZ,
                        rotation,
                        direction,
                        speed,
                    )
                continue
            } else if (type == SPHERICAL_TEXTURE) {
                textures +=
                    SphericalTexture(
                        faces,
                        type,
                        coordinate,
                        vertex1,
                        vertex2,
                        vertex3,
                        scaleX,
                        scaleY,
                        scaleZ,
                        rotation,
                        direction,
                        speed,
                    )
                continue
            }
            val transU = this.textureTransU!![coordinate]
            val transV = this.textureTransV!![coordinate]
            require(type == CUBE_TEXTURE)
            textures +=
                CubeTexture(
                    faces,
                    type,
                    coordinate,
                    vertex1,
                    vertex2,
                    vertex3,
                    scaleX,
                    scaleY,
                    scaleZ,
                    rotation,
                    direction,
                    speed,
                    transU,
                    transV,
                )
        }
        return textures
    }

    fun retainTextures(predicate: TextureRetainPredicate) {
        if (textureTriangleCount == 0) return
        val textures = getTexturedFaces()
        val (retained, removed) = textures.partition(predicate)
        val cubeTextures = retained.count { it is CubeTexture }
        val complexTextures = retained.count { it is ComplexTexture }
        for (texture in removed) {
            for ((face, _) in texture.faces) {
                this.textureCoordinates!![face] = -1
                this.triangleTextures!![face] = -1
            }
        }
        this.textureTriangleCount = retained.size
        if (cubeTextures == 0) {
            this.textureTransU = null
            this.textureTransV = null
        }
        if (complexTextures == 0) {
            this.textureScaleX = null
            this.textureScaleY = null
            this.textureScaleZ = null
            this.textureRotation = null
            this.textureDirection = null
            this.textureSpeed = null
        }
        if (retained.isEmpty()) {
            this.textureRenderTypes = null
            this.textureTriangleVertex1 = null
            this.textureTriangleVertex2 = null
            this.textureTriangleVertex3 = null
            return
        }

        this.textureTriangleVertex1 = IntArray(textureTriangleCount)
        this.textureTriangleVertex2 = IntArray(textureTriangleCount)
        this.textureTriangleVertex3 = IntArray(textureTriangleCount)
        this.textureRenderTypes = IntArray(textureTriangleCount)
        if (complexTextures > 0) {
            this.textureScaleX = IntArray(textureTriangleCount)
            this.textureScaleY = IntArray(textureTriangleCount)
            this.textureScaleZ = IntArray(textureTriangleCount)
            this.textureRotation = IntArray(textureTriangleCount)
            this.textureDirection = IntArray(textureTriangleCount)
            this.textureSpeed = IntArray(textureTriangleCount)
            if (cubeTextures > 0) {
                this.textureTransU = IntArray(textureTriangleCount)
                this.textureTransV = IntArray(textureTriangleCount)
            }
        }
        for ((index, texture) in retained.withIndex()) {
            for ((face, _) in texture.faces) {
                this.textureCoordinates!![face] = index
            }
            this.textureRenderTypes!![index] = texture.renderType
            this.textureTriangleVertex1!![index] = texture.vertex1
            this.textureTriangleVertex2!![index] = texture.vertex2
            this.textureTriangleVertex3!![index] = texture.vertex3
            if (texture is ComplexTexture) {
                this.textureScaleX!![index] = texture.scaleX
                this.textureScaleY!![index] = texture.scaleY
                this.textureScaleZ!![index] = texture.scaleZ
                this.textureRotation!![index] = texture.rotation
                this.textureDirection!![index] = texture.direction
                this.textureSpeed!![index] = texture.speed
                if (texture is CubeTexture) {
                    this.textureTransU!![index] = texture.transU
                    this.textureTransV!![index] = texture.transV
                }
            }
        }
    }

    fun translatePoints(vararg points: Pair<VertexPoint, VertexPoint>) {
        val posX = requireNotNull(this.vertexPositionsX)
        val posY = requireNotNull(this.vertexPositionsY)
        val posZ = requireNotNull(this.vertexPositionsZ)
        for ((from, to) in points) {
            for (i in 0 until this.vertexCount) {
                val x = posX[i]
                val y = posY[i]
                val z = posZ[i]
                if (-from.x == x && -from.y == y && -from.z == z) {
                    posX[i] = -to.x
                    posY[i] = -to.y
                    posZ[i] = -to.z
                }
            }
        }
    }

    fun moveSeparatePartsCloserTogetherZAxis(
        splitPoint: Int,
        offset: Int,
    ) {
        val posZ = this.vertexPositionsZ ?: return
        for (i in posZ.indices) {
            val pos = posZ[i]
            if (pos < splitPoint) {
                posZ[i] += offset
            } else {
                posZ[i] -= offset
            }
        }
    }

    fun splitIndicesZAxis(splitPoint: Int): Pair<List<Int>, List<Int>> {
        val posZ = checkNotNull(this.vertexPositionsZ)
        val additions = mutableListOf<Int>()
        val subtractions = mutableListOf<Int>()
        for (i in posZ.indices) {
            val pos = posZ[i]
            if (pos < splitPoint) {
                additions += i
            } else {
                subtractions += i
            }
        }
        return additions to subtractions
    }

    fun removeTrianglesByColour(colour: Int) {
        val colours = this.triangleColors ?: return
        val indices = mutableSetOf<Int>()
        for (i in colours.indices) {
            val col = colours[i].toInt()
            if (col and 0xFFFF == colour and 0xFFFF) {
                indices += i
            }
        }
        this.triangleCount -= indices.size
        this.triangleVertex1 = removeIndices(triangleVertex1, indices)
        this.triangleVertex2 = removeIndices(triangleVertex2, indices)
        this.triangleVertex3 = removeIndices(triangleVertex3, indices)
        this.triangleColors = removeIndices(triangleColors, indices)
        this.triangleRenderTypes = removeIndices(triangleRenderTypes, indices)
        this.triangleRenderPriorities = removeIndices(triangleRenderPriorities, indices)
        this.textureCoordinates = removeIndices(textureCoordinates, indices)
        this.triangleTextures = removeIndices(triangleTextures, indices)
        this.triangleAlphas = removeIndices(triangleAlphas, indices)
        this.triangleSkins = removeIndices(triangleSkins, indices)
    }

    fun removeTrianglesByPriority(condition: (priority: Int) -> Boolean) {
        val priorities = this.triangleRenderPriorities ?: return
        val indices = mutableSetOf<Int>()
        for (i in priorities.indices) {
            val pri = priorities[i]
            if (condition(pri and 0xFFFF)) {
                indices += i
            }
        }
        this.triangleCount -= indices.size
        this.triangleVertex1 = removeIndices(triangleVertex1, indices)
        this.triangleVertex2 = removeIndices(triangleVertex2, indices)
        this.triangleVertex3 = removeIndices(triangleVertex3, indices)
        this.triangleColors = removeIndices(triangleColors, indices)
        this.triangleRenderTypes = removeIndices(triangleRenderTypes, indices)
        this.triangleRenderPriorities = removeIndices(triangleRenderPriorities, indices)
        this.textureCoordinates = removeIndices(textureCoordinates, indices)
        this.triangleTextures = removeIndices(triangleTextures, indices)
        this.triangleAlphas = removeIndices(triangleAlphas, indices)
        this.triangleSkins = removeIndices(triangleSkins, indices)
    }

    fun removeTrianglesBySkins(condition: (skin: Int) -> Boolean) {
        val skins = this.triangleSkins ?: return
        val indices = mutableSetOf<Int>()
        for (i in skins.indices) {
            val pri = skins[i]
            if (condition(pri)) {
                indices += i
            }
        }
        this.triangleCount -= indices.size
        this.triangleVertex1 = removeIndices(triangleVertex1, indices)
        this.triangleVertex2 = removeIndices(triangleVertex2, indices)
        this.triangleVertex3 = removeIndices(triangleVertex3, indices)
        this.triangleColors = removeIndices(triangleColors, indices)
        this.triangleRenderTypes = removeIndices(triangleRenderTypes, indices)
        this.triangleRenderPriorities = removeIndices(triangleRenderPriorities, indices)
        this.textureCoordinates = removeIndices(textureCoordinates, indices)
        this.triangleTextures = removeIndices(triangleTextures, indices)
        this.triangleAlphas = removeIndices(triangleAlphas, indices)
        this.triangleSkins = removeIndices(triangleSkins, indices)
    }

    fun removeFullyTransparentTriangles() {
        val alphas = this.triangleAlphas ?: return
        val indices = mutableSetOf<Int>()
        for (i in alphas.indices) {
            if (alphas[i] and 0xFF == 0xFF) {
                indices += i
            }
        }
        if (indices.isEmpty()) return

        this.triangleCount -= indices.size
        this.triangleVertex1 = removeIndices(triangleVertex1, indices)
        this.triangleVertex2 = removeIndices(triangleVertex2, indices)
        this.triangleVertex3 = removeIndices(triangleVertex3, indices)
        this.triangleColors = removeIndices(triangleColors, indices)
        this.triangleRenderTypes = removeIndices(triangleRenderTypes, indices)
        this.triangleRenderPriorities = removeIndices(triangleRenderPriorities, indices)
        this.textureCoordinates = removeIndices(textureCoordinates, indices)
        this.triangleTextures = removeIndices(triangleTextures, indices)
        this.triangleAlphas = removeIndices(triangleAlphas, indices)
        this.triangleSkins = removeIndices(triangleSkins, indices)
    }

    fun removeNonSolidTriangles() {
        val renderTypes = this.triangleRenderTypes ?: return
        val indices = mutableSetOf<Int>()
        for (i in renderTypes.indices) {
            if (renderTypes[i] >= 2) {
                indices += i
            }
        }
        if (indices.isEmpty()) return

        this.triangleCount -= indices.size
        this.triangleVertex1 = removeIndices(triangleVertex1, indices)
        this.triangleVertex2 = removeIndices(triangleVertex2, indices)
        this.triangleVertex3 = removeIndices(triangleVertex3, indices)
        this.triangleColors = removeIndices(triangleColors, indices)
        this.triangleRenderTypes = removeIndices(triangleRenderTypes, indices)
        this.triangleRenderPriorities = removeIndices(triangleRenderPriorities, indices)
        this.textureCoordinates = removeIndices(textureCoordinates, indices)
        this.triangleTextures = removeIndices(triangleTextures, indices)
        this.triangleAlphas = removeIndices(triangleAlphas, indices)
        this.triangleSkins = removeIndices(triangleSkins, indices)
    }

    fun addTrianglesByColour(
        from: Model,
        colour: Int,
    ) {
        val colours = from.triangleColors ?: return
        val indices = mutableSetOf<Int>()
        for (i in colours.indices) {
            val col = colours[i].toInt()
            if (col and 0xFFFF == colour and 0xFFFF) {
                indices += i
            }
        }
        this.triangleCount += indices.size
        this.triangleVertex1 += filterIndices(from.triangleVertex1, indices)
        this.triangleVertex2 += filterIndices(from.triangleVertex2, indices)
        this.triangleVertex3 += filterIndices(from.triangleVertex3, indices)
        this.triangleColors += filterIndices(from.triangleColors, indices)
        this.triangleRenderTypes += filterIndices(from.triangleRenderTypes, indices)
        this.triangleRenderTypes = fill(triangleRenderTypes, triangleCount, 0)
        this.triangleRenderPriorities += filterIndices(from.triangleRenderPriorities, indices)
        this.triangleRenderPriorities = fill(triangleRenderPriorities, triangleCount, if (colour == 0) 0 else 7)
        this.textureCoordinates += filterIndices(from.textureCoordinates, indices)
        this.triangleTextures += filterIndices(from.triangleTextures, indices)
        this.triangleAlphas += filterIndices(from.triangleAlphas, indices)
        this.triangleSkins += filterIndices(from.triangleSkins, indices)
    }

    private fun fill(
        array: IntArray?,
        expected: Int,
        fill: Int,
    ): IntArray? {
        if (array == null || array.size == expected) return null
        val list = array.toMutableList()
        while (list.size < expected) {
            list += fill
        }
        return list.toIntArray()
    }

    private operator fun IntArray?.plus(other: IntArray?): IntArray? {
        if (this == null) return other
        return (this.toList() + (other?.toList() ?: emptyList())).toIntArray()
    }

    private operator fun ShortArray?.plus(other: ShortArray?): ShortArray? {
        if (this == null) return other
        return (this.toList() + (other?.toList() ?: emptyList())).toShortArray()
    }

    private fun removeIndices(
        array: IntArray?,
        indices: Set<Int>,
    ): IntArray? {
        if (array == null) return null
        val newResult = mutableListOf<Int>()
        for (i in array.indices) {
            if (i in indices) continue
            newResult += array[i]
        }
        return newResult.toIntArray()
    }

    private fun removeIndices(
        array: ShortArray?,
        indices: Set<Int>,
    ): ShortArray? {
        if (array == null) return null
        val newResult = mutableListOf<Short>()
        for (i in array.indices) {
            if (i in indices) continue
            newResult += array[i]
        }
        return newResult.toShortArray()
    }

    private fun filterIndices(
        array: IntArray?,
        indices: Set<Int>,
    ): IntArray? {
        if (array == null) return null
        val newResult = mutableListOf<Int>()
        for (i in array.indices) {
            if (i !in indices) continue
            newResult += array[i]
        }
        return newResult.toIntArray()
    }

    private fun filterIndices(
        array: ShortArray?,
        indices: Set<Int>,
    ): ShortArray? {
        if (array == null) return null
        val newResult = mutableListOf<Short>()
        for (i in array.indices) {
            if (i !in indices) continue
            newResult += array[i]
        }
        return newResult.toShortArray()
    }

    fun setEmitters(vararg emitters: EmissiveTriangle) {
        this.emitters = emitters.toList().toTypedArray()
    }

    fun setEffectors(vararg effectors: EffectiveVertex) {
        this.effectors = effectors.toList().toTypedArray()
    }

    fun setVertexPositionByVertexIndex(
        index: Int,
        posX: Int,
        posY: Int,
        posZ: Int,
    ) {
        val triangleVertex1 = requireNotNull(this.triangleVertex1)
        val triangleVertex2 = requireNotNull(this.triangleVertex2)
        val triangleVertex3 = requireNotNull(this.triangleVertex3)
        val vertexPositionsX = requireNotNull(this.vertexPositionsX)
        val vertexPositionsY = requireNotNull(this.vertexPositionsY)
        val vertexPositionsZ = requireNotNull(this.vertexPositionsZ)
        // All the arrays must be the same length so we can only check it on one of them.
        require(index in triangleVertex1.indices)
        vertexPositionsX[triangleVertex1[index]] = posX
        vertexPositionsY[triangleVertex2[index]] = posY
        vertexPositionsZ[triangleVertex3[index]] = posZ
    }

    fun transformVertexSkins(transformer: (src: Int) -> Int) {
        val skins = requireNotNull(this.vertexSkins)
        for (i in skins.indices) {
            val current = skins[i]
            val result = transformer(current)
            skins[i] = result
        }
    }

    fun fillVertexSkins(value: Int) {
        vertexSkins = IntArray(vertexCount) { value }
    }

    fun translate(
        offsetX: Int,
        offsetY: Int,
        offsetZ: Int,
    ) {
        val vertexPositionsX = requireNotNull(this.vertexPositionsX)
        val vertexPositionsY = requireNotNull(this.vertexPositionsY)
        val vertexPositionsZ = requireNotNull(this.vertexPositionsZ)
        for (i in vertexPositionsX.indices) {
            vertexPositionsX[i] += offsetX
            vertexPositionsY[i] += offsetY
            vertexPositionsZ[i] += offsetZ
        }
    }

    fun resize(
        x: Int,
        y: Int,
        z: Int,
    ) {
        require(x >= 0)
        require(y >= 0)
        require(z >= 0)
        val vertexPositionsX = requireNotNull(this.vertexPositionsX)
        val vertexPositionsY = requireNotNull(this.vertexPositionsY)
        val vertexPositionsZ = requireNotNull(this.vertexPositionsZ)
        for (i in vertexPositionsX.indices) {
            vertexPositionsX[i] = vertexPositionsX[i] * x / 128
            vertexPositionsY[i] = vertexPositionsY[i] * y / 128
            vertexPositionsZ[i] = vertexPositionsZ[i] * z / 128
        }
    }

    /**
     * Rotates the model by the provided [xan], [yan] and [zan] values.
     * The values are expected to be in range of 0 to 2047(inclusive), and will be sanitized to be within them.
     * @param xan defines the rotation on the horizontal axis (think of it as a chathead looking left/right)
     * @param yan defines the rotation on the vertical axis (think of it as a chathead looking up or down)
     * @param zan defines the tilt of the model (think of it as a chathead tilting its head to the right or left)
     */
    fun rotate(
        xan: Int,
        yan: Int,
        zan: Int,
    ) {
        val vertexPositionsX = requireNotNull(this.vertexPositionsX)
        val vertexPositionsY = requireNotNull(this.vertexPositionsY)
        val vertexPositionsZ = requireNotNull(this.vertexPositionsZ)
        for (i in 0 until vertexCount) {
            if (xan != 0) {
                val sin = SINE[xan and 0x7FF]
                val cos = COSINE[xan and 0x7FF]
                val temp = sin * vertexPositionsZ[i] + cos * vertexPositionsX[i] shr 16
                vertexPositionsZ[i] = cos * vertexPositionsZ[i] - sin * vertexPositionsX[i] shr 16
                vertexPositionsX[i] = temp
            }

            if (yan != 0) {
                val sin = SINE[yan and 0x7FF]
                val cos = COSINE[yan and 0x7FF]
                val temp = cos * vertexPositionsY[i] - sin * vertexPositionsZ[i] shr 16
                vertexPositionsZ[i] = sin * vertexPositionsY[i] + cos * vertexPositionsZ[i] shr 16
                vertexPositionsY[i] = temp
            }

            if (zan != 0) {
                val sin = SINE[zan and 0x7FF]
                val cos = COSINE[zan and 0x7FF]
                val temp = sin * vertexPositionsY[i] + cos * vertexPositionsX[i] shr 16
                vertexPositionsY[i] = cos * vertexPositionsY[i] - sin * vertexPositionsX[i] shr 16
                vertexPositionsX[i] = temp
            }
        }
    }

    fun decode(
        data: ByteArray,
        vararg options: MeshDecodingOption,
    ) {
        val version = data[data.size - 1].toInt()
        val extra = data[data.size - 2].toInt()
        return when {
            version == -3 && extra == -1 -> decode4(data, options)
            version == -2 && extra == -1 -> decode3(data, options)
            version == -1 && extra == -1 -> decode2(data, options)
            else -> decode1(data, options)
        }
    }

    fun removeTextures() {
        this.textureTriangleCount = 0
        this.triangleTextures = null
        this.textureRenderTypes = null
        this.textureTriangleVertex1 = null
        this.textureTriangleVertex2 = null
        this.textureTriangleVertex3 = null
        this.textureScaleX = null
        this.textureScaleY = null
        this.textureScaleZ = null
        this.textureRotation = null
        this.textureDirection = null
        this.textureSpeed = null
        this.textureTransU = null
        this.textureTransV = null
        this.textureCoordinates = null
    }

    fun removeSkeletalInformation() {
        this.skeletalBones = null
        this.skeletalScales = null
        if (type == MeshType.UnversionedSkeletal) {
            type = MeshType.Unversioned
        } else if (type == MeshType.VersionedSkeletal) {
            type = MeshType.Versioned
        }
    }

    fun setSkeletalInformation(
        bones: Array<IntArray?>,
        scales: Array<IntArray?>,
    ) {
        this.skeletalBones = bones
        this.skeletalScales = scales
        if (type == MeshType.Unversioned) {
            type = MeshType.UnversionedSkeletal
        } else if (type == MeshType.Versioned) {
            type = MeshType.VersionedSkeletal
        }
    }

    fun removeParticles() {
        this.emitters = null
        this.effectors = null
    }

    fun removeBillboards() {
        this.faceBillboards = null
    }

    fun encode(): Writer =
        when (type) {
            MeshType.Unversioned -> encode1()
            MeshType.Versioned -> encode2()
            MeshType.UnversionedSkeletal -> encode3()
            MeshType.VersionedSkeletal -> encode4()
        }

    private fun decode1(data: ByteArray, options: Array<out MeshDecodingOption>) {
        this.type = MeshType.Unversioned
        val buf1 = ArrayReader(data)
        val buf2 = ArrayReader(data)
        val buf3 = ArrayReader(data)
        val buf4 = ArrayReader(data)
        val buf5 = ArrayReader(data)
        buf1.position(buf1.length - 18)
        val vertexCount = buf1.readUnsignedShort()
        val triangleCount = buf1.readUnsignedShort()
        val textureTriangleCount = buf1.readUnsignedByte()
        val hasTextures = buf1.readUnsignedByte()
        val modelPriority = buf1.readUnsignedByte()
        val hasFaceAlphas = buf1.readUnsignedByte()
        val hasFaceSkins = buf1.readUnsignedByte()
        val hasVertexSkins = buf1.readUnsignedByte()
        val vertexXBufIndex = buf1.readUnsignedShort()
        val vertexYBufIndex = buf1.readUnsignedShort()

        @Suppress("UNUSED_VARIABLE")
        val vertexZBufIndex = buf1.readUnsignedShort()
        val triangleIndicesBufIndex = buf1.readUnsignedShort()

        var position = 0

        @Suppress("KotlinConstantConditions")
        val vertexFlagsOffset = position
        position += vertexCount
        val facesCompressTypeOffset = position
        position += triangleCount
        val facePrioritiesOffset = position
        if (modelPriority == 0xFF) {
            position += triangleCount
        }
        val faceSkinsOffset = position
        if (hasFaceSkins == 1) {
            position += triangleCount
        }
        val faceTypesOffset = position
        if (hasTextures == 1) {
            position += triangleCount
        }
        val vertexSkinsOffset = position
        if (hasVertexSkins == 1) {
            position += vertexCount
        }
        val faceAlphasOffset = position
        if (hasFaceAlphas == 1) {
            position += triangleCount
        }
        val faceIndicesOffset = position
        position += triangleIndicesBufIndex
        val faceColorsOffset = position
        position += triangleCount * 2
        val faceMappingsOffset = position
        position += textureTriangleCount * 6
        val vertexXOffsetOffset = position
        position += vertexXBufIndex
        val vertexYOffsetOffset = position
        position += vertexYBufIndex
        val vertexZOffsetOffset = position

        initializeUnversioned(
            vertexCount,
            triangleCount,
            textureTriangleCount,
            hasVertexSkins,
            hasTextures,
            modelPriority,
            hasFaceAlphas,
            hasFaceSkins,
            hasSkeletalBones = false,
        )

        buf1.position(vertexFlagsOffset)
        buf2.position(vertexXOffsetOffset)
        buf3.position(vertexYOffsetOffset)
        buf4.position(vertexZOffsetOffset)
        buf5.position(vertexSkinsOffset)
        readVertexPositions(
            hasVertexSkins,
            hasSkeletalBones = false,
            buf1,
            buf2,
            buf3,
            buf4,
            buf5,
        )
        buf1.position(faceColorsOffset)
        buf2.position(faceTypesOffset)
        buf3.position(facePrioritiesOffset)
        buf4.position(faceAlphasOffset)
        buf5.position(faceSkinsOffset)
        val (usesFaceTypes, usesMaterials) =
            readUnversionedTriangleInfo(
                options,
                hasTextures,
                modelPriority,
                hasFaceAlphas,
                hasFaceSkins,
                buf1,
                buf2,
                buf3,
                buf4,
                buf5,
            )
        buf1.position(faceIndicesOffset)
        buf2.position(facesCompressTypeOffset)
        readTriangleVertices(buf1, buf2)
        buf1.position(faceMappingsOffset)
        readUnversionedTextureVertices(buf1)
        if (!options.contains(MeshDecodingOption.PreserveOriginalData)) {
            filterUnversionedTextures(
                usesFaceTypes,
                usesMaterials,
            )
        }
    }

    private fun decode2(data: ByteArray, options: Array<out MeshDecodingOption>) {
        this.type = MeshType.Versioned
        val buf1 = ArrayReader(data)
        val buf2 = ArrayReader(data)
        val buf3 = ArrayReader(data)
        val buf4 = ArrayReader(data)
        val buf5 = ArrayReader(data)
        val buf6 = ArrayReader(data)
        val buf7 = ArrayReader(data)
        buf1.position(buf1.length - 23)
        val vertexCount = buf1.readUnsignedShort()
        val triangleCount = buf1.readUnsignedShort()
        val textureTriangleCount = buf1.readUnsignedByte()
        val footerFlags = buf1.readUnsignedByte()
        val hasFaceTypes = footerFlags and FACE_TYPES_FLAG == FACE_TYPES_FLAG
        val hasParticleEffects = footerFlags and PARTICLES_FLAG == PARTICLES_FLAG
        val hasBillboards = footerFlags and BILLBOARDS_FLAG == BILLBOARDS_FLAG
        val hasVersion = footerFlags and VERSION_FLAG == VERSION_FLAG
        val version =
            if (hasVersion) {
                buf1.position(buf1.position() - 7)
                val version = buf1.readUnsignedByte()
                buf1.position(buf1.position() + 6)
                version
            } else {
                DEFAULT_VERSION
            }
        val modelPriority = buf1.readUnsignedByte()
        val hasFaceAlphas = buf1.readUnsignedByte()
        val hasFaceSkins = buf1.readUnsignedByte()
        val hasTextures = buf1.readUnsignedByte()
        val hasVertexSkins = buf1.readUnsignedByte()
        val modelVerticesX = buf1.readUnsignedShort()
        val modelVerticesY = buf1.readUnsignedShort()
        val modelVerticesZ = buf1.readUnsignedShort()
        val faceIndices = buf1.readUnsignedShort()
        val textureIndices = buf1.readUnsignedShort()

        var simpleTextureFaceCount = 0
        var complexTextureFaceCount = 0
        var cubeTextureFaceCount = 0
        if (textureTriangleCount > 0) {
            val textureRenderTypes = IntArray(textureTriangleCount)
            this.textureRenderTypes = textureRenderTypes
            buf1.position(0)
            for (index in 0 until textureTriangleCount) {
                textureRenderTypes[index] = buf1.readByte()
                val type = textureRenderTypes[index]
                if (type == SIMPLE_TEXTURE) {
                    simpleTextureFaceCount++
                }
                if (type == CUBE_TEXTURE) {
                    cubeTextureFaceCount++
                }
                if (type in COMPLEX_TEXTURE_RANGE) {
                    complexTextureFaceCount++
                }
            }
        }

        var offset = textureTriangleCount
        val vertexFlagsOffset = offset
        offset += vertexCount
        val faceTypesOffset = offset
        if (hasFaceTypes) {
            offset += triangleCount
        }
        val faceCompressTypeOffset = offset
        offset += triangleCount
        val facePrioritiesOffset = offset
        if (modelPriority == 0xFF) {
            offset += triangleCount
        }
        val faceSkinsOffset = offset
        if (hasFaceSkins == 1) {
            offset += triangleCount
        }
        val vertexSkinsOffset = offset
        if (hasVertexSkins == 1) {
            offset += vertexCount
        }
        val faceAlphasOffset = offset
        if (hasFaceAlphas == 1) {
            offset += triangleCount
        }
        val faceIndicesOffset = offset
        offset += faceIndices
        val faceMaterialsOffset = offset
        if (hasTextures == 1) {
            offset += triangleCount * 2
        }
        val faceTextureIndicesOffset = offset
        offset += textureIndices
        val faceColorsOffset = offset
        offset += 2 * triangleCount
        val vertexXOffsetOffset = offset
        offset += modelVerticesX
        val vertexYOffsetOffset = offset
        offset += modelVerticesY
        val vertexZOffsetOffset = offset
        offset += modelVerticesZ
        val simpleTexturesOffset = offset
        offset += 6 * simpleTextureFaceCount
        val complexTexturesOffset = offset
        offset += complexTextureFaceCount * 6
        var textureBytes = 6
        if (version == 14) {
            textureBytes = 7
        } else if (version >= 15) {
            textureBytes = 9
        }
        val texturesScaleOffset = offset
        offset += complexTextureFaceCount * textureBytes
        val texturesRotationOffset = offset
        offset += complexTextureFaceCount
        val texturesDirectionOffset = offset
        offset += complexTextureFaceCount
        val texturesTranslationOffset = offset
        offset += complexTextureFaceCount + (2 * cubeTextureFaceCount)
        val particlesOffset = offset

        initializeVersioned(
            version,
            vertexCount,
            triangleCount,
            textureTriangleCount,
            hasFaceTypes,
            hasVertexSkins,
            hasTextures,
            modelPriority,
            hasFaceAlphas,
            hasFaceSkins,
            complexTextureFaceCount,
            cubeTextureFaceCount,
            hasSkeletalBones = false,
        )

        buf1.position(vertexFlagsOffset)
        buf2.position(vertexXOffsetOffset)
        buf3.position(vertexYOffsetOffset)
        buf4.position(vertexZOffsetOffset)
        buf5.position(vertexSkinsOffset)
        readVertexPositions(
            hasVertexSkins,
            hasSkeletalBones = false,
            buf1,
            buf2,
            buf3,
            buf4,
            buf5,
        )

        buf1.position(faceColorsOffset)
        buf2.position(faceTypesOffset)
        buf3.position(facePrioritiesOffset)
        buf4.position(faceAlphasOffset)
        buf5.position(faceSkinsOffset)
        buf6.position(faceMaterialsOffset)
        buf7.position(faceTextureIndicesOffset)
        readVersionedTriangleInfo(
            hasTextures,
            modelPriority,
            hasFaceAlphas,
            hasFaceSkins,
            hasFaceTypes,
            buf1,
            buf2,
            buf3,
            buf4,
            buf5,
            buf6,
            buf7,
        )
        buf1.position(faceIndicesOffset)
        buf2.position(faceCompressTypeOffset)
        readTriangleVertices(
            buf1,
            buf2,
        )
        buf1.position(simpleTexturesOffset)
        buf2.position(complexTexturesOffset)
        buf3.position(texturesScaleOffset)
        buf4.position(texturesRotationOffset)
        buf5.position(texturesDirectionOffset)
        buf6.position(texturesTranslationOffset)
        readVersionedTextures(
            buf1,
            buf2,
            buf3,
            buf4,
            buf5,
            buf6,
        )

        buf1.position(particlesOffset)
        if (hasParticleEffects) {
            decodeParticles(buf1, modelPriority)
        }
        if (hasBillboards) {
            decodeBillboards(buf1)
        }
        if (options.contains(MeshDecodingOption.ScaleVersionedMesh)) {
            if (version > DEFAULT_VERSION) {
                downscale()
            }
        }
    }

    private fun initializeUnversioned(
        vertexCount: Int,
        triangleCount: Int,
        textureTriangleCount: Int,
        hasVertexSkins: Int,
        hasTextures: Int,
        modelPriority: Int,
        hasFaceAlphas: Int,
        hasFaceSkins: Int,
        hasSkeletalBones: Boolean,
    ) {
        this.version = UNVERSIONED
        this.vertexCount = vertexCount
        this.triangleCount = triangleCount
        this.textureTriangleCount = textureTriangleCount
        this.vertexPositionsX = IntArray(vertexCount)
        this.vertexPositionsY = IntArray(vertexCount)
        this.vertexPositionsZ = IntArray(vertexCount)
        this.triangleVertex1 = IntArray(triangleCount)
        this.triangleVertex2 = IntArray(triangleCount)
        this.triangleVertex3 = IntArray(triangleCount)
        triangleColors = ShortArray(triangleCount)
        if (textureTriangleCount > 0) {
            textureRenderTypes = IntArray(textureTriangleCount)
            textureTriangleVertex1 = IntArray(textureTriangleCount)
            textureTriangleVertex2 = IntArray(textureTriangleCount)
            textureTriangleVertex3 = IntArray(textureTriangleCount)
        }
        if (hasVertexSkins == 1) {
            vertexSkins = IntArray(vertexCount)
        }
        if (hasTextures == 1) {
            triangleRenderTypes = IntArray(triangleCount)
            textureCoordinates = IntArray(triangleCount)
            triangleTextures = IntArray(triangleCount)
        }
        if (modelPriority == 0xFF) {
            triangleRenderPriorities = IntArray(triangleCount)
        } else {
            renderPriority = modelPriority.toByte().toInt()
        }
        if (hasFaceAlphas == 1) {
            triangleAlphas = IntArray(triangleCount)
        }

        if (hasFaceSkins == 1) {
            triangleSkins = IntArray(triangleCount)
        }
        if (hasSkeletalBones) {
            this.skeletalBones = arrayOfNulls(vertexCount)
            this.skeletalScales = arrayOfNulls(vertexCount)
        }
    }

    private fun initializeVersioned(
        version: Int,
        vertexCount: Int,
        triangleCount: Int,
        textureTriangleCount: Int,
        hasFaceTypes: Boolean,
        hasVertexSkins: Int,
        hasTextures: Int,
        modelPriority: Int,
        hasFaceAlphas: Int,
        hasFaceSkins: Int,
        complexTextureFaceCount: Int,
        cubeTextureFaceCount: Int,
        hasSkeletalBones: Boolean,
    ) {
        this.version = version
        this.vertexCount = vertexCount
        this.triangleCount = triangleCount
        this.textureTriangleCount = textureTriangleCount
        vertexPositionsX = IntArray(vertexCount)
        vertexPositionsY = IntArray(vertexCount)
        vertexPositionsZ = IntArray(vertexCount)
        triangleVertex1 = IntArray(triangleCount)
        triangleVertex2 = IntArray(triangleCount)
        triangleVertex3 = IntArray(triangleCount)
        triangleColors = ShortArray(triangleCount)
        if (hasVertexSkins == 1) {
            vertexSkins = IntArray(vertexCount)
        }

        if (hasFaceTypes) {
            triangleRenderTypes = IntArray(triangleCount)
        }

        if (modelPriority == 0xFF) {
            triangleRenderPriorities = IntArray(triangleCount)
        } else {
            renderPriority = modelPriority.toByte().toInt()
        }

        if (hasFaceAlphas == 1) {
            triangleAlphas = IntArray(triangleCount)
        }

        if (hasFaceSkins == 1) {
            triangleSkins = IntArray(triangleCount)
        }

        if (hasTextures == 1) {
            triangleTextures = IntArray(triangleCount)
        }

        if (hasTextures == 1 && textureTriangleCount > 0) {
            textureCoordinates = IntArray(triangleCount)
        }

        if (textureTriangleCount > 0) {
            textureTriangleVertex1 = IntArray(textureTriangleCount)
            textureTriangleVertex2 = IntArray(textureTriangleCount)
            textureTriangleVertex3 = IntArray(textureTriangleCount)
            if (complexTextureFaceCount > 0) {
                this.textureScaleX = IntArray(complexTextureFaceCount)
                this.textureScaleY = IntArray(complexTextureFaceCount)
                this.textureScaleZ = IntArray(complexTextureFaceCount)
                this.textureRotation = IntArray(complexTextureFaceCount)
                this.textureDirection = IntArray(complexTextureFaceCount)
                this.textureSpeed = IntArray(complexTextureFaceCount)
            }
            if (cubeTextureFaceCount > 0) {
                this.textureTransU = IntArray(cubeTextureFaceCount)
                this.textureTransV = IntArray(cubeTextureFaceCount)
            }
        }
        if (hasSkeletalBones) {
            this.skeletalBones = arrayOfNulls(vertexCount)
            this.skeletalScales = arrayOfNulls(vertexCount)
        }
    }

    private fun readVertexPositions(
        hasVertexSkins: Int,
        hasSkeletalBones: Boolean,
        buf1: Reader,
        buf2: Reader,
        buf3: Reader,
        buf4: Reader,
        buf5: Reader,
    ) {
        if (vertexCount <= 0) return
        val vertexPositionsX = requireNotNull(vertexPositionsX)
        val vertexPositionsY = requireNotNull(vertexPositionsY)
        val vertexPositionsZ = requireNotNull(vertexPositionsZ)
        var lastXOffset = 0
        var lastYOffset = 0
        var lastZOffset = 0
        for (index in 0 until vertexCount) {
            val pflag = buf1.readUnsignedByte()
            var xOffset = 0
            if (pflag and X_POS_FLAG != 0) {
                xOffset = buf2.gSmart1or2s()
            }
            var yOffset = 0
            if (pflag and Y_POS_FLAG != 0) {
                yOffset = buf3.gSmart1or2s()
            }
            var zOffset = 0
            if (pflag and Z_POS_FLAG != 0) {
                zOffset = buf4.gSmart1or2s()
            }
            vertexPositionsX[index] = xOffset + lastXOffset
            vertexPositionsY[index] = yOffset + lastYOffset
            vertexPositionsZ[index] = zOffset + lastZOffset
            lastXOffset = vertexPositionsX[index]
            lastYOffset = vertexPositionsY[index]
            lastZOffset = vertexPositionsZ[index]
            if (hasVertexSkins == 1) {
                val vertexSkins = requireNotNull(vertexSkins)
                vertexSkins[index] = buf5.readUnsignedByte()
            }
        }

        if (hasSkeletalBones) {
            val skeletalBones = requireNotNull(this.skeletalBones)
            val skeletalScales = requireNotNull(this.skeletalScales)
            for (index in 0 until vertexCount) {
                val count = buf5.readUnsignedByte()
                val bones = IntArray(count)
                val scales = IntArray(count)
                skeletalBones[index] = bones
                skeletalScales[index] = scales
                for (i in 0 until count) {
                    bones[i] = buf5.readUnsignedByte()
                    scales[i] = buf5.readUnsignedByte()
                }
            }
        }
    }

    private fun readUnversionedTriangleInfo(
        options: Array<out MeshDecodingOption>,
        hasTextures: Int,
        modelPriority: Int,
        hasFaceAlphas: Int,
        hasFaceSkins: Int,
        buf1: Reader,
        buf2: Reader,
        buf3: Reader,
        buf4: Reader,
        buf5: Reader,
    ): Pair<Boolean, Boolean> {
        if (triangleCount <= 0) return Pair(first = false, second = false)
        var usesFaceTypes = false
        var usesMaterials = false
        val triangleColors = requireNotNull(this.triangleColors)
        val preserveData = options.contains(MeshDecodingOption.PreserveOriginalData)
        for (index in 0 until triangleCount) {
            triangleColors[index] = buf1.readUnsignedShort().toShort()
            if (hasTextures == 1) {
                val triangleRenderTypes = requireNotNull(this.triangleRenderTypes)
                val textureCoordinates = requireNotNull(this.textureCoordinates)
                val triangleTextures = requireNotNull(this.triangleTextures)
                val flag = buf2.readUnsignedByte()
                if (flag and USES_FACE_TYPES_FLAG == 1) {
                    triangleRenderTypes[index] = 1
                    usesFaceTypes = true
                } else {
                    triangleRenderTypes[index] = 0
                }
                if (flag and USES_MATERIALS_FLAG == 2) {
                    textureCoordinates[index] = flag shr 2
                    triangleTextures[index] = triangleColors[index].toInt()
                    if (!preserveData) {
                        triangleColors[index] = 127.toShort()
                    }
                    if (triangleTextures[index] != -1) {
                        usesMaterials = true
                    }
                } else {
                    textureCoordinates[index] = -1
                    triangleTextures[index] = -1
                }
            }
            if (modelPriority == 0xFF) {
                val triangleRenderPriorities = requireNotNull(this.triangleRenderPriorities)
                triangleRenderPriorities[index] = buf3.readByte()
            }
            if (hasFaceAlphas == 1) {
                val triangleAlphas = requireNotNull(this.triangleAlphas)
                triangleAlphas[index] = buf4.readByte()
            }
            if (hasFaceSkins == 1) {
                val triangleSkins = requireNotNull(this.triangleSkins)
                triangleSkins[index] = buf5.readUnsignedByte()
            }
        }
        return Pair(first = usesFaceTypes, second = usesMaterials)
    }

    private fun readVersionedTriangleInfo(
        hasTextures: Int,
        modelPriority: Int,
        hasFaceAlphas: Int,
        hasFaceSkins: Int,
        hasFaceTypes: Boolean,
        buf1: Reader,
        buf2: Reader,
        buf3: Reader,
        buf4: Reader,
        buf5: Reader,
        buf6: Reader,
        buf7: Reader,
    ) {
        if (triangleCount <= 0) return
        val triangleColors = requireNotNull(this.triangleColors)
        val textureCoordinates = this.textureCoordinates
        for (index in 0 until triangleCount) {
            triangleColors[index] = buf1.readUnsignedShort().toShort()
            if (hasFaceTypes) {
                val triangleRenderTypes = requireNotNull(this.triangleRenderTypes)
                triangleRenderTypes[index] = buf2.readByte()
            }
            if (modelPriority == 0xFF) {
                val triangleRenderPriorities = requireNotNull(this.triangleRenderPriorities)
                triangleRenderPriorities[index] = buf3.readByte()
            }
            if (hasFaceAlphas == 1) {
                val triangleAlphas = requireNotNull(this.triangleAlphas)
                triangleAlphas[index] = buf4.readByte()
            }
            if (hasFaceSkins == 1) {
                val triangleSkins = requireNotNull(this.triangleSkins)
                triangleSkins[index] = buf5.readUnsignedByte()
            }
            if (hasTextures == 1) {
                val triangleTextures = requireNotNull(this.triangleTextures)
                triangleTextures[index] = (buf6.readUnsignedShort() - 1).toShort().toInt()
            }
            if (textureCoordinates != null) {
                val triangleTextures = requireNotNull(this.triangleTextures)
                if (triangleTextures[index] == -1) {
                    textureCoordinates[index] = -1
                } else {
                    textureCoordinates[index] = (buf7.readUnsignedByte() - 1).toByte().toInt()
                }
            }
        }
    }

    private fun readTriangleVertices(buf1: Reader, buf2: Reader) {
        if (triangleCount <= 0) return
        val triangleVertex1 = requireNotNull(this.triangleVertex1)
        val triangleVertex2 = requireNotNull(this.triangleVertex2)
        val triangleVertex3 = requireNotNull(this.triangleVertex3)
        var vertex1 = 0
        var vertex2 = 0
        var vertex3 = 0
        var offset = 0
        for (index in 0 until triangleCount) {
            when (buf2.readUnsignedByte()) {
                1 -> {
                    vertex1 = buf1.gSmart1or2s() + offset
                    offset = vertex1
                    vertex2 = buf1.gSmart1or2s() + offset
                    offset = vertex2
                    vertex3 = buf1.gSmart1or2s() + offset
                    offset = vertex3
                    triangleVertex1[index] = vertex1
                    triangleVertex2[index] = vertex2
                    triangleVertex3[index] = vertex3
                }
                2 -> {
                    vertex2 = vertex3
                    vertex3 = buf1.gSmart1or2s() + offset
                    triangleVertex1[index] = vertex1
                    offset = vertex3
                    triangleVertex2[index] = vertex2
                    triangleVertex3[index] = vertex3
                }
                3 -> {
                    vertex1 = vertex3
                    vertex3 = buf1.gSmart1or2s() + offset
                    triangleVertex1[index] = vertex1
                    offset = vertex3
                    triangleVertex2[index] = vertex2
                    triangleVertex3[index] = vertex3
                }
                4 -> {
                    val pos1 = vertex1
                    vertex1 = vertex2
                    vertex3 = buf1.gSmart1or2s() + offset
                    vertex2 = pos1
                    triangleVertex1[index] = vertex1
                    offset = vertex3
                    triangleVertex2[index] = vertex2
                    triangleVertex3[index] = vertex3
                }
            }
        }
    }

    private fun readUnversionedTextureVertices(buf1: Reader) {
        if (textureTriangleCount <= 0) return
        val textureRenderTypes = requireNotNull(this.textureRenderTypes)
        val textureTriangleVertex1 = requireNotNull(this.textureTriangleVertex1)
        val textureTriangleVertex2 = requireNotNull(this.textureTriangleVertex2)
        val textureTriangleVertex3 = requireNotNull(this.textureTriangleVertex3)
        for (index in 0 until textureTriangleCount) {
            textureRenderTypes[index] = 0
            textureTriangleVertex1[index] =
                buf1
                    .readUnsignedShort()
                    .toShort()
                    .toInt()
            textureTriangleVertex2[index] =
                buf1
                    .readUnsignedShort()
                    .toShort()
                    .toInt()
            textureTriangleVertex3[index] =
                buf1
                    .readUnsignedShort()
                    .toShort()
                    .toInt()
        }
    }

    private fun filterUnversionedTextures(
        usesFaceTypes: Boolean,
        usesMaterials: Boolean,
    ) {
        val textureCoordinates = this.textureCoordinates
        if (textureCoordinates != null) {
            var usesMapping = false
            require(triangleCount > 0)
            val triangleVertex1 = requireNotNull(this.triangleVertex1)
            val triangleVertex2 = requireNotNull(this.triangleVertex2)
            val triangleVertex3 = requireNotNull(this.triangleVertex3)
            for (index in 0 until triangleCount) {
                val texture = textureCoordinates[index] and 0xFF
                if (texture != 0xFF) {
                    val textureTriangleVertex1 = requireNotNull(this.textureTriangleVertex1)
                    val textureTriangleVertex2 = requireNotNull(this.textureTriangleVertex2)
                    val textureTriangleVertex3 = requireNotNull(this.textureTriangleVertex3)
                    if (triangleVertex1[index] != textureTriangleVertex1[texture] and 0xFFFF ||
                        triangleVertex2[index] != textureTriangleVertex2[texture] and 0xFFFF ||
                        triangleVertex3[index] != textureTriangleVertex3[texture] and 0xFFFF
                    ) {
                        usesMapping = true
                    } else {
                        textureCoordinates[index] = -1
                    }
                }
            }
            if (!usesMapping) {
                this.textureCoordinates = null
            }
        }
        if (!usesFaceTypes) {
            triangleRenderTypes = null
        }
        if (!usesMaterials) {
            triangleTextures = null
        }
    }

    private fun readVersionedTextures(
        buf1: Reader,
        buf2: Reader,
        buf3: Reader,
        buf4: Reader,
        buf5: Reader,
        buf6: Reader,
    ) {
        if (textureTriangleCount <= 0) return
        val textureRenderTypes = requireNotNull(this.textureRenderTypes)
        val textureTriangleVertex1 = requireNotNull(this.textureTriangleVertex1)
        val textureTriangleVertex2 = requireNotNull(this.textureTriangleVertex2)
        val textureTriangleVertex3 = requireNotNull(this.textureTriangleVertex3)
        for (index in 0 until textureTriangleCount) {
            val textureRenderType = textureRenderTypes[index] and 0xFF
            if (textureRenderType == SIMPLE_TEXTURE) {
                textureTriangleVertex1[index] =
                    buf1
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                textureTriangleVertex2[index] =
                    buf1
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                textureTriangleVertex3[index] =
                    buf1
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
            }
            if (textureRenderType == CYLINDRICAL_TEXTURE) {
                val textureScaleX = requireNotNull(this.textureScaleX)
                val textureScaleY = requireNotNull(this.textureScaleY)
                val textureScaleZ = requireNotNull(this.textureScaleZ)
                val textureSpeed = requireNotNull(this.textureSpeed)
                val textureRotation = requireNotNull(this.textureRotation)
                val textureDirection = requireNotNull(this.textureDirection)
                textureTriangleVertex1[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                textureTriangleVertex2[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                textureTriangleVertex3[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                if (version < 15) {
                    textureScaleZ[index] = buf3.readUnsignedShort()
                    if (version < 14) {
                        textureSpeed[index] = buf3.readUnsignedShort()
                    } else {
                        textureSpeed[index] = buf3.readMedium()
                    }
                    textureScaleX[index] = buf3.readUnsignedShort()
                } else {
                    textureScaleZ[index] = buf3.readMedium()
                    textureSpeed[index] = buf3.readMedium()
                    textureScaleX[index] = buf3.readMedium()
                }
                textureRotation[index] = buf4.readByte()
                textureScaleY[index] = buf5.readByte()
                textureDirection[index] = buf6.readByte()
            }
            if (textureRenderType == CUBE_TEXTURE) {
                val textureScaleX = requireNotNull(this.textureScaleX)
                val textureScaleY = requireNotNull(this.textureScaleY)
                val textureScaleZ = requireNotNull(this.textureScaleZ)
                val textureSpeed = requireNotNull(this.textureSpeed)
                val textureRotation = requireNotNull(this.textureRotation)
                val textureDirection = requireNotNull(this.textureDirection)
                val textureTransU = requireNotNull(this.textureTransU)
                val textureTransV = requireNotNull(this.textureTransV)
                textureTriangleVertex1[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                textureTriangleVertex2[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                textureTriangleVertex3[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                if (version < 15) {
                    textureScaleZ[index] = buf3.readUnsignedShort()
                    if (version >= 14) {
                        textureSpeed[index] = buf3.readMedium()
                    } else {
                        textureSpeed[index] = buf3.readUnsignedShort()
                    }
                    textureScaleX[index] = buf3.readUnsignedShort()
                } else {
                    textureScaleZ[index] = buf3.readMedium()
                    textureSpeed[index] = buf3.readMedium()
                    textureScaleX[index] = buf3.readMedium()
                }
                textureRotation[index] = buf4.readByte()
                textureScaleY[index] = buf5.readByte()
                textureDirection[index] = buf6.readByte()
                textureTransU[index] = buf6.readByte()
                textureTransV[index] = buf6.readByte()
            }
            if (textureRenderType == SPHERICAL_TEXTURE) {
                val textureScaleX = requireNotNull(this.textureScaleX)
                val textureScaleY = requireNotNull(this.textureScaleY)
                val textureScaleZ = requireNotNull(this.textureScaleZ)
                val textureSpeed = requireNotNull(this.textureSpeed)
                val textureRotation = requireNotNull(this.textureRotation)
                val textureDirection = requireNotNull(this.textureDirection)
                textureTriangleVertex1[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                textureTriangleVertex2[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                textureTriangleVertex3[index] =
                    buf2
                        .readUnsignedShort()
                        .toShort()
                        .toInt()
                if (version < 15) {
                    textureScaleZ[index] = buf3.readUnsignedShort()
                    if (version < 14) {
                        textureSpeed[index] = buf3.readUnsignedShort()
                    } else {
                        textureSpeed[index] = buf3.readMedium()
                    }
                    textureScaleX[index] = buf3.readUnsignedShort()
                } else {
                    textureScaleZ[index] = buf3.readMedium()
                    textureSpeed[index] = buf3.readMedium()
                    textureScaleX[index] = buf3.readMedium()
                }
                textureRotation[index] = buf4.readByte()
                textureScaleY[index] = buf5.readByte()
                textureDirection[index] = buf6.readByte()
            }
        }
    }

    private fun decodeParticles(buf1: Reader, modelPriority: Int) {
        val numEmitters = buf1.readUnsignedByte()
        if (numEmitters > 0) {
            val triangleVertex1 = requireNotNull(this.triangleVertex1)
            val triangleVertex2 = requireNotNull(this.triangleVertex2)
            val triangleVertex3 = requireNotNull(this.triangleVertex3)

            emitters =
                Array(numEmitters) {
                    val emitter = buf1.readUnsignedShort()
                    val face = buf1.readUnsignedShort()
                    val pri =
                        if (modelPriority == 0xFF) {
                            val triangleRenderPriorities = requireNotNull(this.triangleRenderPriorities)
                            triangleRenderPriorities[face]
                        } else {
                            modelPriority
                        }
                    EmissiveTriangle(
                        emitter,
                        face,
                        triangleVertex1[face],
                        triangleVertex2[face],
                        triangleVertex3[face],
                        pri,
                    )
                }
        }
        val numEffectors = buf1.readUnsignedByte()
        if (numEffectors > 0) {
            effectors =
                Array(numEffectors) {
                    val effector = buf1.readUnsignedShort()
                    val vertex = buf1.readUnsignedShort()
                    EffectiveVertex(effector, vertex)
                }
        }
    }

    private fun decodeBillboards(buf1: Reader) {
        val count = buf1.readUnsignedByte()
        if (count > 0) {
            val faceBillboards =
                Array(count) {
                    val id = buf1.readUnsignedShort()
                    val face = buf1.readUnsignedShort()
                    val skin = buf1.readUnsignedByte()
                    val distance = buf1.readByte()
                    FaceBillboard(id, face, skin, distance)
                }
            this.faceBillboards = faceBillboards
        }
    }

    private fun decode3(data: ByteArray, options: Array<out MeshDecodingOption>) {
        this.type = MeshType.UnversionedSkeletal
        val buf1 = ArrayReader(data)
        val buf2 = ArrayReader(data)
        val buf3 = ArrayReader(data)
        val buf4 = ArrayReader(data)
        val buf5 = ArrayReader(data)
        buf1.position(buf1.length - 23)
        val vertexCount = buf1.readUnsignedShort()
        val triangleCount = buf1.readUnsignedShort()
        val textureTriangleCount = buf1.readUnsignedByte()
        val hasTextures = buf1.readUnsignedByte()
        val modelPriority = buf1.readUnsignedByte()
        val hasFaceAlphas = buf1.readUnsignedByte()
        val hasFaceSkins = buf1.readUnsignedByte()
        val hasVertexSkins = buf1.readUnsignedByte()
        val hasSkeletalBones = buf1.readUnsignedByte()
        val vertexXBufIndex = buf1.readUnsignedShort()
        val vertexYBufIndex = buf1.readUnsignedShort()

        @Suppress("UNUSED_VARIABLE")
        val vertexZBufIndex = buf1.readUnsignedShort()
        val triangleIndicesBufIndex = buf1.readUnsignedShort()
        val skeletalInfoOffset = buf1.readUnsignedShort()

        var position = 0

        @Suppress("KotlinConstantConditions")
        val vertexFlagsOffset = position
        position += vertexCount
        val facesCompressTypeOffset = position
        position += triangleCount
        val facePrioritiesOffset = position
        if (modelPriority == 0xFF) {
            position += triangleCount
        }
        val faceSkinsOffset = position
        if (hasFaceSkins == 1) {
            position += triangleCount
        }
        val faceTypesOffset = position
        if (hasTextures == 1) {
            position += triangleCount
        }
        val vertexSkinsOffset = position
        position += skeletalInfoOffset
        val faceAlphasOffset = position
        if (hasFaceAlphas == 1) {
            position += triangleCount
        }
        val faceIndicesOffset = position
        position += triangleIndicesBufIndex
        val faceColorsOffset = position
        position += triangleCount * 2
        val faceMappingsOffset = position
        position += textureTriangleCount * 6
        val vertexXOffsetOffset = position
        position += vertexXBufIndex
        val vertexYOffsetOffset = position
        position += vertexYBufIndex
        val vertexZOffsetOffset = position

        initializeUnversioned(
            vertexCount,
            triangleCount,
            textureTriangleCount,
            hasVertexSkins,
            hasTextures,
            modelPriority,
            hasFaceAlphas,
            hasFaceSkins,
            hasSkeletalBones = hasSkeletalBones == 1,
        )

        buf1.position(vertexFlagsOffset)
        buf2.position(vertexXOffsetOffset)
        buf3.position(vertexYOffsetOffset)
        buf4.position(vertexZOffsetOffset)
        buf5.position(vertexSkinsOffset)
        readVertexPositions(
            hasVertexSkins,
            hasSkeletalBones = hasSkeletalBones == 1,
            buf1,
            buf2,
            buf3,
            buf4,
            buf5,
        )
        buf1.position(faceColorsOffset)
        buf2.position(faceTypesOffset)
        buf3.position(facePrioritiesOffset)
        buf4.position(faceAlphasOffset)
        buf5.position(faceSkinsOffset)
        val (usesFaceTypes, usesMaterials) =
            readUnversionedTriangleInfo(
                options,
                hasTextures,
                modelPriority,
                hasFaceAlphas,
                hasFaceSkins,
                buf1,
                buf2,
                buf3,
                buf4,
                buf5,
            )
        buf1.position(faceIndicesOffset)
        buf2.position(facesCompressTypeOffset)
        readTriangleVertices(
            buf1,
            buf2,
        )
        buf1.position(faceMappingsOffset)
        readUnversionedTextureVertices(buf1)
        if (!options.contains(MeshDecodingOption.PreserveOriginalData)) {
            filterUnversionedTextures(
                usesFaceTypes,
                usesMaterials,
            )
        }
    }

    private fun decode4(data: ByteArray, options: Array<out MeshDecodingOption>) {
        this.type = MeshType.VersionedSkeletal
        val buf1 = ArrayReader(data)
        val buf2 = ArrayReader(data)
        val buf3 = ArrayReader(data)
        val buf4 = ArrayReader(data)
        val buf5 = ArrayReader(data)
        val buf6 = ArrayReader(data)
        val buf7 = ArrayReader(data)
        buf1.position(buf1.length - 26)
        val vertexCount = buf1.readUnsignedShort()
        val triangleCount = buf1.readUnsignedShort()
        val textureTriangleCount = buf1.readUnsignedByte()
        val footerFlags = buf1.readUnsignedByte()
        val hasFaceTypes = footerFlags and FACE_TYPES_FLAG == 1
        val hasParticleEffects = footerFlags and PARTICLES_FLAG == 2
        val hasBillboards = footerFlags and BILLBOARDS_FLAG == 4
        val hasVersion = footerFlags and VERSION_FLAG == 8
        val version =
            if (hasVersion) {
                buf1.position(buf1.position() - 7)
                val version = buf1.readUnsignedByte()
                buf1.position(buf1.position() + 6)
                version
            } else {
                DEFAULT_VERSION
            }
        val modelPriority = buf1.readUnsignedByte()
        val hasFaceAlphas = buf1.readUnsignedByte()
        val hasFaceSkins = buf1.readUnsignedByte()
        val hasTextures = buf1.readUnsignedByte()
        val hasVertexSkins = buf1.readUnsignedByte()
        val hasSkeletalBones = buf1.readUnsignedByte()
        val modelVerticesX = buf1.readUnsignedShort()
        val modelVerticesY = buf1.readUnsignedShort()
        val modelVerticesZ = buf1.readUnsignedShort()
        val faceIndices = buf1.readUnsignedShort()
        val textureIndices = buf1.readUnsignedShort()
        val skeletalInfoOffset = buf1.readUnsignedShort()

        var simpleTextureFaceCount = 0
        var complexTextureFaceCount = 0
        var cubeTextureFaceCount = 0
        if (textureTriangleCount > 0) {
            val textureRenderTypes = IntArray(textureTriangleCount)
            this.textureRenderTypes = textureRenderTypes
            buf1.position(0)
            for (index in 0 until textureTriangleCount) {
                textureRenderTypes[index] = buf1.readByte()
                val type = textureRenderTypes[index]
                if (type == 0) {
                    simpleTextureFaceCount++
                }
                if (type == 2) {
                    cubeTextureFaceCount++
                }
                if (type in 1..3) {
                    complexTextureFaceCount++
                }
            }
        }

        var offset = textureTriangleCount
        val vertexFlagsOffset = offset
        offset += vertexCount
        val faceTypesOffset = offset
        if (hasFaceTypes) {
            offset += triangleCount
        }
        val faceCompressTypeOffset = offset
        offset += triangleCount
        val facePrioritiesOffset = offset
        if (modelPriority == 0xFF) {
            offset += triangleCount
        }
        val faceSkinsOffset = offset
        if (hasFaceSkins == 1) {
            offset += triangleCount
        }
        val vertexSkinsOffset = offset
        offset += skeletalInfoOffset
        val faceAlphasOffset = offset
        if (hasFaceAlphas == 1) {
            offset += triangleCount
        }
        val faceIndicesOffset = offset
        offset += faceIndices
        val faceMaterialsOffset = offset
        if (hasTextures == 1) {
            offset += triangleCount * 2
        }
        val faceTextureIndicesOffset = offset
        offset += textureIndices
        val faceColorsOffset = offset
        offset += 2 * triangleCount
        val vertexXOffsetOffset = offset
        offset += modelVerticesX
        val vertexYOffsetOffset = offset
        offset += modelVerticesY
        val vertexZOffsetOffset = offset
        offset += modelVerticesZ
        val simpleTexturesOffset = offset
        offset += 6 * simpleTextureFaceCount
        val complexTexturesOffset = offset
        offset += complexTextureFaceCount * 6
        var textureBytes = 6
        if (version == 14) {
            textureBytes = 7
        } else if (version >= 15) {
            textureBytes = 9
        }
        val texturesScaleOffset = offset
        offset += complexTextureFaceCount * textureBytes
        val texturesRotationOffset = offset
        offset += complexTextureFaceCount
        val texturesDirectionOffset = offset
        offset += complexTextureFaceCount
        val texturesTranslationOffset = offset
        offset += complexTextureFaceCount + (2 * cubeTextureFaceCount)
        val particlesOffset = offset

        initializeVersioned(
            version,
            vertexCount,
            triangleCount,
            textureTriangleCount,
            hasFaceTypes,
            hasVertexSkins,
            hasTextures,
            modelPriority,
            hasFaceAlphas,
            hasFaceSkins,
            complexTextureFaceCount,
            cubeTextureFaceCount,
            hasSkeletalBones = hasSkeletalBones == 1,
        )

        buf1.position(vertexFlagsOffset)
        buf2.position(vertexXOffsetOffset)
        buf3.position(vertexYOffsetOffset)
        buf4.position(vertexZOffsetOffset)
        buf5.position(vertexSkinsOffset)
        readVertexPositions(
            hasVertexSkins,
            hasSkeletalBones = hasSkeletalBones == 1,
            buf1,
            buf2,
            buf3,
            buf4,
            buf5,
        )

        buf1.position(faceColorsOffset)
        buf2.position(faceTypesOffset)
        buf3.position(facePrioritiesOffset)
        buf4.position(faceAlphasOffset)
        buf5.position(faceSkinsOffset)
        buf6.position(faceMaterialsOffset)
        buf7.position(faceTextureIndicesOffset)
        readVersionedTriangleInfo(
            hasTextures,
            modelPriority,
            hasFaceAlphas,
            hasFaceSkins,
            hasFaceTypes,
            buf1,
            buf2,
            buf3,
            buf4,
            buf5,
            buf6,
            buf7,
        )
        buf1.position(faceIndicesOffset)
        buf2.position(faceCompressTypeOffset)
        readTriangleVertices(
            buf1,
            buf2,
        )
        buf1.position(simpleTexturesOffset)
        buf2.position(complexTexturesOffset)
        buf3.position(texturesScaleOffset)
        buf4.position(texturesRotationOffset)
        buf5.position(texturesDirectionOffset)
        buf6.position(texturesTranslationOffset)
        readVersionedTextures(
            buf1,
            buf2,
            buf3,
            buf4,
            buf5,
            buf6,
        )

        buf1.position(particlesOffset)
        if (hasParticleEffects) {
            decodeParticles(
                buf1,
                modelPriority,
            )
        }
        if (hasBillboards) {
            decodeBillboards(buf1)
        }
        if (options.contains(MeshDecodingOption.ScaleVersionedMesh)) {
            if (version > DEFAULT_VERSION) {
                downscale()
            }
        }
    }

    fun downscale(factor: Int = DOWNSCALE_FACTOR) {
        if (vertexCount > 0) {
            val vertexPositionsX = requireNotNull(this.vertexPositionsX)
            val vertexPositionsY = requireNotNull(this.vertexPositionsY)
            val vertexPositionsZ = requireNotNull(this.vertexPositionsZ)
            for (i in 0 until vertexCount) {
                vertexPositionsX[i] = vertexPositionsX[i] shr factor
                vertexPositionsY[i] = vertexPositionsY[i] shr factor
                vertexPositionsZ[i] = vertexPositionsZ[i] shr factor
            }
        }
        if (textureTriangleCount <= 0) return
        val scaleZ = textureScaleZ ?: return
        val speed = requireNotNull(this.textureSpeed)
        val renderTypes = requireNotNull(this.textureRenderTypes)
        val scaleX = requireNotNull(this.textureScaleX)
        for (i in scaleZ.indices) {
            scaleZ[i] = scaleZ[i] shr factor
            speed[i] = speed[i] shr factor
            if (renderTypes[i] != 1) {
                scaleX[i] = scaleX[i] shr factor
            }
        }
    }

    private fun upscale(factor: Int = DOWNSCALE_FACTOR) {
        if (vertexCount > 0) {
            val vertexPositionsX = requireNotNull(this.vertexPositionsX)
            val vertexPositionsY = requireNotNull(this.vertexPositionsY)
            val vertexPositionsZ = requireNotNull(this.vertexPositionsZ)
            for (i in 0 until vertexCount) {
                vertexPositionsX[i] = vertexPositionsX[i] shl factor
                vertexPositionsY[i] = vertexPositionsY[i] shl factor
                vertexPositionsZ[i] = vertexPositionsZ[i] shl factor
            }
        }
        if (textureTriangleCount <= 0) return
        val scaleZ = textureScaleZ ?: return
        val speed = requireNotNull(this.textureSpeed)
        val renderTypes = requireNotNull(this.textureRenderTypes)
        val scaleX = requireNotNull(this.textureScaleX)
        for (i in scaleZ.indices) {
            scaleZ[i] = scaleZ[i] shl factor
            speed[i] = speed[i] shl factor
            if (renderTypes[i] != 1) {
                scaleX[i] = scaleX[i] shl factor
            }
        }
    }

    private fun encode1(): Writer {
        val masterBuffer = BufferWriter(16 * 1024)
        val vertexFlagsBuffer = BufferWriter(128)
        val faceTypesBuffer = BufferWriter(128)
        val faceIndexTypesBuffer = BufferWriter(128)
        val facePrioritiesBuffer = BufferWriter(128)
        val faceSkinsBuffer = BufferWriter(128)
        val vertexSkinsBuffer = BufferWriter(128)
        val faceAlphasBuffer = BufferWriter(128)
        val faceIndicesBuffer = BufferWriter(128)
        val faceColorsBuffer = BufferWriter(128)
        val vertexXBuffer = BufferWriter(128)
        val vertexYBuffer = BufferWriter(128)
        val vertexZBuffer = BufferWriter(128)
        val texturesBuffer = BufferWriter(128)
        val footerBuffer = BufferWriter(128)
        val buffers =
            listOf(
                vertexFlagsBuffer,
                faceIndexTypesBuffer,
                facePrioritiesBuffer,
                faceSkinsBuffer,
                faceTypesBuffer,
                vertexSkinsBuffer,
                faceAlphasBuffer,
                faceIndicesBuffer,
                faceColorsBuffer,
                texturesBuffer,
                vertexXBuffer,
                vertexYBuffer,
                vertexZBuffer,
                footerBuffer,
            )
        encodeVertexPositions(
            hasSkeletalBones = false,
            vertexXBuffer,
            vertexYBuffer,
            vertexZBuffer,
            vertexFlagsBuffer,
            vertexSkinsBuffer,
        )
        encodeUnversionedTriangleInfo(
            faceColorsBuffer,
            faceTypesBuffer,
            facePrioritiesBuffer,
            faceAlphasBuffer,
            faceSkinsBuffer,
        )
        encodeVertices(
            faceIndicesBuffer,
            faceIndexTypesBuffer,
        )
        encodeUnversionedTriangles(
            texturesBuffer,
        )
        footerBuffer.writeShort(vertexCount)
        footerBuffer.writeShort(triangleCount)
        footerBuffer.writeByte(textureTriangleCount)
        footerBuffer.writeByte(triangleRenderTypes != null)
        footerBuffer.writeByte(if (triangleRenderPriorities != null) -1 else renderPriority)
        footerBuffer.writeByte(triangleAlphas != null)
        footerBuffer.writeByte(triangleSkins != null)
        footerBuffer.writeByte(vertexSkins != null)
        footerBuffer.writeShort(vertexXBuffer.position())
        footerBuffer.writeShort(vertexYBuffer.position())
        footerBuffer.writeShort(vertexZBuffer.position())
        footerBuffer.writeShort(faceIndicesBuffer.position())
        buffers.forEach {
            masterBuffer.writeBytes(it.toArray())
        }
        return masterBuffer
    }

    private fun encode2(): Writer {
        val masterBuffer = BufferWriter(16 * 1024)
        val faceMappingsBuffer = BufferWriter(128)
        val vertexFlagsBuffer = BufferWriter(128)
        val faceTypesBuffer = BufferWriter(128)
        val faceIndexTypesBuffer = BufferWriter(128)
        val facePrioritiesBuffer = BufferWriter(128)
        val faceSkinsBuffer = BufferWriter(128)
        val vertexSkinsBuffer = BufferWriter(128)
        val faceAlphasBuffer = BufferWriter(128)
        val faceIndicesBuffer = BufferWriter(128)
        val faceMaterialsBuffer = BufferWriter(128)
        val faceTexturesBuffer = BufferWriter(128)
        val faceColorsBuffer = BufferWriter(128)
        val vertexXBuffer = BufferWriter(128)
        val vertexYBuffer = BufferWriter(128)
        val vertexZBuffer = BufferWriter(128)
        val simpleTexturesBuffer = BufferWriter(128)
        val complexTexturesBuffer = BufferWriter(128)
        val textureScaleBuffer = BufferWriter(128)
        val textureRotationBuffer = BufferWriter(128)
        val textureDirectionBuffer = BufferWriter(128)
        val textureTranslationBuffer = BufferWriter(128)
        val particleEffectsBuffer = BufferWriter(128)
        val billboardsBuffer = BufferWriter(128)
        val footerBuffer = BufferWriter(128)
        val buffers =
            listOf(
                faceMappingsBuffer,
                vertexFlagsBuffer,
                faceTypesBuffer,
                faceIndexTypesBuffer,
                facePrioritiesBuffer,
                faceSkinsBuffer,
                vertexSkinsBuffer,
                faceAlphasBuffer,
                faceIndicesBuffer,
                faceMaterialsBuffer,
                faceTexturesBuffer,
                faceColorsBuffer,
                vertexXBuffer,
                vertexYBuffer,
                vertexZBuffer,
                simpleTexturesBuffer,
                complexTexturesBuffer,
                textureScaleBuffer,
                textureRotationBuffer,
                textureDirectionBuffer,
                textureTranslationBuffer,
                particleEffectsBuffer,
                billboardsBuffer,
                footerBuffer,
            )
        if (textureTriangleCount > 0) {
            val textureRenderTypes = requireNotNull(this.textureRenderTypes)
            for (face in 0 until textureTriangleCount) {
                faceMappingsBuffer.writeByte(textureRenderTypes[face])
            }
        }

        encodeVertexPositions(
            hasSkeletalBones = false,
            vertexXBuffer,
            vertexYBuffer,
            vertexZBuffer,
            vertexFlagsBuffer,
            vertexSkinsBuffer,
        )

        val hasFaceTypes = triangleRenderTypes != null
        val hasFacePriorities = triangleRenderPriorities != null
        val hasFaceAlpha = triangleAlphas != null
        val hasFaceSkins = triangleSkins != null
        val hasFaceTextures = triangleTextures != null
        val hasVertexSkins = this.vertexSkins != null
        encodeVersionedTriangleInfo(
            hasFaceTypes,
            hasFacePriorities,
            hasFaceAlpha,
            hasFaceSkins,
            hasFaceTextures,
            faceColorsBuffer,
            faceTypesBuffer,
            facePrioritiesBuffer,
            faceAlphasBuffer,
            faceSkinsBuffer,
            faceMaterialsBuffer,
            faceTexturesBuffer,
        )
        encodeVertices(
            faceIndicesBuffer,
            faceIndexTypesBuffer,
        )
        encodeVersionedTriangles(
            simpleTexturesBuffer,
            complexTexturesBuffer,
            textureScaleBuffer,
            textureRotationBuffer,
            textureDirectionBuffer,
            textureTranslationBuffer,
        )
        val hasParticleEffects = emitters != null || effectors != null
        if (hasParticleEffects) {
            encodeParticles(particleEffectsBuffer)
        }
        val hasBillboards = this.faceBillboards != null
        if (hasBillboards) {
            encodeBillboards(billboardsBuffer)
        }
        val hasVersion = version != UNVERSIONED && version != DEFAULT_VERSION
        if (hasVersion) {
            footerBuffer.writeByte(version)
        }
        footerBuffer.writeShort(vertexCount)
        footerBuffer.writeShort(triangleCount)
        footerBuffer.writeByte(textureTriangleCount)
        var flags = 0
        if (hasFaceTypes) {
            flags = flags or FACE_TYPES_FLAG
        }
        if (hasParticleEffects) {
            flags = flags or PARTICLES_FLAG
        }
        if (hasBillboards) {
            flags = flags or BILLBOARDS_FLAG
        }
        if (hasVersion) {
            flags = flags or VERSION_FLAG
        }
        footerBuffer.writeByte(flags)
        footerBuffer.writeByte(if (hasFacePriorities) -1 else renderPriority)
        footerBuffer.writeByte(hasFaceAlpha)
        footerBuffer.writeByte(hasFaceSkins)
        footerBuffer.writeByte(hasFaceTextures)
        footerBuffer.writeByte(hasVertexSkins)
        footerBuffer.writeShort(vertexXBuffer.position())
        footerBuffer.writeShort(vertexYBuffer.position())
        footerBuffer.writeShort(vertexZBuffer.position())
        footerBuffer.writeShort(faceIndicesBuffer.position())
        footerBuffer.writeShort(faceTexturesBuffer.position())
        buffers.forEach {
            masterBuffer.writeBytes(it.toArray())
        }
        masterBuffer.writeByte(0xFF)
        masterBuffer.writeByte(0xFF)
        return masterBuffer
    }

    private fun encode3(): Writer {
        val masterBuffer = BufferWriter(16 * 1024)
        val vertexFlagsBuffer = BufferWriter(128)
        val faceTypesBuffer = BufferWriter(128)
        val faceIndexTypesBuffer = BufferWriter(128)
        val facePrioritiesBuffer = BufferWriter(128)
        val faceSkinsBuffer = BufferWriter(128)
        val vertexSkinsBuffer = BufferWriter(128)
        val faceAlphasBuffer = BufferWriter(128)
        val faceIndicesBuffer = BufferWriter(128)
        val faceColorsBuffer = BufferWriter(128)
        val vertexXBuffer = BufferWriter(128)
        val vertexYBuffer = BufferWriter(128)
        val vertexZBuffer = BufferWriter(128)
        val texturesBuffer = BufferWriter(128)
        val footerBuffer = BufferWriter(128)
        val hasSkeletalBones = this.skeletalBones != null && this.skeletalScales != null
        val buffers =
            listOf(
                vertexFlagsBuffer,
                faceIndexTypesBuffer,
                facePrioritiesBuffer,
                faceSkinsBuffer,
                faceTypesBuffer,
                vertexSkinsBuffer,
                faceAlphasBuffer,
                faceIndicesBuffer,
                faceColorsBuffer,
                texturesBuffer,
                vertexXBuffer,
                vertexYBuffer,
                vertexZBuffer,
                footerBuffer,
            )
        encodeVertexPositions(
            hasSkeletalBones,
            vertexXBuffer,
            vertexYBuffer,
            vertexZBuffer,
            vertexFlagsBuffer,
            vertexSkinsBuffer,
        )
        encodeUnversionedTriangleInfo(
            faceColorsBuffer,
            faceTypesBuffer,
            facePrioritiesBuffer,
            faceAlphasBuffer,
            faceSkinsBuffer,
        )
        encodeVertices(
            faceIndicesBuffer,
            faceIndexTypesBuffer,
        )
        encodeUnversionedTriangles(
            texturesBuffer,
        )
        footerBuffer.writeShort(vertexCount)
        footerBuffer.writeShort(triangleCount)
        footerBuffer.writeByte(textureTriangleCount)
        footerBuffer.writeByte(triangleRenderTypes != null)
        footerBuffer.writeByte(if (triangleRenderPriorities != null) -1 else renderPriority)
        footerBuffer.writeByte(triangleAlphas != null)
        footerBuffer.writeByte(triangleSkins != null)
        footerBuffer.writeByte(vertexSkins != null)
        footerBuffer.writeByte(hasSkeletalBones)
        footerBuffer.writeShort(vertexXBuffer.position())
        footerBuffer.writeShort(vertexYBuffer.position())
        footerBuffer.writeShort(vertexZBuffer.position())
        footerBuffer.writeShort(faceIndicesBuffer.position())
        footerBuffer.writeShort(vertexSkinsBuffer.position())
        buffers.forEach {
            masterBuffer.writeBytes(it.toArray())
        }
        masterBuffer.writeByte(0xFF)
        masterBuffer.writeByte(0xFE)
        return masterBuffer
    }

    private fun encode4(): Writer {
        val masterBuffer = BufferWriter(16 * 1024)
        val faceMappingsBuffer = BufferWriter(128)
        val vertexFlagsBuffer = BufferWriter(128)
        val faceTypesBuffer = BufferWriter(128)
        val faceIndexTypesBuffer = BufferWriter(128)
        val facePrioritiesBuffer = BufferWriter(128)
        val faceSkinsBuffer = BufferWriter(128)
        val vertexSkinsBuffer = BufferWriter(128)
        val faceAlphasBuffer = BufferWriter(128)
        val faceIndicesBuffer = BufferWriter(128)
        val faceMaterialsBuffer = BufferWriter(128)
        val faceTexturesBuffer = BufferWriter(128)
        val faceColorsBuffer = BufferWriter(128)
        val vertexXBuffer = BufferWriter(128)
        val vertexYBuffer = BufferWriter(128)
        val vertexZBuffer = BufferWriter(128)
        val simpleTexturesBuffer = BufferWriter(128)
        val complexTexturesBuffer = BufferWriter(128)
        val textureScaleBuffer = BufferWriter(128)
        val textureRotationBuffer = BufferWriter(128)
        val textureDirectionBuffer = BufferWriter(128)
        val textureTranslationBuffer = BufferWriter(128)
        val particleEffectsBuffer = BufferWriter(128)
        val billboardsBuffer = BufferWriter(128)
        val footerBuffer = BufferWriter(128)
        val hasSkeletalBones = this.skeletalBones != null && this.skeletalScales != null
        val buffers =
            listOf(
                faceMappingsBuffer,
                vertexFlagsBuffer,
                faceTypesBuffer,
                faceIndexTypesBuffer,
                facePrioritiesBuffer,
                faceSkinsBuffer,
                vertexSkinsBuffer,
                faceAlphasBuffer,
                faceIndicesBuffer,
                faceMaterialsBuffer,
                faceTexturesBuffer,
                faceColorsBuffer,
                vertexXBuffer,
                vertexYBuffer,
                vertexZBuffer,
                simpleTexturesBuffer,
                complexTexturesBuffer,
                textureScaleBuffer,
                textureRotationBuffer,
                textureDirectionBuffer,
                textureTranslationBuffer,
                particleEffectsBuffer,
                billboardsBuffer,
                footerBuffer,
            )
        if (textureTriangleCount > 0) {
            val textureRenderTypes = requireNotNull(this.textureRenderTypes)
            for (face in 0 until textureTriangleCount) {
                faceMappingsBuffer.writeByte(textureRenderTypes[face])
            }
        }

        encodeVertexPositions(
            hasSkeletalBones,
            vertexXBuffer,
            vertexYBuffer,
            vertexZBuffer,
            vertexFlagsBuffer,
            vertexSkinsBuffer,
        )

        val hasFaceTypes = triangleRenderTypes != null
        val hasFacePriorities = triangleRenderPriorities != null
        val hasFaceAlpha = triangleAlphas != null
        val hasFaceSkins = triangleSkins != null
        val hasFaceTextures = triangleTextures != null
        val hasVertexSkins = this.vertexSkins != null
        encodeVersionedTriangleInfo(
            hasFaceTypes,
            hasFacePriorities,
            hasFaceAlpha,
            hasFaceSkins,
            hasFaceTextures,
            faceColorsBuffer,
            faceTypesBuffer,
            facePrioritiesBuffer,
            faceAlphasBuffer,
            faceSkinsBuffer,
            faceMaterialsBuffer,
            faceTexturesBuffer,
        )
        encodeVertices(
            faceIndicesBuffer,
            faceIndexTypesBuffer,
        )
        encodeVersionedTriangles(
            simpleTexturesBuffer,
            complexTexturesBuffer,
            textureScaleBuffer,
            textureRotationBuffer,
            textureDirectionBuffer,
            textureTranslationBuffer,
        )
        val hasParticleEffects = emitters != null || effectors != null
        if (hasParticleEffects) {
            encodeParticles(particleEffectsBuffer)
        }
        val hasBillboards = this.faceBillboards != null
        if (hasBillboards) {
            encodeBillboards(billboardsBuffer)
        }
        val hasVersion = version != UNVERSIONED && version != DEFAULT_VERSION
        if (hasVersion) {
            footerBuffer.writeByte(version)
        }
        footerBuffer.writeShort(vertexCount)
        footerBuffer.writeShort(triangleCount)
        footerBuffer.writeByte(textureTriangleCount)
        var flags = 0
        if (hasFaceTypes) {
            flags = flags or FACE_TYPES_FLAG
        }
        if (hasParticleEffects) {
            flags = flags or PARTICLES_FLAG
        }
        if (hasBillboards) {
            flags = flags or BILLBOARDS_FLAG
        }
        if (hasVersion) {
            flags = flags or VERSION_FLAG
        }
        footerBuffer.writeByte(flags)
        footerBuffer.writeByte(if (hasFacePriorities) -1 else renderPriority)
        footerBuffer.writeByte(hasFaceAlpha)
        footerBuffer.writeByte(hasFaceSkins)
        footerBuffer.writeByte(hasFaceTextures)
        footerBuffer.writeByte(hasVertexSkins)
        footerBuffer.writeByte(hasSkeletalBones)
        footerBuffer.writeShort(vertexXBuffer.position())
        footerBuffer.writeShort(vertexYBuffer.position())
        footerBuffer.writeShort(vertexZBuffer.position())
        footerBuffer.writeShort(faceIndicesBuffer.position())
        footerBuffer.writeShort(faceTexturesBuffer.position())
        footerBuffer.writeShort(vertexSkinsBuffer.position())
        buffers.forEach {
            masterBuffer.writeBytes(it.toArray())
        }
        masterBuffer.writeByte(0xFF)
        masterBuffer.writeByte(0xFD)
        return masterBuffer
    }

    private fun encodeBillboards(billboardsBuffer: Writer) {
        val faceBillboards = requireNotNull(this.faceBillboards)
        billboardsBuffer.writeByte(faceBillboards.size)
        for (billboard in faceBillboards) {
            billboardsBuffer.writeShort(billboard.id)
            billboardsBuffer.writeShort(billboard.face)
            billboardsBuffer.writeByte(billboard.skin)
            billboardsBuffer.writeByte(billboard.distance)
        }
    }

    private fun encodeParticles(particleEffectsBuffer: Writer) {
        val numEmitters = emitters?.size ?: 0
        particleEffectsBuffer.writeByte(numEmitters)
        if (numEmitters > 0) {
            val emitters = requireNotNull(this.emitters)
            for (index in 0 until numEmitters) {
                val (emitter, face) = emitters[index]
                particleEffectsBuffer.writeShort(emitter)
                particleEffectsBuffer.writeShort(face)
            }
        }
        val numEffectors = effectors?.size ?: 0
        particleEffectsBuffer.writeByte(numEffectors)
        if (numEffectors > 0) {
            val effectors = requireNotNull(this.effectors)
            for (index in 0 until numEffectors) {
                val (effector, vertex1) = effectors[index]
                particleEffectsBuffer.writeShort(effector)
                particleEffectsBuffer.writeShort(vertex1)
            }
        }
    }

    private fun encodeUnversionedTriangleInfo(
        faceColoursBuf: Writer,
        faceTypesBuf: Writer,
        facePrioritiesBuf: Writer,
        faceAlphasBuf: Writer,
        faceSkinsBuf: Writer,
    ) {
        if (triangleCount <= 0) return
        val triangleColors = requireNotNull(this.triangleColors)
        val hasTriangleRenderTypes = triangleRenderTypes != null || this.textureCoordinates != null || this.triangleTextures != null
        val hasTriangleRenderPriorities = triangleRenderPriorities != null
        val hasTriangleAlphas = triangleAlphas != null
        val hasTriangleSkins = triangleSkins != null
        for (index in 0 until triangleCount) {
            faceColoursBuf.writeShort(triangleColors[index].toInt())
            if (hasTriangleRenderTypes) {
                val faceType = this.triangleRenderTypes?.get(index) ?: 0
                val triangleCoordinate = this.textureCoordinates?.get(index) ?: -1
                val triangleTexture = this.triangleTextures?.get(index) ?: -1
                var flag = 0x0
                if (faceType == 1) {
                    flag = flag or USES_FACE_TYPES_FLAG
                }
                if (triangleCoordinate != -1 || triangleTexture != -1) {
                    flag = flag or (triangleCoordinate shl 2)
                    flag = flag or USES_MATERIALS_FLAG
                }
                faceTypesBuf.writeByte(flag)
            }
            if (hasTriangleRenderPriorities) {
                val triangleRenderPriorities = requireNotNull(this.triangleRenderPriorities)
                facePrioritiesBuf.writeByte(triangleRenderPriorities[index])
            }
            if (hasTriangleAlphas) {
                val triangleAlphas = requireNotNull(this.triangleAlphas)
                faceAlphasBuf.writeByte(triangleAlphas[index])
            }
            if (hasTriangleSkins) {
                val triangleSkins = requireNotNull(this.triangleSkins)
                faceSkinsBuf.writeByte(triangleSkins[index])
            }
        }
    }

    private fun encodeVersionedTriangleInfo(
        hasFaceTypes: Boolean,
        hasFacePriorities: Boolean,
        hasFaceAlpha: Boolean,
        hasFaceSkins: Boolean,
        hasFaceTextures: Boolean,
        faceColorsBuffer: Writer,
        faceTypesBuffer: Writer,
        facePrioritiesBuffer: Writer,
        faceAlphasBuffer: Writer,
        faceSkinsBuffer: Writer,
        faceMaterialsBuffer: Writer,
        faceTexturesBuffer: Writer,
    ) {
        if (triangleCount <= 0) return
        val triangleColors = requireNotNull(this.triangleColors)
        val textureCoordinates = this.textureCoordinates
        for (face in 0 until triangleCount) {
            faceColorsBuffer.writeShort(triangleColors[face].toInt())
            if (hasFaceTypes) {
                val triangleRenderTypes = requireNotNull(this.triangleRenderTypes)
                faceTypesBuffer.writeByte(triangleRenderTypes[face])
            }
            if (hasFacePriorities) {
                val triangleRenderPriorities = requireNotNull(this.triangleRenderPriorities)
                facePrioritiesBuffer.writeByte(triangleRenderPriorities[face])
            }
            if (hasFaceAlpha) {
                val triangleAlphas = requireNotNull(this.triangleAlphas)
                faceAlphasBuffer.writeByte(triangleAlphas[face])
            }
            if (hasFaceSkins) {
                val triangleSkins = requireNotNull(this.triangleSkins)
                faceSkinsBuffer.writeByte(triangleSkins[face])
            }
            if (hasFaceTextures) {
                val triangleTextures = requireNotNull(this.triangleTextures)
                faceMaterialsBuffer.writeShort(triangleTextures[face] + 1)
            }
            if (textureCoordinates != null) {
                val triangleTextures = requireNotNull(this.triangleTextures)
                if (triangleTextures[face] != -1) {
                    faceTexturesBuffer.writeByte(textureCoordinates[face] + 1)
                }
            }
        }
    }

    private fun encodeVertexPositions(
        hasSkeletalBones: Boolean,
        vertexXBuffer: Writer,
        vertexYBuffer: Writer,
        vertexZBuffer: Writer,
        vertexFlagsBuffer: Writer,
        vertexSkinsBuffer: Writer,
    ) {
        if (vertexCount <= 0) return
        val vertexPositionsX = requireNotNull(this.vertexPositionsX)
        val vertexPositionsY = requireNotNull(this.vertexPositionsY)
        val vertexPositionsZ = requireNotNull(this.vertexPositionsZ)
        var baseX = 0
        var baseY = 0
        var baseZ = 0
        for (vertex in 0 until vertexCount) {
            val x = vertexPositionsX[vertex]
            val y = vertexPositionsY[vertex]
            val z = vertexPositionsZ[vertex]
            val xOffset = x - baseX
            val yOffset = y - baseY
            val zOffset = z - baseZ
            var flag = 0
            if (xOffset != 0) {
                flag = flag or X_POS_FLAG
                vertexXBuffer.pSmart1or2s(xOffset)
            }
            if (yOffset != 0) {
                flag = flag or Y_POS_FLAG
                vertexYBuffer.pSmart1or2s(yOffset)
            }
            if (zOffset != 0) {
                flag = flag or Z_POS_FLAG
                vertexZBuffer.pSmart1or2s(zOffset)
            }
            vertexFlagsBuffer.writeByte(flag)
            baseX += xOffset
            baseY += yOffset
            baseZ += zOffset
            val vertexSkins = this.vertexSkins
            if (vertexSkins != null) {
                val weight = vertexSkins[vertex]
                vertexSkinsBuffer.writeByte(weight)
            }
        }
        if (hasSkeletalBones) {
            val skeletalBones = requireNotNull(this.skeletalBones)
            val skeletalScales = requireNotNull(this.skeletalScales)
            require(skeletalBones.size == skeletalScales.size)
            for (index in 0 until vertexCount) {
                val bones = requireNotNull(skeletalBones[index])
                val scales = requireNotNull(skeletalScales[index])
                require(bones.size == scales.size)
                vertexSkinsBuffer.writeByte(bones.size)
                for (i in bones.indices) {
                    vertexSkinsBuffer.writeByte(bones[i])
                    vertexSkinsBuffer.writeByte(scales[i])
                }
            }
        }
    }

    private fun encodeUnversionedTriangles(texturesBuf: Writer) {
        if (textureTriangleCount <= 0) return
        val textureTriangleVertex1 = requireNotNull(this.textureTriangleVertex1)
        val textureTriangleVertex2 = requireNotNull(this.textureTriangleVertex2)
        val textureTriangleVertex3 = requireNotNull(this.textureTriangleVertex3)
        for (index in 0 until textureTriangleCount) {
            texturesBuf.writeShort(textureTriangleVertex1[index])
            texturesBuf.writeShort(textureTriangleVertex2[index])
            texturesBuf.writeShort(textureTriangleVertex3[index])
        }
    }

    private fun encodeVersionedTriangles(
        simple: Writer,
        complex: Writer,
        scale: Writer,
        rotation: Writer,
        direction: Writer,
        translation: Writer,
    ) {
        if (textureTriangleCount <= 0) return
        val textureRenderTypes = requireNotNull(this.textureRenderTypes)
        val textureTriangleVertex1 = requireNotNull(this.textureTriangleVertex1)
        val textureTriangleVertex2 = requireNotNull(this.textureTriangleVertex2)
        val textureTriangleVertex3 = requireNotNull(this.textureTriangleVertex3)
        var version = this.version
        for (face in 0 until textureTriangleCount) {
            val type = textureRenderTypes[face] and 0xFF
            if (type == SIMPLE_TEXTURE) {
                simple.writeShort(textureTriangleVertex1[face])
                simple.writeShort(textureTriangleVertex2[face])
                simple.writeShort(textureTriangleVertex3[face])
                continue
            }
            val scaleX = requireNotNull(textureScaleX)[face]
            val scaleY = requireNotNull(textureScaleY)[face]
            val scaleZ = requireNotNull(textureScaleZ)[face]
            val textureSpeed = requireNotNull(this.textureSpeed)
            val textureRotation = requireNotNull(this.textureRotation)
            val textureDirection = requireNotNull(this.textureDirection)
            when (type) {
                CYLINDRICAL_TEXTURE -> {
                    complex.writeShort(textureTriangleVertex1[face])
                    complex.writeShort(textureTriangleVertex2[face])
                    complex.writeShort(textureTriangleVertex3[face])
                    if (version >= 15 || scaleX > 0xffff || scaleZ > 0xffff) {
                        if (version < 15) {
                            version = 15
                        }
                        val speed = textureSpeed[face]
                        scale.writeMedium(scaleZ)
                        scale.writeMedium(speed)
                        scale.writeMedium(scaleX)
                    } else {
                        scale.writeShort(scaleZ)
                        val speed = textureSpeed[face]
                        if (version < 14 && speed > 0xffff) {
                            version = 14
                        }
                        if (version < 14) {
                            scale.writeShort(speed)
                        } else {
                            scale.writeMedium(speed)
                        }
                        scale.writeShort(scaleX)
                    }
                    rotation.writeByte(textureRotation[face])
                    direction.writeByte(scaleY)
                    translation.writeByte(textureDirection[face])
                }

                CUBE_TEXTURE -> {
                    val textureTransU = requireNotNull(this.textureTransU)
                    val textureTransV = requireNotNull(this.textureTransV)
                    complex.writeShort(textureTriangleVertex1[face])
                    complex.writeShort(textureTriangleVertex2[face])
                    complex.writeShort(textureTriangleVertex3[face])
                    if (version >= 15 || scaleX > 0xffff || scaleZ > 0xffff) {
                        if (version < 15) {
                            version = 15
                        }
                        val speed = textureSpeed[face]
                        scale.writeMedium(scaleZ)
                        scale.writeMedium(speed)
                        scale.writeMedium(scaleX)
                    } else {
                        scale.writeShort(scaleZ)
                        val speed = textureSpeed[face]
                        if (version < 14 && speed > 0xffff) {
                            version = 14
                        }
                        if (version < 14) {
                            scale.writeShort(speed)
                        } else {
                            scale.writeMedium(speed)
                        }
                        scale.writeShort(scaleX)
                    }
                    rotation.writeByte(textureRotation[face])
                    direction.writeByte(scaleY)
                    translation.writeByte(textureDirection[face])
                    translation.writeByte(textureTransU[face])
                    translation.writeByte(textureTransV[face])
                }

                SPHERICAL_TEXTURE -> {
                    complex.writeShort(textureTriangleVertex1[face])
                    complex.writeShort(textureTriangleVertex2[face])
                    complex.writeShort(textureTriangleVertex3[face])
                    if (version >= 15 || scaleX > 0xFFFF || scaleZ > 0xFFFF) {
                        if (version < 15) {
                            version = 15
                        }
                        val speed = textureSpeed[face]
                        scale.writeMedium(scaleZ)
                        scale.writeMedium(speed)
                        scale.writeMedium(scaleX)
                    } else {
                        scale.writeShort(scaleZ)
                        val speed = textureSpeed[face]
                        if (version < 14 && speed > 0xFFFF) {
                            version = 14
                        }
                        if (version < 14) {
                            scale.writeShort(speed)
                        } else {
                            scale.writeMedium(speed)
                        }
                        scale.writeShort(scaleX)
                    }
                    rotation.writeByte(textureRotation[face])
                    direction.writeByte(scaleY)
                    translation.writeByte(textureDirection[face])
                }
            }
        }
    }

    private fun encodeVertices(ibuffer: Writer, tbuffer: Writer) {
        if (triangleCount <= 0) return
        val triangleVertex1 = requireNotNull(this.triangleVertex1)
        val triangleVertex2 = requireNotNull(this.triangleVertex2)
        val triangleVertex3 = requireNotNull(this.triangleVertex3)
        var lastA = 0
        var lastB = 0
        var lastC = 0
        var accum = 0
        for (index in 0 until triangleCount) {
            val a = triangleVertex1[index]
            val b = triangleVertex2[index]
            val c = triangleVertex3[index]

            val compression =
                when {
                    a == lastA && b == lastC -> {
                        ibuffer.pSmart1or2s(c - accum)
                        accum = c
                        2
                    }
                    a == lastC && b == lastB -> {
                        ibuffer.pSmart1or2s(c - accum)
                        accum = c
                        3
                    }
                    a == lastB && b == lastA -> {
                        ibuffer.pSmart1or2s(c - accum)
                        accum = c
                        4
                    }
                    else -> {
                        ibuffer.pSmart1or2s(a - accum)
                        accum = a
                        ibuffer.pSmart1or2s(b - accum)
                        accum = b
                        ibuffer.pSmart1or2s(c - accum)
                        accum = c
                        1
                    }
                }
            lastA = a
            lastB = b
            lastC = c
            tbuffer.writeByte(compression)
        }
    }

    override fun toString(): String {
        return "Model(" +
            "id=$id, " +
            "vertexCount=$vertexCount, " +
            "triangleCount=$triangleCount, " +
            "textureTriangleCount=$textureTriangleCount, " +
            "version=$version, " +
            "type=$type, " +
            "renderPriority=$renderPriority, " +
            "triangleColors=${triangleColors?.contentToString()}, " +
            "triangleAlphas=${triangleAlphas?.contentToString()}, " +
            "triangleSkins=${triangleSkins?.contentToString()}, " +
            "triangleRenderTypes=${triangleRenderTypes?.contentToString()}, " +
            "triangleRenderPriorities=${triangleRenderPriorities?.contentToString()}, " +
            "triangleVertex1=${triangleVertex1?.contentToString()}, " +
            "triangleVertex2=${triangleVertex2?.contentToString()}, " +
            "triangleVertex3=${triangleVertex3?.contentToString()}, " +
            "vertexPositionsX=${vertexPositionsX?.contentToString()}, " +
            "vertexPositionsY=${vertexPositionsY?.contentToString()}, " +
            "vertexPositionsZ=${vertexPositionsZ?.contentToString()}, " +
            "vertexSkins=${vertexSkins?.contentToString()}, " +
            "triangleTextures=${triangleTextures?.contentToString()}, " +
            "textureRenderTypes=${textureRenderTypes?.contentToString()}, " +
            "textureTriangleVertex1=${textureTriangleVertex1?.contentToString()}, " +
            "textureTriangleVertex2=${textureTriangleVertex2?.contentToString()}, " +
            "textureTriangleVertex3=${textureTriangleVertex3?.contentToString()}, " +
            "textureScaleX=${textureScaleX?.contentToString()}, " +
            "textureScaleY=${textureScaleY?.contentToString()}, " +
            "textureScaleZ=${textureScaleZ?.contentToString()}, " +
            "textureRotation=${textureRotation?.contentToString()}, " +
            "textureDirection=${textureDirection?.contentToString()}, " +
            "textureSpeed=${textureSpeed?.contentToString()}, " +
            "textureTransU=${textureTransU?.contentToString()}, " +
            "textureTransV=${textureTransV?.contentToString()}, " +
            "textureCoordinates=${textureCoordinates?.contentToString()}, " +
            "skeletalBones=${skeletalBones?.contentToString()}, " +
            "skeletalScales=${skeletalScales?.contentToString()}, " +
            "emitters=${emitters?.contentToString()}, " +
            "effectors=${effectors?.contentToString()}, " +
            "faceBillboards=${faceBillboards?.contentToString()}" +
            ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Model

        if (id != other.id) return false
        if (vertexCount != other.vertexCount) return false
        if (triangleCount != other.triangleCount) return false
        if (textureTriangleCount != other.textureTriangleCount) return false
        if (version != other.version) return false
        if (type != other.type) return false
        if (renderPriority != other.renderPriority) return false
        if (triangleColors != null) {
            if (other.triangleColors == null) return false
            if (!triangleColors.contentEquals(other.triangleColors)) return false
        } else if (other.triangleColors != null) {
            return false
        }
        if (triangleAlphas != null) {
            if (other.triangleAlphas == null) return false
            if (!triangleAlphas.contentEquals(other.triangleAlphas)) return false
        } else if (other.triangleAlphas != null) {
            return false
        }
        if (triangleSkins != null) {
            if (other.triangleSkins == null) return false
            if (!triangleSkins.contentEquals(other.triangleSkins)) return false
        } else if (other.triangleSkins != null) {
            return false
        }
        if (triangleRenderTypes != null) {
            if (other.triangleRenderTypes == null) return false
            if (!triangleRenderTypes.contentEquals(other.triangleRenderTypes)) return false
        } else if (other.triangleRenderTypes != null) {
            return false
        }
        if (triangleRenderPriorities != null) {
            if (other.triangleRenderPriorities == null) return false
            if (!triangleRenderPriorities.contentEquals(other.triangleRenderPriorities)) return false
        } else if (other.triangleRenderPriorities != null) {
            return false
        }
        if (triangleVertex1 != null) {
            if (other.triangleVertex1 == null) return false
            if (!triangleVertex1.contentEquals(other.triangleVertex1)) return false
        } else if (other.triangleVertex1 != null) {
            return false
        }
        if (triangleVertex2 != null) {
            if (other.triangleVertex2 == null) return false
            if (!triangleVertex2.contentEquals(other.triangleVertex2)) return false
        } else if (other.triangleVertex2 != null) {
            return false
        }
        if (triangleVertex3 != null) {
            if (other.triangleVertex3 == null) return false
            if (!triangleVertex3.contentEquals(other.triangleVertex3)) return false
        } else if (other.triangleVertex3 != null) {
            return false
        }
        if (vertexPositionsX != null) {
            if (other.vertexPositionsX == null) return false
            if (!vertexPositionsX.contentEquals(other.vertexPositionsX)) return false
        } else if (other.vertexPositionsX != null) {
            return false
        }
        if (vertexPositionsY != null) {
            if (other.vertexPositionsY == null) return false
            if (!vertexPositionsY.contentEquals(other.vertexPositionsY)) return false
        } else if (other.vertexPositionsY != null) {
            return false
        }
        if (vertexPositionsZ != null) {
            if (other.vertexPositionsZ == null) return false
            if (!vertexPositionsZ.contentEquals(other.vertexPositionsZ)) return false
        } else if (other.vertexPositionsZ != null) {
            return false
        }
        if (vertexSkins != null) {
            if (other.vertexSkins == null) return false
            if (!vertexSkins.contentEquals(other.vertexSkins)) return false
        } else if (other.vertexSkins != null) {
            return false
        }
        if (triangleTextures != null) {
            if (other.triangleTextures == null) return false
            if (!triangleTextures.contentEquals(other.triangleTextures)) return false
        } else if (other.triangleTextures != null) {
            return false
        }
        if (textureRenderTypes != null) {
            if (other.textureRenderTypes == null) return false
            if (!textureRenderTypes.contentEquals(other.textureRenderTypes)) return false
        } else if (other.textureRenderTypes != null) {
            return false
        }
        if (textureTriangleVertex1 != null) {
            if (other.textureTriangleVertex1 == null) return false
            if (!textureTriangleVertex1.contentEquals(other.textureTriangleVertex1)) return false
        } else if (other.textureTriangleVertex1 != null) {
            return false
        }
        if (textureTriangleVertex2 != null) {
            if (other.textureTriangleVertex2 == null) return false
            if (!textureTriangleVertex2.contentEquals(other.textureTriangleVertex2)) return false
        } else if (other.textureTriangleVertex2 != null) {
            return false
        }
        if (textureTriangleVertex3 != null) {
            if (other.textureTriangleVertex3 == null) return false
            if (!textureTriangleVertex3.contentEquals(other.textureTriangleVertex3)) return false
        } else if (other.textureTriangleVertex3 != null) {
            return false
        }
        if (textureScaleX != null) {
            if (other.textureScaleX == null) return false
            if (!textureScaleX.contentEquals(other.textureScaleX)) return false
        } else if (other.textureScaleX != null) {
            return false
        }
        if (textureScaleY != null) {
            if (other.textureScaleY == null) return false
            if (!textureScaleY.contentEquals(other.textureScaleY)) return false
        } else if (other.textureScaleY != null) {
            return false
        }
        if (textureScaleZ != null) {
            if (other.textureScaleZ == null) return false
            if (!textureScaleZ.contentEquals(other.textureScaleZ)) return false
        } else if (other.textureScaleZ != null) {
            return false
        }
        if (textureRotation != null) {
            if (other.textureRotation == null) return false
            if (!textureRotation.contentEquals(other.textureRotation)) return false
        } else if (other.textureRotation != null) {
            return false
        }
        if (textureDirection != null) {
            if (other.textureDirection == null) return false
            if (!textureDirection.contentEquals(other.textureDirection)) return false
        } else if (other.textureDirection != null) {
            return false
        }
        if (textureSpeed != null) {
            if (other.textureSpeed == null) return false
            if (!textureSpeed.contentEquals(other.textureSpeed)) return false
        } else if (other.textureSpeed != null) {
            return false
        }
        if (textureTransU != null) {
            if (other.textureTransU == null) return false
            if (!textureTransU.contentEquals(other.textureTransU)) return false
        } else if (other.textureTransU != null) {
            return false
        }
        if (textureTransV != null) {
            if (other.textureTransV == null) return false
            if (!textureTransV.contentEquals(other.textureTransV)) return false
        } else if (other.textureTransV != null) {
            return false
        }
        if (textureCoordinates != null) {
            if (other.textureCoordinates == null) return false
            if (!textureCoordinates.contentEquals(other.textureCoordinates)) return false
        } else if (other.textureCoordinates != null) {
            return false
        }
        if (skeletalBones != null) {
            if (other.skeletalBones == null) return false
            if (!skeletalBones.contentDeepEquals(other.skeletalBones)) return false
        } else if (other.skeletalBones != null) {
            return false
        }
        if (skeletalScales != null) {
            if (other.skeletalScales == null) return false
            if (!skeletalScales.contentDeepEquals(other.skeletalScales)) return false
        } else if (other.skeletalScales != null) {
            return false
        }
        if (emitters != null) {
            if (other.emitters == null) return false
            if (!emitters.contentEquals(other.emitters)) return false
        } else if (other.emitters != null) {
            return false
        }
        if (effectors != null) {
            if (other.effectors == null) return false
            if (!effectors.contentEquals(other.effectors)) return false
        } else if (other.effectors != null) {
            return false
        }
        if (faceBillboards != null) {
            if (other.faceBillboards == null) return false
            if (!faceBillboards.contentEquals(other.faceBillboards)) return false
        } else if (other.faceBillboards != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + vertexCount
        result = 31 * result + triangleCount
        result = 31 * result + textureTriangleCount
        result = 31 * result + version
        result = 31 * result + type.hashCode()
        result = 31 * result + renderPriority
        result = 31 * result + (triangleColors?.contentHashCode() ?: 0)
        result = 31 * result + (triangleAlphas?.contentHashCode() ?: 0)
        result = 31 * result + (triangleSkins?.contentHashCode() ?: 0)
        result = 31 * result + (triangleRenderTypes?.contentHashCode() ?: 0)
        result = 31 * result + (triangleRenderPriorities?.contentHashCode() ?: 0)
        result = 31 * result + (triangleVertex1?.contentHashCode() ?: 0)
        result = 31 * result + (triangleVertex2?.contentHashCode() ?: 0)
        result = 31 * result + (triangleVertex3?.contentHashCode() ?: 0)
        result = 31 * result + (vertexPositionsX?.contentHashCode() ?: 0)
        result = 31 * result + (vertexPositionsY?.contentHashCode() ?: 0)
        result = 31 * result + (vertexPositionsZ?.contentHashCode() ?: 0)
        result = 31 * result + (vertexSkins?.contentHashCode() ?: 0)
        result = 31 * result + (triangleTextures?.contentHashCode() ?: 0)
        result = 31 * result + (textureRenderTypes?.contentHashCode() ?: 0)
        result = 31 * result + (textureTriangleVertex1?.contentHashCode() ?: 0)
        result = 31 * result + (textureTriangleVertex2?.contentHashCode() ?: 0)
        result = 31 * result + (textureTriangleVertex3?.contentHashCode() ?: 0)
        result = 31 * result + (textureScaleX?.contentHashCode() ?: 0)
        result = 31 * result + (textureScaleY?.contentHashCode() ?: 0)
        result = 31 * result + (textureScaleZ?.contentHashCode() ?: 0)
        result = 31 * result + (textureRotation?.contentHashCode() ?: 0)
        result = 31 * result + (textureDirection?.contentHashCode() ?: 0)
        result = 31 * result + (textureSpeed?.contentHashCode() ?: 0)
        result = 31 * result + (textureTransU?.contentHashCode() ?: 0)
        result = 31 * result + (textureTransV?.contentHashCode() ?: 0)
        result = 31 * result + (textureCoordinates?.contentHashCode() ?: 0)
        result = 31 * result + (skeletalBones?.contentDeepHashCode() ?: 0)
        result = 31 * result + (skeletalScales?.contentDeepHashCode() ?: 0)
        result = 31 * result + (emitters?.contentHashCode() ?: 0)
        result = 31 * result + (effectors?.contentHashCode() ?: 0)
        result = 31 * result + (faceBillboards?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        private const val DOWNSCALE_FACTOR = 2
        private const val UNVERSIONED = -1
        private const val DEFAULT_VERSION = 12

        private const val FACE_TYPES_FLAG = 0x1
        private const val PARTICLES_FLAG = 0x2
        private const val BILLBOARDS_FLAG = 0x4
        private const val VERSION_FLAG = 0x8

        private const val SIMPLE_TEXTURE = 0x0
        private const val CYLINDRICAL_TEXTURE = 0x1
        private const val CUBE_TEXTURE = 0x2
        private const val SPHERICAL_TEXTURE = 0x3
        private val COMPLEX_TEXTURE_RANGE = 1..3

        private const val X_POS_FLAG = 0x1
        private const val Y_POS_FLAG = 0x2
        private const val Z_POS_FLAG = 0x4

        private const val USES_FACE_TYPES_FLAG = 0x1
        private const val USES_MATERIALS_FLAG = 0x2

        private const val UNIT = Math.PI / 1024.0

        val SINE =
            IntArray(2048) {
                (65536.0 * sin(it * UNIT)).toInt()
            }

        val COSINE =
            IntArray(2048) {
                (65536.0 * cos(it * UNIT)).toInt()
            }

        fun decode(
            id: Int,
            data: ByteArray,
            vararg options: MeshDecodingOption,
        ): Model {
            val model = Model(id)
            try {
                model.decode(data, *options)
            } catch (e: Exception) {
                println(model.version)
                println(model)
                throw e
            }
            return model
        }

        fun merge(
            id: Int,
            models: Collection<Model>,
        ): Model {
            // We don't necessarily need to go with version 15 right away, however that is guaranteed to support it all.
            if (models.any { it.version > DEFAULT_VERSION }) {
                for (model in models) {
                    if (model.version <= DEFAULT_VERSION) {
                        model.upscale(2)
                        model.version = 15
                    }
                }
            }
            val model = Model(id)
            model.append(models)
            if (model.skeletalBones != null) {
                model.type = MeshType.VersionedSkeletal
            } else {
                model.type = MeshType.Versioned
            }
            return model
        }

        private fun Model.append(models: Collection<Model>) {
            vertexCount = 0
            triangleCount = 0
            renderPriority = 0
            var hasTriangleRenderTypes = false
            var hasTriangleRenderPriorities = false
            var hasTriangleAlphas = false
            var hasTriangleSkins = false
            var hasTriangleTextures = false
            var hasTextureCoordinates = false
            var hasSkeletalInfo = false
            vertexCount = 0
            triangleCount = 0
            textureTriangleCount = 0
            renderPriority = -1

            var emittersCount = 0
            var effectorsCount = 0
            var billboardsCount = 0
            for (model in models) {
                vertexCount += model.vertexCount
                triangleCount += model.triangleCount
                textureTriangleCount += model.textureTriangleCount
                if (model.effectors != null) {
                    effectorsCount += model.effectors!!.size
                }
                if (model.faceBillboards != null) {
                    billboardsCount += model.faceBillboards!!.size
                }
                if (model.emitters != null) {
                    emittersCount += model.emitters!!.size
                }
                if (model.triangleRenderPriorities != null) {
                    hasTriangleRenderPriorities = true
                } else {
                    if (renderPriority == -1) {
                        renderPriority = model.renderPriority
                    }
                    if (renderPriority != model.renderPriority) {
                        hasTriangleRenderPriorities = true
                    }
                }

                hasTriangleRenderTypes = hasTriangleRenderTypes or (model.triangleRenderTypes != null)
                hasTriangleAlphas = hasTriangleAlphas or (model.triangleAlphas != null)
                hasTriangleSkins = hasTriangleSkins or (model.triangleSkins != null)
                hasTriangleTextures = hasTriangleTextures or (model.triangleTextures != null)
                hasTextureCoordinates = hasTextureCoordinates or (model.textureCoordinates != null)
                hasSkeletalInfo = hasSkeletalInfo or (model.skeletalBones != null)
            }

            vertexPositionsX = IntArray(vertexCount)
            vertexPositionsY = IntArray(vertexCount)
            vertexPositionsZ = IntArray(vertexCount)
            vertexSkins = IntArray(vertexCount)
            vertexPartMasks = ShortArray(vertexCount)
            triangleVertex1 = IntArray(triangleCount)
            triangleVertex2 = IntArray(triangleCount)
            triangleVertex3 = IntArray(triangleCount)
            var effectors: Array<EffectiveVertex?>? = null
            var emitters: Array<EmissiveTriangle?>? = null
            var billboards: Array<FaceBillboard?>? = null
            if (effectorsCount > 0) {
                effectors = arrayOfNulls(effectorsCount)
            }
            if (emittersCount > 0) {
                emitters = arrayOfNulls(emittersCount)
            }
            if (billboardsCount > 0) {
                billboards = arrayOfNulls(billboardsCount)
            }
            if (hasTriangleRenderTypes) {
                triangleRenderTypes = IntArray(triangleCount)
            }

            if (hasTriangleRenderPriorities) {
                triangleRenderPriorities = IntArray(triangleCount)
            }

            if (hasTriangleAlphas) {
                triangleAlphas = IntArray(triangleCount)
            }

            if (hasTriangleSkins) {
                triangleSkins = IntArray(triangleCount)
            }

            if (hasTriangleTextures) {
                triangleTextures = IntArray(triangleCount)
            }

            if (hasTextureCoordinates) {
                textureCoordinates = IntArray(triangleCount)
            }

            if (hasSkeletalInfo) {
                skeletalBones = arrayOfNulls(vertexCount)
                skeletalScales = arrayOfNulls(vertexCount)
            }

            triangleColors = ShortArray(triangleCount)
            if (textureTriangleCount > 0) {
                textureScaleZ = IntArray(textureTriangleCount)
                textureRotation = IntArray(textureTriangleCount)
                textureScaleY = IntArray(textureTriangleCount)
                textureTriangleVertex3 = IntArray(textureTriangleCount)
                textureTriangleVertex1 = IntArray(textureTriangleCount)
                textureScaleX = IntArray(textureTriangleCount)
                textureTriangleVertex2 = IntArray(textureTriangleCount)
                textureTransU = IntArray(textureTriangleCount)
                textureRenderTypes = IntArray(textureTriangleCount)
                textureDirection = IntArray(textureTriangleCount)
                textureSpeed = IntArray(textureTriangleCount)
                textureTransV = IntArray(textureTriangleCount)
            }

            vertexCount = 0
            triangleCount = 0
            textureTriangleCount = 0
            emittersCount = 0
            effectorsCount = 0
            billboardsCount = 0
            for ((modelIndex, model) in models.withIndex()) {
                val partMask = (1 shl modelIndex).toShort()
                for (index in 0 until model.triangleCount) {
                    if (model.faceBillboards != null) {
                        for (i in 0 until model.faceBillboards!!.size) {
                            val billboard = model.faceBillboards!![i]
                            billboards!![billboardsCount++] = FaceBillboard(billboard.id, billboard.face + triangleCount, billboard.skin, billboard.distance)
                        }
                    }
                    if (hasTriangleRenderTypes && model.triangleRenderTypes != null) {
                        triangleRenderTypes!![triangleCount] = model.triangleRenderTypes!![index]
                    }
                    if (hasTriangleRenderPriorities) {
                        if (model.triangleRenderPriorities != null) {
                            triangleRenderPriorities!![triangleCount] = model.triangleRenderPriorities!![index]
                        } else {
                            triangleRenderPriorities!![triangleCount] = model.renderPriority
                        }
                    }
                    if (hasTriangleAlphas && model.triangleAlphas != null) {
                        triangleAlphas!![triangleCount] = model.triangleAlphas!![index]
                    }
                    if (hasTriangleSkins && model.triangleSkins != null) {
                        triangleSkins!![triangleCount] = model.triangleSkins!![index]
                    }
                    if (hasTriangleTextures) {
                        if (model.triangleTextures != null) {
                            triangleTextures!![triangleCount] = model.triangleTextures!![index]
                        } else {
                            triangleTextures!![triangleCount] = -1
                        }
                    }
                    if (hasTextureCoordinates) {
                        if (model.textureCoordinates != null && model.textureCoordinates!![index] != -1) {
                            textureCoordinates!![triangleCount] = (textureTriangleCount + model.textureCoordinates!![index]).toByte().toInt()
                        } else {
                            textureCoordinates!![triangleCount] = -1
                        }
                    }
                    triangleColors!![triangleCount] = model.triangleColors!![index]
                    triangleVertex1!![triangleCount] = this.findVertex(model, model.triangleVertex1!![index], partMask)
                    triangleVertex2!![triangleCount] = this.findVertex(model, model.triangleVertex2!![index], partMask)
                    triangleVertex3!![triangleCount] = this.findVertex(model, model.triangleVertex3!![index], partMask)
                    ++triangleCount
                }
                if (emitters != null) {
                    for (index in emitters.indices) {
                        val emitter = model.emitters!![index]
                        val vertex1 = findVertex(model, emitter.s, partMask)
                        val vertex2 = findVertex(model, emitter.t, partMask)
                        val vertex3 = findVertex(model, emitter.u, partMask)
                        emitters[emittersCount] = EmissiveTriangle(emitter.emitter, emitter.face, vertex1, vertex2, vertex3, emitter.priority)
                        emittersCount++
                    }
                }
                if (effectors != null) {
                    for (index in effectors.indices) {
                        val effector = model.effectors!![index]
                        val vertex = findVertex(model, effector.vertex, partMask)
                        effectors[effectorsCount] = EffectiveVertex(effector.effector, vertex)
                        effectorsCount++
                    }
                }
            }
            var count = 0
            for ((modelIndex, model) in models.withIndex()) {
                val partMask = (1 shl modelIndex).toShort()
                for (index in 0 until model.triangleCount) {
                    if (hasTextureCoordinates) {
                        textureCoordinates!![count++] =
                            (
                                if (model.textureCoordinates == null || model.textureCoordinates!![index] == -1) {
                                    -1
                                } else {
                                    model.textureCoordinates!![index] + textureTriangleCount
                                }
                            ).toByte().toInt()
                    }
                }
                for (index in 0 until model.textureTriangleCount) {
                    textureRenderTypes!![textureTriangleCount] = model.textureRenderTypes!![index]
                    val type = textureRenderTypes!![textureTriangleCount].toByte()
                    if (type.toInt() == 0) {
                        textureTriangleVertex1!![textureTriangleCount] = findVertex(model, model.textureTriangleVertex1!![index], partMask)
                        textureTriangleVertex2!![textureTriangleCount] = findVertex(model, model.textureTriangleVertex2!![index], partMask)
                        textureTriangleVertex3!![textureTriangleCount] = findVertex(model, model.textureTriangleVertex3!![index], partMask)
                    }
                    if (type in 1..3) {
                        textureTriangleVertex1!![textureTriangleCount] = model.textureTriangleVertex1!![index]
                        textureTriangleVertex2!![textureTriangleCount] = model.textureTriangleVertex2!![index]
                        textureTriangleVertex3!![textureTriangleCount] = model.textureTriangleVertex3!![index]
                        textureScaleZ!![textureTriangleCount] = model.textureScaleZ!![index]
                        textureSpeed!![textureTriangleCount] = model.textureSpeed!![index]
                        textureScaleX!![textureTriangleCount] = model.textureScaleX!![index]
                        textureRotation!![textureTriangleCount] = model.textureRotation!![index]
                        textureScaleY!![textureTriangleCount] = model.textureScaleY!![index]
                        textureDirection!![textureTriangleCount] = model.textureDirection!![index]
                    }
                    if (type.toInt() == 2) {
                        textureTransU!![textureTriangleCount] = model.textureTransU!![index]
                        textureTransV!![textureTriangleCount] = model.textureTransV!![index]
                    }
                    textureTriangleCount++
                }
            }
            sortTextures()
            if (emitters != null) {
                this.emitters = emitters.requireNoNulls()
            }
            if (effectors != null) {
                this.effectors = effectors.requireNoNulls()
            }
            if (billboards != null) {
                this.faceBillboards = billboards.requireNoNulls()
            }
        }

        private fun Model.sortTextures() {
            if (textureTriangleCount <= 0) return
            var simpleTextureFaceCount = 0
            var complexTextureFaceCount = 0
            var cubeTextureFaceCount = 0
            for (index in 0 until textureTriangleCount) {
                val type = textureRenderTypes!![index]
                if (type == SIMPLE_TEXTURE) {
                    simpleTextureFaceCount++
                }
                if (type == CUBE_TEXTURE) {
                    cubeTextureFaceCount++
                }
                if (type in COMPLEX_TEXTURE_RANGE) {
                    complexTextureFaceCount++
                }
            }

            val textureTriangleVertex1 = IntArray(textureTriangleCount)
            val textureTriangleVertex2 = IntArray(textureTriangleCount)
            val textureTriangleVertex3 = IntArray(textureTriangleCount)
            val textureRenderTypes = IntArray(textureTriangleCount)
            var count = 0
            if (complexTextureFaceCount > 0) {
                val textureScaleX = IntArray(textureTriangleCount)
                val textureScaleY = IntArray(textureTriangleCount)
                val textureScaleZ = IntArray(textureTriangleCount)
                val textureRotation = IntArray(textureTriangleCount)
                val textureDirection = IntArray(textureTriangleCount)
                val textureSpeed = IntArray(textureTriangleCount)
                if (cubeTextureFaceCount > 0) {
                    val textureTransU = IntArray(textureTriangleCount)
                    val textureTransV = IntArray(textureTriangleCount)
                    for (index in 0 until textureTriangleCount) {
                        val type = this.textureRenderTypes!![index]
                        if (type == CUBE_TEXTURE) {
                            textureTriangleVertex1[count] = this.textureTriangleVertex1!![index]
                            textureTriangleVertex2[count] = this.textureTriangleVertex2!![index]
                            textureTriangleVertex3[count] = this.textureTriangleVertex3!![index]
                            textureScaleX[count] = this.textureScaleX!![index]
                            textureScaleY[count] = this.textureScaleY!![index]
                            textureScaleZ[count] = this.textureScaleZ!![index]
                            textureRotation[count] = this.textureRotation!![index]
                            textureDirection[count] = this.textureDirection!![index]
                            textureSpeed[count] = this.textureSpeed!![index]
                            textureTransU[count] = this.textureTransU!![index]
                            textureTransV[count] = this.textureTransV!![index]
                            textureRenderTypes[count] = this.textureRenderTypes!![index]
                            count++
                        }
                    }
                    this.textureTransU = textureTransU
                    this.textureTransV = textureTransV
                }
                for (index in 0 until textureTriangleCount) {
                    val type = this.textureRenderTypes!![index]
                    if (type != CUBE_TEXTURE && type in COMPLEX_TEXTURE_RANGE) {
                        textureTriangleVertex1[count] = this.textureTriangleVertex1!![index]
                        textureTriangleVertex2[count] = this.textureTriangleVertex2!![index]
                        textureTriangleVertex3[count] = this.textureTriangleVertex3!![index]
                        textureScaleX[count] = this.textureScaleX!![index]
                        textureScaleY[count] = this.textureScaleY!![index]
                        textureScaleZ[count] = this.textureScaleZ!![index]
                        textureRotation[count] = this.textureRotation!![index]
                        textureDirection[count] = this.textureDirection!![index]
                        textureSpeed[count] = this.textureSpeed!![index]
                        textureRenderTypes[count] = this.textureRenderTypes!![index]
                        count++
                    }
                }
                this.textureScaleX = textureScaleX
                this.textureScaleY = textureScaleY
                this.textureScaleZ = textureScaleZ
                this.textureRotation = textureRotation
                this.textureDirection = textureDirection
                this.textureSpeed = textureSpeed
            }
            for (index in 0 until textureTriangleCount) {
                val type = this.textureRenderTypes!![index]
                if (type == SIMPLE_TEXTURE) {
                    textureTriangleVertex1[count] = this.textureTriangleVertex1!![index]
                    textureTriangleVertex2[count] = this.textureTriangleVertex2!![index]
                    textureTriangleVertex3[count] = this.textureTriangleVertex3!![index]
                    textureRenderTypes[count] = this.textureRenderTypes!![index]
                    count++
                }
            }
            this.textureTriangleVertex1 = textureTriangleVertex1
            this.textureTriangleVertex2 = textureTriangleVertex2
            this.textureTriangleVertex3 = textureTriangleVertex3
            this.textureRenderTypes = textureRenderTypes
        }

        private fun Model.findVertex(
            var1: Model,
            var2: Int,
            partMask: Short,
        ): Int {
            var var3 = -1
            val var4 = var1.vertexPositionsX!![var2]
            val var5 = var1.vertexPositionsY!![var2]
            val var6 = var1.vertexPositionsZ!![var2]

            for (var7 in 0 until vertexCount) {
                if (var4 == vertexPositionsX!![var7] && var5 == vertexPositionsY!![var7] && var6 == vertexPositionsZ!![var7]) {
                    var3 = var7
                    vertexPartMasks!![var7] = (vertexPartMasks!![var7].toInt() or partMask.toInt()).toShort()
                    break
                }
            }

            if (var3 == -1) {
                vertexPositionsX!![vertexCount] = var4
                vertexPositionsY!![vertexCount] = var5
                vertexPositionsZ!![vertexCount] = var6
                if (var1.vertexSkins != null) {
                    vertexSkins!![vertexCount] = var1.vertexSkins!![var2]
                } else if (this.vertexSkins != null) {
                    this.vertexSkins!![vertexCount] = -1
                }
                vertexPartMasks!![vertexCount] = partMask
                if (var1.skeletalBones != null) {
                    skeletalBones!![vertexCount] = var1.skeletalBones!![var2]
                    skeletalScales!![vertexCount] = var1.skeletalScales!![var2]
                }
                var3 = vertexCount++
            }

            return var3
        }
    }
}

data class SimpleTexture(
    override val faces: List<TexturedFace>,
    override val renderType: Int,
    override val coordinate: Int,
    override val vertex1: Int,
    override val vertex2: Int,
    override val vertex3: Int,
) : Texture

data class CylindricalTexture(
    override val faces: List<TexturedFace>,
    override val renderType: Int,
    override val coordinate: Int,
    override val vertex1: Int,
    override val vertex2: Int,
    override val vertex3: Int,
    override val scaleX: Int,
    override val scaleY: Int,
    override val scaleZ: Int,
    override val rotation: Int,
    override val direction: Int,
    override val speed: Int,
) : ComplexTexture

data class CubeTexture(
    override val faces: List<TexturedFace>,
    override val renderType: Int,
    override val coordinate: Int,
    override val vertex1: Int,
    override val vertex2: Int,
    override val vertex3: Int,
    override val scaleX: Int,
    override val scaleY: Int,
    override val scaleZ: Int,
    override val rotation: Int,
    override val direction: Int,
    override val speed: Int,
    val transU: Int,
    val transV: Int,
) : ComplexTexture

data class SphericalTexture(
    override val faces: List<TexturedFace>,
    override val renderType: Int,
    override val coordinate: Int,
    override val vertex1: Int,
    override val vertex2: Int,
    override val vertex3: Int,
    override val scaleX: Int,
    override val scaleY: Int,
    override val scaleZ: Int,
    override val rotation: Int,
    override val direction: Int,
    override val speed: Int,
) : ComplexTexture

sealed interface ComplexTexture : Texture {
    val scaleX: Int
    val scaleY: Int
    val scaleZ: Int
    val rotation: Int
    val direction: Int
    val speed: Int
}

sealed interface Texture {
    val faces: List<TexturedFace>
    val renderType: Int
    val coordinate: Int
    val vertex1: Int
    val vertex2: Int
    val vertex3: Int
}

data class TexturedFace(
    val index: Int,
    val texture: Int,
)

typealias TextureRetainPredicate = (texture: Texture) -> Boolean

enum class MeshType {
    Unversioned,
    Versioned,
    UnversionedSkeletal,
    VersionedSkeletal,
}

enum class MeshDecodingOption {
    /**
     * If this option is enabled, the original data is preserved for unversioned decode functions.
     * This means the code will not run the code that is found in the client, to strip away "unused"
     * textures and other meta-data. Those operations cause a loss of data, making it impossible to
     * generate a byte-for-byte identical output.
     *
     * An example of this would be setting material colour to 127 because it contains a texture.
     * We cannot get the old colour that was encoded on the mesh back as it could've been anything.
     */
    PreserveOriginalData,

    /**
     * Scales meshes with a version of above 12 down by a factor of 4 (shr 2).
     * Newer meshes in higher revision clients were up-scaled for easier modelling,
     * the client in those revisions performs a down-scaling operation.
     */
    ScaleVersionedMesh,
}

data class EmissiveTriangle(
    val emitter: Int,
    val face: Int,
    val s: Int,
    val t: Int,
    val u: Int,
    val priority: Int,
)

data class EffectiveVertex(
    val effector: Int,
    val vertex: Int,
)

data class FaceBillboard(
    val id: Int,
    val face: Int,
    val skin: Int,
    val distance: Int,
)

data class VertexPoint(
    val x: Int,
    val y: Int,
    val z: Int,
)
