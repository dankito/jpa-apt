package net.dankito.jpa.apt.configurationprocessor.source

import com.squareup.javapoet.ClassName
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.JPAEntityConfiguration


class SourceCodeGeneratorContext(entityConfiguration: JPAEntityConfiguration) {

    private val entityConfigsOrderedHierarchically = LinkedHashSet<EntityConfig>()

    private val classNamesToEntityConfigsMap = HashMap<ClassName, EntityConfig>()

    private val targetEntities = HashMap<EntityConfig, Set<EntityConfig>>()


    init {
        entityConfigsOrderedHierarchically.addAll(entityConfiguration.entities.filter { it.parentEntity == null })

        addChildrenRecursively(entityConfigsOrderedHierarchically, entityConfigsOrderedHierarchically.toList() /* make a copy */)
    }

    private fun addChildrenRecursively(sortedList: LinkedHashSet<EntityConfig>, entityConfigsFromLastRound: Collection<EntityConfig>) {
        val addedChildEntities = LinkedHashSet<EntityConfig>()

        entityConfigsFromLastRound.forEach {
            it.childEntities.forEach { childEntity ->
                sortedList.add(childEntity)
                addedChildEntities.add(childEntity)
            }
        }

        if(addedChildEntities.size > 0) {
            addChildrenRecursively(sortedList, addedChildEntities)
        }
    }


    fun getEntityConfigsOrderedHierarchically(): Collection<EntityConfig> {
        return entityConfigsOrderedHierarchically
    }

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


    fun addTargetEntities(entityConfig: EntityConfig, targetEntities: Set<EntityConfig>) {
        this.targetEntities.put(entityConfig, targetEntities)
    }

    fun getTargetEntities(entityConfig: EntityConfig): Set<EntityConfig>? {
        return this.targetEntities[entityConfig]
    }

}