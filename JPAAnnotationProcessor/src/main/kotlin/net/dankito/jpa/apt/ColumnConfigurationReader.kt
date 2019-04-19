package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.*
import java.sql.SQLException
import java.util.*
import javax.persistence.*


open class ColumnConfigurationReader(private var relationColumnConfigurationReader: RelationColumnConfigurationReader = RelationColumnConfigurationReader()) {

    fun readEntityColumns(reader: IAnnotationReader) {
        for(entityConfig in reader.getEntityConfigsInOrderAdded()) {
            readEntityColumns(reader, entityConfig)
        }
    }

    private fun readEntityColumns(reader: IAnnotationReader, entityConfig: EntityConfig) {
        // TODO: set all fields and methods directly on type so that logic for nonStatic() etc. can be implemented here
        val fields = reader.getNonStaticNonTransientFields(entityConfig)
        val methodsMap = reader.getNonStaticNonAbstractNonTransientMethodsMap(entityConfig)

        val properties = findProperties(fields, methodsMap)
        properties.forEach { reader.registerProperty(it) }

        readEntityColumns(reader, entityConfig, properties)
    }

    private fun readEntityColumns(reader: IAnnotationReader, entityConfig: EntityConfig, properties: List<Property>) {
        for(property in properties) {
            val cachedPropertyConfig = reader.getColumnConfiguration(property)

            if (cachedPropertyConfig != null) {
                entityConfig.addColumn(cachedPropertyConfig)
            }
            else {
                val column = readColumnConfiguration(reader, entityConfig, property)
                entityConfig.addColumn(column)
            }
        }
    }

    private fun readColumnConfiguration(reader: IAnnotationReader, entityConfig: EntityConfig, property: Property) : ColumnConfig {
        val column = ColumnConfig(entityConfig, property)
        reader.registerColumn(column)

        readColumnConfiguration(reader, property.field, column)

        property.getter?.let { getter ->
            readColumnConfiguration(reader, getter, column)
        }

        return column
    }

    private fun readColumnConfiguration(reader: IAnnotationReader, element: ElementBase, column: ColumnConfig) {
        readDataType(reader, element, column)

        readIdConfiguration(element, column)
        readVersionConfiguration(element, column)

        readBasicAnnotation(element, column)
        readColumnAnnotation(element, column)

        readRelationConfiguration(reader, element, column)
    }


    @Throws(SQLException::class)
    private fun readDataType(reader: IAnnotationReader, element: ElementBase, column: ColumnConfig) {
        if(column.type == Date::class.java || column.type == Calendar::class.java) { // TODO: what about java.sql.Date (or Timestamp)?
            readDateOrCalenderDataType(column, element, reader)
        }
        else if (column.isEnumType()) {
            readEnumDataType(element, column)
        }
        else if(element.getAnnotation(Lob::class.java) != null) {
            readLobAnnotation(element, column)
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
    private fun readDateOrCalenderDataType(column: ColumnConfig, element: ElementBase, reader: IAnnotationReader) {
        val temporal = element.getAnnotation(Temporal::class.java)

        if (temporal == null) {
            reader.logWarn("@Temporal not set on field $column.\n"
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

    private fun readEnumDataType(element: ElementBase, column: ColumnConfig) {
        element.getAnnotation(Enumerated::class.java)?.let { enumerated ->
            if (enumerated.value == EnumType.STRING) {
                column.dataType = DataType.ENUM_STRING
                return
            }
        }

        column.dataType = DataType.ENUM_INTEGER // default data type for enums (also if Enumerated annotation is not set)
    }

    private fun readLobAnnotation(element: ElementBase, column: ColumnConfig) {
        // TODO: configure Lob field; set settings according to p. 39/40
        element.getAnnotation(Lob::class.java)?.let {
            column.isLob = true

            // A Lob may be either a binary or character type.
            // The Lob type is inferred from the type of the persistent field or property, and except for string and character-based types defaults to Blob.

            val type = column.type

            if (type.canBeAssignedTo(CharSequence::class.java) || type.canBeAssignedTo(CharArray::class.java) ||
                    type.canBeAssignedTo(Array<Char>::class.java)) {
                column.dataType = DataType.STRING
                column.columnDefinition = "longvarchar"
            }
            else {
                column.dataType = DataType.BYTE_ARRAY
                column.columnDefinition = "longvarbinary"
            }
        }
    }

    private fun isCollectionClass(type: Type): Boolean {
        return type.canBeAssignedTo(Collection::class.java)
    }


    private fun readIdConfiguration(element: ElementBase, column: ColumnConfig) {
        element.getAnnotation(Id::class.java)?.let {
            val entityConfig = column.entityConfig

            column.isId = true
            entityConfig.setIdColumnAndSetItOnChildEntities(column)

            setAccess(element, entityConfig)

            readGeneratedValueConfiguration(element, column)

            readSequenceGeneratorConfiguration(element, column)
            readTableGeneratorConfiguration(element, column)
        }
    }

    private fun setAccess(element: ElementBase, entityConfig: EntityConfig) {
        if (entityConfig.access == null) { // if access != null than it has been set by @AccessAnnotation
            // otherwise access is determined where @Id Annotation is placed, on field or get method
            if (element is Method) {
                entityConfig.access = AccessType.PROPERTY
            } else {
                entityConfig.access = AccessType.FIELD
            }
        }
    }

    private fun readGeneratedValueConfiguration(element: ElementBase, column: ColumnConfig) {
        element.getAnnotation(GeneratedValue::class.java)?.let { generatedValue ->
            column.isGeneratedId = true
            column.generatedIdType = generatedValue.strategy
            column.idGenerator = generatedValue.generator
        }
    }

    private fun readSequenceGeneratorConfiguration(element: ElementBase, column: ColumnConfig) {
        element.getAnnotation(SequenceGenerator::class.java)?.let { sequenceGenerator ->
            // TODO
        }
    }

    private fun  readTableGeneratorConfiguration(element: ElementBase, column: ColumnConfig) {
        element.getAnnotation(TableGenerator::class.java)?.let { tableGenerator ->
            // TODO
        }
    }

    private fun readVersionConfiguration(element: ElementBase, column: ColumnConfig) {
        element.getAnnotation(Version::class.java)?.let {
            if(isValidDataTypeForVersion(column.type) == false) {
                throw SQLException("Data Type for @Version property $column is ${column.type} but must be one of these types: " +
                        "int, Integer, short, Short, long, Long, java.sql.Timestamp.")
            }

            column.isVersion = true
            column.entityConfig.setVersionColumnAndSetItOnChildEntities(column)
        }
    }

    private fun isValidDataTypeForVersion(type: Type): Boolean { // according to http://www.objectdb.com/api/java/jpa/Version
        return type.isOfType(Long::class.javaPrimitiveType) || type.isOfType(Long::class.java) || type.isOfType(Long::class.javaObjectType)
                || type.isOfType(Int::class.javaPrimitiveType)|| type.isOfType(Int::class.java) || type.isOfType(Int::class.javaObjectType)
                || type.isOfType(Short::class.javaPrimitiveType) || type.isOfType(Short::class.java) || type.isOfType(Short::class.javaObjectType)
                || type.isOfType(java.sql.Timestamp::class.java)
    }

    private fun readBasicAnnotation(element: ElementBase, column: ColumnConfig) {
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

    private fun readColumnAnnotation(element: ElementBase, column: ColumnConfig) {
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

    protected open fun readRelationConfiguration(reader: IAnnotationReader, element: ElementBase, column: ColumnConfig) {
        relationColumnConfigurationReader.readRelationConfiguration(reader, element, column)
    }


    protected open fun findProperties(fields: List<Field>, methodsMap: MutableMap<String, Method>) : List<Property> {
        val properties = ArrayList<Property>()

        for (field in fields) {
            val getter = findGetMethod(field, methodsMap)
            val setter = findSetMethod(field, methodsMap)

            val property = Property(field, getter, setter)
            properties.add(property)
        }

        return properties
    }

    private fun findGetMethod(field: Field, methodsMap: MutableMap<String, Method>): Method? {
        val fieldName = getFieldNameWithFirstLetterUpperCase(field)

        methodsMap["get" + fieldName]?.let { getMethod ->
            if (isGetMethodForField(getMethod, field)) {
                methodsMap.remove(getMethod.name)

                return getMethod
            }
        }

        if (field.type.isBooleanType) {
            methodsMap["is" + fieldName]?.let { getMethod ->
                if (isGetMethodForField(getMethod, field)) {
                    methodsMap.remove(getMethod.name)

                    return getMethod
                }
            }

            methodsMap["has" + fieldName]?.let { getMethod ->
                if (isGetMethodForField(getMethod, field)) {
                    methodsMap.remove(getMethod.name)

                    return getMethod
                }
            }
        }

        return null
    }

    private fun isGetMethodForField(getMethod: Method?, field: Field): Boolean {
        return getMethod != null && isGetMethod(getMethod) && getMethod.returnType == field.type
    }

    private fun isGetMethod(method: Method): Boolean {
        val methodName = method.name
        return method.hasNoParameters() &&
                ( methodName.startsWith("get") || isBooleanGetMethod(method, methodName) )
    }

    private fun isBooleanGetMethod(method: Method, methodName: String): Boolean {
        return (method.returnType.isBooleanType)
                && (methodName.startsWith("is") || methodName.startsWith("has"))
    }

    private fun findSetMethod(field: Field, methodsMap: MutableMap<String, Method>): Method? {
        val fieldName = getFieldNameWithFirstLetterUpperCase(field)

        methodsMap["set" + fieldName]?.let { setMethod ->

            if(setMethod.returnType.isVoidType && setMethod.hasCountParameters(1)
                    && setMethod.parameters[0] == field.type) {

                methodsMap.remove(setMethod.name)

                return setMethod
            }
        }

        return null
    }

    private fun getFieldNameWithFirstLetterUpperCase(field: Field) = field.name.substring(0, 1).toUpperCase(Locale.ENGLISH) + field.name.substring(1)

}