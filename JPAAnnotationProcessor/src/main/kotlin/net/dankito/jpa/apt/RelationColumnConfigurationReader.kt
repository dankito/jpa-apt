package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.ColumnConfig
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.RelationType
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.persistence.*
import javax.tools.Diagnostic


class RelationColumnConfigurationReader {

    companion object {
        private val log = LoggerFactory.getLogger(RelationColumnConfigurationReader::class.java)
    }


    fun readRelationConfiguration(column: ColumnConfig, element: Element, context: AnnotationProcessingContext) {
        element.getAnnotation(OneToOne::class.java)?.let { oneToOne ->
            readOneToOneConfiguration(column, element, oneToOne, context)
        }

        element.getAnnotation(ManyToOne::class.java)?.let { manyToOne ->
            readManyToOneConfiguration(column, element, manyToOne, context)
        }

        element.getAnnotation(OneToMany::class.java)?.let { oneToMany ->
            readOneToManyConfiguration(column, element, oneToMany, context)
        }

        element.getAnnotation(ManyToMany::class.java)?.let { manyToMany ->
            readManyToManyConfiguration(column, element, manyToMany, context)
        }
    }


    private fun readOneToOneConfiguration(column: ColumnConfig, element: Element, oneToOne: OneToOne, context: AnnotationProcessingContext) {
        column.relationType = RelationType.OneToOne

        readJoinColumnConfiguration(column, element)

        val targetEntityAnnotationValue = getClassFromAnnotationValue(oneToOne)
        column.targetEntity = getTargetEntityConfig(column, targetEntityAnnotationValue, context)

        column.cascade = oneToOne.cascade.filterNotNull().toTypedArray()

        column.fetch = oneToOne.fetch
        if (column.fetch == FetchType.LAZY) {
            context.processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "FetchType.LAZY as for $column is not supported for @OneToOne relationships as this would " +
                    "require Proxy Generation or Byte code manipulation like with JavaAssist,  which is not supported on Android. " +
                    "As LAZY is per JPA specification only a hint, it will be in this case silently ignored and Fetch set to  EAGER.")
        }

        // TODO: what's the difference between JoinColumn.nullable() and OneToOne.optional() ?
        if(oneToOne.optional == false) { // don't overwrite a may previously set value by JoinColumn; true is the default value for optional
            column.canBeNull = oneToOne.optional
        }

        column.orphanRemoval = oneToOne.orphanRemoval

        // TODO: check if this column even has to be generated or if it's completely mapped by foreign @OneToOne property
        // TODO:
//        configureOneToOneTargetProperty(property, propertyConfig, elements, targetEntityClass, fetch, cascade, joinColumnName)
    }


    private fun readManyToOneConfiguration(column: ColumnConfig, element: Element, manyToOne: ManyToOne, context: AnnotationProcessingContext) {

    }


    private fun readOneToManyConfiguration(column: ColumnConfig, element: Element, oneToMany: OneToMany, context: AnnotationProcessingContext) {
        column.relationType = RelationType.OneToMany

        val targetEntityAnnotationValue = getClassFromAnnotationValue(oneToMany)
        column.targetEntity = getTargetEntityConfig(column, targetEntityAnnotationValue, context)

        column.cascade = oneToMany.cascade.filterNotNull().toTypedArray()

        column.fetch = oneToMany.fetch

        column.orphanRemoval = oneToMany.orphanRemoval

        // TODO
        if (oneToMany.mappedBy.isNullOrBlank() == false) {
//            try {
//                configureBidirectionalOneToManyField(property, propertyConfig, oneToManyAnnotation, fetch, cascade, targetEntityClass)
//            } catch (ex: Exception) {
//                propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig)
//                log.error("Could not configure bidirectional OneToMany field for property " + property, ex)
//                throw SQLException(ex)
//            }
        }
        else { // ok, this means relation is not bidirectional
//            configureUnidirectionalOneToManyField(property, propertyConfig, targetEntityClass, fetch, cascade)
//            // TODO: unidirectional means we have to create a Join Table, this case is not supported yet
//            //      throw new SQLException("Sorry, but unidirectional @OneToMany associations as for property " + property + " are not supported yet by this implementation. Please add a @ManyToOne field on the many side.");
        }

        // TODO
//        readOrderByAnnotation(property, propertyConfig, targetEntityClass)
    }


    private fun readManyToManyConfiguration(column: ColumnConfig, element: Element, manyToMany: ManyToMany, context: AnnotationProcessingContext) {
        column.relationType = RelationType.ManyToMany

        val targetEntityAnnotationValue = getClassFromAnnotationValue(manyToMany)
        column.targetEntity = getTargetEntityConfig(column, targetEntityAnnotationValue, context)

        column.cascade = manyToMany.cascade.filterNotNull().toTypedArray()

        column.fetch = manyToMany.fetch

//        val mappedBy = elements.get("mappedBy")
//        val targetProperty = findManyToManyTargetProperty(property, mappedBy, targetEntityClass)
//
//        val manyToManyConfig: ManyToManyConfig
//
//        if (targetProperty == null) {
//            propertyConfig.setIsOwningSide(true)
//            propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig)
//            readJoinTableAnnotation(property, propertyConfig, targetEntityClass, null)
//            manyToManyConfig = ManyToManyConfig(property, targetEntityClass, fetch, cascade)
//        } else {
//            propertyConfig.setIsBidirectional(true)
//            propertyConfig.setTargetProperty(targetProperty)
//
//            val owningSideProperty: Property
//            val inverseSideProperty: Property
//
//            if (StringHelper.isNotNullOrEmpty(mappedBy)) {
//                propertyConfig.setIsInverseSide(true)
//                inverseSideProperty = property
//                owningSideProperty = targetProperty
//            } else {
//                propertyConfig.setIsOwningSide(true)
//                propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig)
//                readJoinTableAnnotation(property, propertyConfig, targetEntityClass, targetProperty)
//                owningSideProperty = property
//                inverseSideProperty = targetProperty
//            }
//
//            manyToManyConfig = ManyToManyConfig(owningSideProperty, inverseSideProperty, fetch, cascade)
//        }
//
//        propertyConfig.setManyToManyConfig(manyToManyConfig) // TODO: try to remove
//        configRegistry.registerJoinTableConfiguration(manyToManyConfig.getOwningSideClass(), manyToManyConfig.getInverseSideClass(), manyToManyConfig.getJoinTableConfig())
//
//        readOrderByAnnotation(property, propertyConfig, targetEntityClass)
    }


    private fun getClassFromAnnotationValue(oneToOne: OneToOne): Class<*>? {
        // accessing targetEntity value directly throws javax.lang.model.type.MirroredTypeException: Attempt to access Class object for TypeMirror ...
        // see https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation or http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
        try {
            oneToOne.targetEntity // this should throw
        } catch (mte: MirroredTypeException) {
            try {
                val type = mte.typeMirror.toString()
                if(type == "void") { // targetEntity value not set
                    return null
                }

                return Class.forName(type)
            } catch(getClassFromTypeMirrorException: Exception) {
                log.error("Could not get Class from TypeMirror ${mte.typeMirror}")
            }
        }

        return null // can this ever happen ??
    }

    private fun getClassFromAnnotationValue(manyToOne: ManyToOne): Class<*>? {
        // accessing targetEntity value directly throws javax.lang.model.type.MirroredTypeException: Attempt to access Class object for TypeMirror ...
        // see https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation or http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
        try {
            manyToOne.targetEntity // this should throw
        } catch (mte: MirroredTypeException) {
            try {
                val type = mte.typeMirror.toString()
                if(type == "void") { // targetEntity value not set
                    return null
                }

                return Class.forName(type)
            } catch(getClassFromTypeMirrorException: Exception) {
                log.error("Could not get Class from TypeMirror ${mte.typeMirror}")
            }
        }

        return null // can this ever happen ??
    }

    private fun getClassFromAnnotationValue(oneToMany: OneToMany): Class<*>? {
        // accessing targetEntity value directly throws javax.lang.model.type.MirroredTypeException: Attempt to access Class object for TypeMirror ...
        // see https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation or http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
        try {
            oneToMany.targetEntity // this should throw
        } catch (mte: MirroredTypeException) {
            try {
                val type = mte.typeMirror.toString()
                if(type == "void") { // targetEntity value not set
                    return null
                }

                return Class.forName(type)
            } catch(getClassFromTypeMirrorException: Exception) {
                log.error("Could not get Class from TypeMirror ${mte.typeMirror}")
            }
        }

        return null // can this ever happen ??
    }

    private fun getClassFromAnnotationValue(manyToMany: ManyToMany): Class<*>? {
        // accessing targetEntity value directly throws javax.lang.model.type.MirroredTypeException: Attempt to access Class object for TypeMirror ...
        // see https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation or http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
        try {
            manyToMany.targetEntity // this should throw
        } catch (mte: MirroredTypeException) {
            try {
                val type = mte.typeMirror.toString()
                if(type == "void") { // targetEntity value not set
                    return null
                }

                return Class.forName(type)
            } catch(getClassFromTypeMirrorException: Exception) {
                log.error("Could not get Class from TypeMirror ${mte.typeMirror}")
            }
        }

        return null // can this ever happen ??
    }

    @Throws(SQLException::class)
    private fun readJoinColumnConfiguration(column: ColumnConfig, element: Element) {
        var joinColumnAnnotationOrNameNotSet = true

        element.getAnnotation(JoinColumn::class.java)?.let { joinColumn ->
            column.isJoinColumn = true

            if(joinColumn.name.isNotBlank()) {
                joinColumnAnnotationOrNameNotSet = false
                column.columnName = joinColumn.name
            }

            if(joinColumn.columnDefinition.isNotBlank()) {
                column.columnDefinition = joinColumn.columnDefinition
            }

            column.canBeNull = joinColumn.nullable
            column.unique = joinColumn.unique
            column.insertable = joinColumn.insertable
            column.updatable = joinColumn.updatable

            column.tableName = joinColumn.table
            column.referencedColumnName = joinColumn.referencedColumnName

//            joinColumn.foreignKey // TODO: JPA 2.1
        }

        if(joinColumnAnnotationOrNameNotSet) {
            applyDefaultJoinColumnName(column)
        }
    }

    private fun applyDefaultJoinColumnName(column: ColumnConfig) {
        // from @JoinColumn.name documentation
        /*Default (only applies if a single join column is used):
         * The concatenation of the following: the name of the
         * referencing relationship property or field of the referencing
         * entity or embeddable class; "_"; the name of the referenced
         * primary key column.
         * If there is no such referencing relationship property or
         * field in the entity, or if the join is for an element collection,
         * the join column name is formed as the
         * concatenation of the following: the name of the entity; "_";
         * the name of the referenced primary key column.
         */

        // TODO
    }


    @Throws(SQLException::class)
    private fun getTargetEntityConfig(column: ColumnConfig, annotationTargetEntityValue: Class<*>?, context: AnnotationProcessingContext): EntityConfig {
        var targetEntityClass = getTargetEntityClass(column, annotationTargetEntityValue)

        val targetEntity = context.getEntityConfigForClass(targetEntityClass)
        if(targetEntity == null) {
            throw SQLException("Target Class $targetEntityClass for Property $column is not configured as Entity or has not been passed as parameter to readConfiguration()\r\n" +
                    "Please configure it as @Entity and add it as parameter to readConfiguration() method of JpaEntityConfigurationReader.")
        }

        return targetEntity
    }

    @Throws(SQLException::class)
    private fun getTargetEntityClass(column: ColumnConfig, annotationTargetEntityValue: Class<*>?): Class<*> {
        var targetEntityClass = column.type

        if (annotationTargetEntityValue != null && annotationTargetEntityValue != Void::class) { // Void is the default value for targetEntity
            // TODO
    //            if(isEntityOrMappedSuperclass(annotationTargetEntityValue) == false) {
    //                throw SQLException("Target Class " + annotationTargetEntityValue + " on Property " + column + " is not configured as @Entity or @MappedSuperclass.\r\n" +
    //                        "Please add @Entity or @MappedSuperclass to $annotationTargetEntityValue.")
    //            }

            targetEntityClass = annotationTargetEntityValue
        }
        else if (Collection::class.java.isAssignableFrom(targetEntityClass)) {
            val genericType = column.property.getGenericType()

            if (genericType == null) {
                throw SQLException("For relation property " + column + " either Annotation's targetEntity value has to be set or it's type has to be a " +
                        "Collection with generic type set to target entity's type.")
            } else {
                targetEntityClass = genericType
            }
        }

        return targetEntityClass
    }

}