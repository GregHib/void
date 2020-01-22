package org.redrune.tools.func

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassInfoList

class FileFunc {

    companion object {

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