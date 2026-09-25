package world.gregs.voidps.tools.render

/* Class129 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

/**
 * Only the fields [Mesh]'s merge constructor needs to remap vertices are kept; the model
 * never draws particles.
 */
class ModelParticleEmitter(val id: Int, val vertexA: Int, val vertexB: Int, val vertexC: Int, val priority: Byte) {
    fun copy(vertexA: Int, vertexB: Int, vertexC: Int) = ModelParticleEmitter(id, vertexA, vertexB, vertexC, priority)
}
