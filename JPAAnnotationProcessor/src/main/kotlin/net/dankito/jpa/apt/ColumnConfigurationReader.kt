package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.ColumnConfig
import net.dankito.jpa.apt.config.DataType
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.Property
import net.dankito.jpa.apt.reflection.ReflectionHelper
import java.sql.SQLException
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.persistence.*
import javax.tools.Diagnostic


class ColumnConfigurationReader(private var relationColumnConfigurationReader: RelationColumnConfigurationReader = RelationColumnConfigurationReader(),
                                private var reflectionHelper: ReflectionHelper = ReflectionHelper()) {

    fun readEntityColumns(context: AnnotationProcessingContext) {
        for(entityConfig in context.getEntityConfigsInOrderAdded()) {
            readEntityColumns(entityConfig, context)
        }
    }

    private fun readEntityColumns(entityConfig: EntityConfig, context: AnnotationProcessingContext) {
        val fields = reflectionHelper.getNonStaticNonTransientFields(entityConfig.entityClass)
        val methodsMap = reflectionHelper.getNonStaticNonAbstractNonTransientMethodsMap(entityConfig.entityClass)

        val properties = reflectionHelper.findProperties(fields, methodsMap)
        properties.forEach { context.registerProperty(it) }

        readEntityColumns(properties, entityConfig, context)
    }

    private fun readEntityColumns(properties: List<Property>, entityConfig: EntityConfig, context: AnnotationProcessingContext) {
        for(property in properties) {
            val cachedPropertyConfig = context.getColumnConfiguration(property)

            if (cachedPropertyConfig != null) {
                entityConfig.addColumn(cachedPropertyConfig)
            }
            else {
                val column = readColumnConfiguration(entityConfig, property, context)
                entityConfig.addColumn(column)
            }
        }
    }

    private fun readColumnConfiguration(entityConfig: EntityConfig, property: Property, context: AnnotationProcessingContext) : ColumnConfig {
        val column = ColumnConfig(entityConfig, property)
        context.registerColumn(column)

        context.getAnnotationsForProperty(entityConfig.entityClass, property.field.name)?.let { variableElement ->
            readColumnConfiguration(column, variableElement, context)
        }

        property.getter?.let { getter ->
            context.getAnnotationsForMethod(entityConfig.entityClass, getter.name)?.let { executableElement ->
                readColumnConfiguration(column, executableElement, context)
            }
        }

        return column
    }

    private fun readColumnConfiguration(column: ColumnConfig, element: Element, context: AnnotationProcessingContext) {
        readDataType(column, element, context)

        readIdConfiguration(column, element)
        readVersionConfiguration(column, element)

        readBasicAnnotation(column, element)
        readColumnAnnotation(column, element)

        readRelationConfiguration(column, element, context)
    }


    @Throws(SQLException::class)
    private fun readDataType(column: ColumnConfig, element: Element, context: AnnotationProcessingContext) {
        if(column.type == Date::class.java || column.type == Calendar::class.java) { // TODO: what about java.sql.Date (or Timestamp)?
            readDateOrCalenderDataType(column, element, context)
        }
        else if (column.type.isEnum) {
            readEnumDataType(column, element)
        }
        else if(element.getAnnotation(Lob::class.java) != null) {
            readLobAnnotation(column, element)
        }
        else if (isCollectionClass(column.type)) {
            // TODO
//            val collectionGenericClass = property.getGenericType()
//            if (configRegistry.isAnEntityWhichConfigurationShouldBeRead(collectionGenericClass) == false) {
//                throwEntityIsNotConfiguredToBeReadException(collectionGenericClass, property)
//            }
        }
        else {
            // TODO
//            for (dataType in DataType.values()) {
//                if(property.getType() == dataType.getType()) {
//                    column.dataType = dataType
//                    return
//                }
//            }

//            if (isAnnotationPresent(property, OneToOne::class.java) == false && isAnnotationPresent(property, ManyToOne::class.java) == false &&
//                    isAnnotationPresent(property, OneToMany::class.java) == false && isAnnotationPresent(property, ManyToMany::class.java) == false) {
//                throw SQLException("Don't know how to serialize Type of Property " + property + ". If it's a relationship, did you forget to set appropriate Annotation (@OneToOne, " +
//                        "@OneToMany, ...) on its field or get method?")
//            }
        }
    }

    @Throws(SQLException::class)
    private fun readDateOrCalenderDataType(column: ColumnConfig, element: Element, context: AnnotationProcessingContext) {
        val temporal = element.getAnnotation(Temporal::class.java)

        if (temporal == null) {
            context.processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "@Temporal not set on field $column.\n"
                    + "According to JPA specification for data types java.util.Date and java.util.Calender @Temporal annotation " +
                    "has to be set. Ignoring this java.sql.Timestamp is assumed for " + column.columnName)
            column.dataType = DataType.DATE_TIMESTAMP
        }
        else {
            when (temporal.value) {
                TemporalType.DATE ->
                    column.dataType = DataType.DATE
                TemporalType.TIME ->
                    column.dataType = DataType.DATE_TIMESTAMP
                else ->
                    column.dataType = DataType.DATE_TIMESTAMP
            }
        }
    }

    @Throws(SQLException::class)
    private fun readEnumDataType(column: ColumnConfig, element: Element) {
        element.getAnnotation(Enumerated::class.java)?.let { enumerated ->
            if (enumerated.value == EnumType.STRING) {
                column.dataType = DataType.ENUM_STRING
                return
            }
        }

        column.dataType = DataType.ENUM_INTEGER
    }

    private fun readLobAnnotation(column: ColumnConfig, element: Element) {
        // TODO: configure Lob field; set settings according to p. 39/40
        element.getAnnotation(Lob::class.java)?.let { lob ->
            column.isLob = true

            // A Lob may be either a binary or character type.
            // The Lob type is inferred from the type of the persistent field or property, and except for string and character-based types defaults to Blob.

            if (CharSequence::class.java.isAssignableFrom(column.type) || CharArray::class.java.isAssignableFrom(column.type) ||
                    Array<Char>::class.java.isAssignableFrom(column.type)) {
                column.dataType = DataType.STRING
                column.columnDefinition = "longvarchar"
            }
            else {
                column.dataType = DataType.BYTE_ARRAY
                column.columnDefinition = "longvarbinary"
            }
        }
    }

    private fun isCollectionClass(clazz: Class<*>): Boolean {
        return Collection::class.java.isAssignableFrom(clazz)
    }


    private fun readIdConfiguration(column: ColumnConfig, element: Element) {
        element.getAnnotation(Id::class.java)?.let { id ->
            val entityConfig = column.entityConfig

            column.isId = true
            entityConfig.setIdColumnAndSetItOnChildEntities(column)

            setAccess(entityConfig, element)

            readGeneratedValueConfiguration(column, element)

            readSequenceGeneratorConfiguration(column, element)
            readTableGeneratorConfiguration(column, element)
        }
    }

    private fun setAccess(entityConfig: EntityConfig, element: Element) {
        if (entityConfig.access == null) { // if access != null than it has been set by @AccessAnnotation
            // otherwise access is determined where @Id Annotation is placed, on field or get method
            if (element.kind == ElementKind.METHOD) {
                entityConfig.access = AccessType.PROPERTY
            } else {
                entityConfig.access = AccessType.FIELD
            }
        }
    }

    private fun readGeneratedValueConfiguration(column: ColumnConfig, element: Element) {
        element.getAnnotation(GeneratedValue::class.java)?.let { generatedValue ->
            column.isGeneratedId = true
            column.generatedIdType = generatedValue.strategy
            column.idGenerator = generatedValue.generator
        }
    }

    private fun readSequenceGeneratorConfiguration(column: ColumnConfig, element: Element) {
        element.getAnnotation(SequenceGenerator::class.java)?.let { sequenceGenerator ->
            // TODO
        }
    }

    private fun  readTableGeneratorConfiguration(column: ColumnConfig, element: Element) {
        element.getAnnotation(TableGenerator::class.java)?.let { tableGenerator ->
            // TODO
        }
    }

    private fun readVersionConfiguration(column: ColumnConfig, element: Element) {
        element.getAnnotation(Version::class.java)?.let { version ->
            if(isValidDataTypeForVersion(column.type) == false) {
                throw SQLException("Data Type for @Version property $column is ${column.type} but must be one of these types: " +
                        "int, Integer, short, Short, long, Long, java.sql.Timestamp.")
            }

            column.isVersion = true
            column.entityConfig.setVersionColumnAndSetItOnChildEntities(column)
        }
    }

    private fun isValidDataTypeForVersion(type: Class<*>): Boolean { // according to http://www.objectdb.com/api/java/jpa/Version
        return Long::class.javaPrimitiveType == type || Long::class.java == type || Long::class.javaObjectType == type
                || Int::class.javaPrimitiveType == type || Int::class.java == type || Int::class.javaObjectType == type
                || Short::class.javaPrimitiveType == type || Short::class.java == type || Short::class.javaObjectType == type || java.sql.Timestamp::class.java == type
    }

    private fun readBasicAnnotation(column: ColumnConfig, element: Element) {
        val basicAnnotation = element.getAnnotation(Basic::class.java)

        if(basicAnnotation != null) {
            column.fetch = basicAnnotation.fetch
            column.canBeNull = basicAnnotation.optional
        }
        else {
            val getter = column.property.getter
            if(column.property.field.annotations.size == 0 &&
               (getter == null || getter.annotations.size == 0)) {
                // no Annotations neither on Field nor on Get-Method - then per default property gets treated as if
                // @Basic(fetch = FetchType.EAGER, optional = true) would be set
                column.fetch = FetchType.EAGER
                column.canBeNull = true
            }
        }
    }

    private fun readColumnAnnotation(column: ColumnConfig, element: Element) {
        element.getAnnotation(Column::class.java)?.let { columnAnnotation ->
            val annotationColumnNameValue = columnAnnotation.name
            if(annotationColumnNameValue.isNotEmpty()) { // when name is not set on Annotation
                column.columnName = annotationColumnNameValue
            }

            column.tableName = columnAnnotation.table
            column.insertable = columnAnnotation.insertable
            column.updatable = columnAnnotation.updatable
            column.canBeNull = columnAnnotation.nullable
            column.unique = columnAnnotation.unique
            column.columnDefinition = columnAnnotation.columnDefinition
            column.length = columnAnnotation.length
            column.scale = columnAnnotation.scale
            column.precision = columnAnnotation.precision
        }
    }

    private fun readRelationConfiguration(column: ColumnConfig, element: Element, context: AnnotationProcessingContext) {
        relationColumnConfigurationReader.readRelationConfiguration(column, element, context)
    }

}