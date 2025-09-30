package world.gregs.voidps.engine.dispatch

/**
 * A meta-annotation used to tell the gradle ScriptMetadataTask
 * which annotation fields can contain wildcards
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Wildcard(val type: String)
