package org.redrune.tools.func

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfoList

class FileFunc {

    companion object {

       inline fun <reified T : Any> getChildClassesOf(name: String?): MutableList<Class<*>>? {
            val classGraph = ClassGraph().enableClassInfo()
            val result = classGraph.scan()
            println("name=$name")
            return result.getSubclasses(name).loadClasses() as MutableList<Class<*>>?
        }


        inline fun <reified T> getChildClassesOf(): ArrayList<Any> {
            val classGraph = ClassGraph().enableClassInfo()
            val result = classGraph.scan()
            val name = T::class.qualifiedName
            val list = result.getSubclasses(name).loadClasses() as MutableList<Class<*>>?
            val classes = ArrayList<Any>()
            list?.forEach { classes.add(it.newInstance()) }
            return classes
        }

        fun getClasses(vararg results: ClassInfoList.ClassInfoFilter): MutableList<Class<*>> {
            val classes = mutableListOf<Class<*>>()
            val classGraph = ClassGraph().disableNestedJarScanning().enableClassInfo().ignoreClassVisibility()
            val result = classGraph.scan()
            println("preFilter=${result.allClasses.toMutableList()}")
            results.forEach {
                result.allClasses.filter(it)
            }
            println("postFilter=${result.allClasses.toMutableList()}")
            classes.addAll(result.allClasses.loadClasses())
            return classes
        }
    }
}