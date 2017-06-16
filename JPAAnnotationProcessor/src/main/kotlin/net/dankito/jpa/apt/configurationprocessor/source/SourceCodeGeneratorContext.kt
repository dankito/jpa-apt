package net.dankito.jpa.apt.configurationprocessor.source

import com.squareup.javapoet.ClassName
import net.dankito.jpa.apt.config.EntityConfig


class SourceCodeGeneratorContext {

    val classNamesToEntityConfigsMap = HashMap<ClassName, EntityConfig>()


    fun addEntityConfig(className: ClassName, entityConfig: EntityConfig) {
        classNamesToEntityConfigsMap.put(className, entityConfig)
    }

    fun getEntityConfig(className: ClassName): EntityConfig? {
        return classNamesToEntityConfigsMap[className]
    }

    fun getClassName(entityConfig: EntityConfig): ClassName? {
        classNamesToEntityConfigsMap.entries.forEach {
            if(it.value == entityConfig) {
                return it.key
            }
        }

        return null
    }

}