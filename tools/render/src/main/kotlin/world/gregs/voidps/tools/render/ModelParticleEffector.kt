package world.gregs.voidps.tools.render

/* Class342 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

/**
 * Only the fields [Mesh]'s merge constructor needs to remap vertices are kept; the model
 * never draws particles.
 */
class ModelParticleEffector(val id: Int, val vertex: Int) {
    fun copy(vertex: Int) = ModelParticleEffector(id, vertex)
}
