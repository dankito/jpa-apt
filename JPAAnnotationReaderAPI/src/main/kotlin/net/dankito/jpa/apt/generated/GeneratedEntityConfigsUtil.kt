package net.dankito.jpa.apt.generated

import net.dankito.jpa.apt.config.EntityConfig


open class GeneratedEntityConfigsUtil {

    companion object {
        const val GeneratedEntityConfigsPackageName = "net.dankito.jpa.apt.generated"

        const val GeneratedEntityConfigsClassName = "GeneratedEntityConfigs"

        const val GetGeneratedEntityConfigsMethodName = "getGeneratedEntityConfigs"
    }


    open fun getLastPreviouslyBuiltGeneratedEntityConfigsAndItsNumber(): Pair<Class<*>, Int>? {
        try {
            var lastGeneratedEntityConfigsClass = Class.forName(GeneratedEntityConfigsUtil.GeneratedEntityConfigsPackageName + "." +
                    GeneratedEntityConfigsUtil.GeneratedEntityConfigsClassName)
            var countGeneratedEntityConfigs = 1

            while(lastGeneratedEntityConfigsClass != null) {
                try {
                    Class.forName(GeneratedEntityConfigsUtil.GeneratedEntityConfigsPackageName + "." +
                            GeneratedEntityConfigsUtil.GeneratedEntityConfigsClassName + (countGeneratedEntityConfigs + 1))?.let {
                        lastGeneratedEntityConfigsClass = it

                        countGeneratedEntityConfigs++
                    } ?: break
                } catch(classDoesNotExist: Exception) { break }
            }

            if(lastGeneratedEntityConfigsClass != null) {
                return Pair(lastGeneratedEntityConfigsClass, countGeneratedEntityConfigs)
            }
        } catch(e: Exception) { } // most often the case that there aren't any other entities from other modules / projects

        return null
    }


    open fun generatedEntityConfigs(): List<EntityConfig>? {
        getLastPreviouslyBuiltGeneratedEntityConfigsAndItsNumber()?.let {
            val generatedEntityConfigsClass = it.first
            val generatedEntityConfigsInstance = generatedEntityConfigsClass.newInstance()

            val getGeneratedEntityConfigsMethod = generatedEntityConfigsClass.getDeclaredMethod(GeneratedEntityConfigsUtil.GetGeneratedEntityConfigsMethodName)
            return getGeneratedEntityConfigsMethod.invoke(generatedEntityConfigsInstance) as List<EntityConfig>
        }

        return null
    }

}