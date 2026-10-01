package world.gregs.voidps.tools.render

/* Class129 */

class ModelParticleEmitter(val id: Int, val vertexA: Int, val vertexB: Int, val vertexC: Int, val priority: Byte) {
    fun copy(vertexA: Int, vertexB: Int, vertexC: Int) = ModelParticleEmitter(id, vertexA, vertexB, vertexC, priority)
}
