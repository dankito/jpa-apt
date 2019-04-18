package net.dankito.jpa.apt.reflectionfree

import net.dankito.jpa.apt.config.RelationType
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.lang.model.type.MirroredTypeException
import javax.persistence.*


class RelationColumnConfigurationReader {

    companion object {
        private val log = LoggerFactory.getLogger(RelationColumnConfigurationReader::class.java)
    }


    fun readRelationConfiguration(reader: IAnnotationReader, element: ElementBase, column: ColumnConfig) {
        element.getAnnotation(OneToOne::class.java)?.let { oneToOne ->
            readOneToOneConfiguration(reader, element, column, oneToOne)
        }

        element.getAnnotation(ManyToOne::class.java)?.let { manyToOne ->
            readManyToOneConfiguration(reader, element, column, manyToOne)
        }

        element.getAnnotation(OneToMany::class.java)?.let { oneToMany ->
            readOneToManyConfiguration(reader, column, oneToMany)
        }

        element.getAnnotation(ManyToMany::class.java)?.let { manyToMany ->
            readManyToManyConfiguration(reader, column, manyToMany)
        }
    }


    private fun readOneToOneConfiguration(reader: IAnnotationReader, element: ElementBase, column: ColumnConfig, oneToOne: OneToOne) {

        column.relationType = RelationType.OneToOne

        readJoinColumnConfiguration(element, column)

        val targetEntityClassName = getClassNameFromAnnotationValue(oneToOne)
        column.targetEntity = getTargetEntityConfig(reader, column, targetEntityClassName)

        column.cascade = oneToOne.cascade.filterNotNull().toTypedArray()

        column.fetch = oneToOne.fetch
        if (column.fetch == FetchType.LAZY) {
            reader.logWarn("FetchType.LAZY as for $column is not supported for @OneToOne relationships as this would " +
                    "require Proxy Generation or Byte code manipulation like with JavaAssist,  which is not supported on Android.\n" +
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


    private fun readManyToOneConfiguration(reader: IAnnotationReader, element: ElementBase, column: ColumnConfig, manyToOne: ManyToOne) {

        column.relationType = RelationType.ManyToOne

        readJoinColumnConfiguration(element, column)

        val targetEntityAnnotationValue = getClassNameFromAnnotationValue(manyToOne)
        column.targetEntity = getTargetEntityConfig(reader, column, targetEntityAnnotationValue)

        column.cascade = manyToOne.cascade.filterNotNull().toTypedArray()

        column.fetch = manyToOne.fetch
        if (column.fetch == FetchType.LAZY) {
            reader.logWarn("FetchType.LAZY as for $column is not supported for @ManyToOne relationships as this would " +
                    "require Proxy Generation or Byte code manipulation like with JavaAssist, which is not supported on Android.\n" +
                    "As LAZY is per JPA specification only a hint, it will be in this case silently ignored and Fetch set to  EAGER.")
        }

        // TODO: what's the difference between JoinColumn.nullable() and ManyToOne.optional() ?
        if(manyToOne.optional == false) { // don't overwrite a may previously set value by JoinColumn; true is the default value for optional
            column.canBeNull = manyToOne.optional
        }
    }


    private fun readOneToManyConfiguration(reader: IAnnotationReader, column: ColumnConfig, oneToMany: OneToMany) {
        column.relationType = RelationType.OneToMany

        val targetEntityAnnotationValue = getClassNameFromAnnotationValue(oneToMany)
        column.targetEntity = getTargetEntityConfig(reader, column, targetEntityAnnotationValue)

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


    private fun readManyToManyConfiguration(reader: IAnnotationReader, column: ColumnConfig, manyToMany: ManyToMany) {
        column.relationType = RelationType.ManyToMany

        val targetEntityAnnotationValue = getClassNameFromAnnotationValue(manyToMany)
        column.targetEntity = getTargetEntityConfig(reader, column, targetEntityAnnotationValue)

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


    /**
     * TODO: don't rely on TypeMirror, abstract that away via IAnnotationReader. E. g. a reflection based
     * implementation should not be aware of TypeMirrors
     */
    private fun getClassNameFromAnnotationValue(oneToOne: OneToOne): String? {
        try { // this should throw an exception, for explanation see getClassNameFromMirroredTypeException()
            return oneToOne.targetEntity.qualifiedName
        } catch (mte: MirroredTypeException) {
            return getClassNameFromMirroredTypeException(mte)
        }
    }

    private fun getClassNameFromAnnotationValue(manyToOne: ManyToOne): String? {
        try { // this should throw an exception, for explanation see getClassNameFromMirroredTypeException()
            return manyToOne.targetEntity.qualifiedName
        } catch (mte: MirroredTypeException) {
            return getClassNameFromMirroredTypeException(mte)
        }
    }

    private fun getClassNameFromAnnotationValue(oneToMany: OneToMany): String? {
        try { // this should throw an exception, for explanation see getClassNameFromMirroredTypeException()
            return oneToMany.targetEntity.qualifiedName
        } catch (mte: MirroredTypeException) {
            return getClassNameFromMirroredTypeException(mte)
        }
    }

    private fun getClassNameFromAnnotationValue(manyToMany: ManyToMany): String? {
        try { // this should throw an exception, for explanation see getClassNameFromMirroredTypeException()
            return manyToMany.targetEntity.qualifiedName
        } catch (mte: MirroredTypeException) {
            return getClassNameFromMirroredTypeException(mte)
        }
    }

    private fun getClassNameFromMirroredTypeException(exception: MirroredTypeException): String? {
        // accessing targetEntity value directly throws javax.lang.model.type.MirroredTypeException: Attempt to access Class object for TypeMirror ...
        // see https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation or http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/

        try {
            val className = exception.typeMirror.toString()
            if (className == "void") { // targetEntity value not set
                return null
            }

            return className
        } catch (getClassFromTypeMirrorException: Exception) {
            log.error("Could not get Class from TypeMirror ${exception.typeMirror}")
        }

        return null // can this ever happen ??
    }

    @Throws(SQLException::class)
    private fun readJoinColumnConfiguration(element: ElementBase, column: ColumnConfig) {
        var joinColumnAnnotationOrNameNotSet = true

        element.getAnnotation(JoinColumn::class.java)?.let { joinColumn ->
            column.isJoinColumn = true

            if(joinColumn.name.isNotBlank()) {
                val annotationColumnNameValue = joinColumn.name
                if(annotationColumnNameValue.isNotEmpty()) { // when name is not set on Annotation
                    joinColumnAnnotationOrNameNotSet = false
                    column.columnName = joinColumn.name
                }
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
    private fun getTargetEntityConfig(reader: IAnnotationReader, column: ColumnConfig, targetEntityClassName: String?): EntityConfig {
        val targetEntityClass = getTargetEntityClass(reader, column, targetEntityClassName)

        val targetEntity = reader.getEntityConfigForType(targetEntityClass)

        if (targetEntity == null) {
            throw SQLException("Target Class $targetEntityClass for Property $column is not configured as Entity or has not been passed as parameter to readConfiguration()\r\n" +
                    "Please configure it as @Entity and add it as parameter to readConfiguration() method of JpaEntityConfigurationReader.")
        }

        return targetEntity
    }

    /**
     * If the field targetEntity on a OneToOne, OneToMany, ... annotation is set (that's the [targetEntityClassName]
     * parameter), use that one.
     *
     * Else if column's type is any of the collection types, use the collection's generic parameter type as type
     * (valid for xyzToMany relations).
     *
     * Otherwise use column's type (valid for xyzToOne relations).
     */
    @Throws(SQLException::class)
    private fun getTargetEntityClass(reader: IAnnotationReader, column: ColumnConfig, targetEntityClassName: String?): Type {

        val targetType = targetEntityClassName?.let { reader.typeFromQualifiedName(it) } // TODO: don't use typeFromQualifiedName(), it's intended for parameters only

        // targetEntity on a OneToOne, OneToMany, ManyToOne or ManyToMany annotation is set
        if (targetType != null && targetType.isVoidType == false) { // Void is the default value for targetEntity
            // TODO
    //            if(isEntityOrMappedSuperclass(targetEntityClassName) == false) {
    //                throw SQLException("Target Class " + targetEntityClassName + " on Property " + column + " is not configured as @Entity or @MappedSuperclass.\r\n" +
    //                        "Please add @Entity or @MappedSuperclass to $targetEntityClassName.")
    //            }

            return targetType
        }
        // otherwise for a OneToMany or ManyToMany relation: column is a (descendant of) Collection -> it's generic
        // type parameter has to be set
        else if (column.type.canBeAssignedTo(Collection::class.java)) {
            if (column.type.genericArguments.isEmpty()) {
                throw SQLException("For relation property " + column + " either Annotation's targetEntity value has to be set or it's type has to be a " +
                        "Collection with generic type set to target entity's type.")
            } else {
                return column.type.genericArguments.first()
            }
        }
        // otherwise for OneToOne and ManyToOne relation: simple return column's type
        else {
            return column.type
        }
    }

}