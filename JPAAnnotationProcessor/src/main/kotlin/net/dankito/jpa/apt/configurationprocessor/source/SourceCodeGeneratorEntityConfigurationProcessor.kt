package net.dankito.jpa.apt.configurationprocessor.source

import com.squareup.javapoet.*
import net.dankito.jpa.apt.config.*
import net.dankito.jpa.apt.configurationprocessor.IEntityConfigurationProcessor
import net.dankito.jpa.apt.reflection.ReflectionHelper
import java.lang.Exception
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.persistence.AccessType
import javax.persistence.CascadeType
import javax.persistence.FetchType
import javax.persistence.GenerationType


class SourceCodeGeneratorEntityConfigurationProcessor : IEntityConfigurationProcessor {

    companion object {
        const val GeneratedEntityConfigsPackageName = "net.dankito.jpa.apt.generated"

        const val GeneratedEntityConfigsClassName = "GeneratedEntityConfigs"

        const val GetGeneratedEntityConfigsMethodName = "getGeneratedEntityConfigs"
    }

    override fun processConfiguration(entityConfiguration: JPAEntityConfiguration, processingEnv: ProcessingEnvironment) {
        val context = SourceCodeGeneratorContext(entityConfiguration)

        createEntityConfigClasses(context, processingEnv)

        createEntityConfigClassesLoader(context, processingEnv)
    }

    private fun createEntityConfigClasses(context: SourceCodeGeneratorContext, processingEnv: ProcessingEnvironment) {
        context.getEntityConfigsOrderedHierarchically().forEach { createEntityConfigClass(it, context, processingEnv) }
    }

    private fun createEntityConfigClass(entityConfig: EntityConfig, context: SourceCodeGeneratorContext, processingEnv: ProcessingEnvironment) {
        val className = (entityConfig.tableName.substring(0, 1).toUpperCase() + entityConfig.tableName.substring(1)) + "EntityConfig"
        val packageName = entityConfig.entityClass.`package`.name

        val entityClassName = ClassName.get(entityConfig.entityClass)

        val entityClassBuilder = TypeSpec.classBuilder(className)
                .superclass(EntityConfig::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(ReflectionHelper::class.java, "reflectionHelper", Modifier.PRIVATE)

        val constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception::class.java)
                .addParameter(EntityConfig::class.java, "parentEntity")

                .addStatement("super(\$T.class)", entityClassName)
                .addCode(System.lineSeparator())

                .addStatement("reflectionHelper = new \$T()", ReflectionHelper::class.java)
                .addStatement("reflectionHelper.makeAccessible(this.getConstructor())")
                .addCode(System.lineSeparator())

                .beginControlFlow("if(\$N != null)", "parentEntity")
                .addStatement("\$N.addChildEntityConfig(this)", "parentEntity")
                .endControlFlow()
                .addCode(System.lineSeparator())

                .addStatement("this.setTableName(\$S)", entityConfig.tableName)

                .addStatement("this.setCatalogName(\$S)", entityConfig.catalogName)
                .addStatement("this.setSchemaName(\$S)", entityConfig.schemaName)

        if(entityConfig.access == null) {
            constructorBuilder.addStatement("this.setAccess(null)")
        }
        else {
            constructorBuilder.addStatement("this.setAccess(\$T.\$L)", ClassName.get(AccessType::class.java), entityConfig.access.toString())
        }

        addNewLine(constructorBuilder)

        addCreateColumnConfigsMethod(entityClassBuilder, entityConfig, context)

        addLifeCycleMethods(entityClassBuilder, constructorBuilder, entityConfig)

        val constructor = constructorBuilder.build()

        val entityClass = entityClassBuilder
                .addMethod(constructor)
                .build()

        val javaFile = JavaFile.builder(packageName, entityClass)
                .build()

        javaFile.writeTo(processingEnv.filer)

        context.addEntityConfig(ClassName.get(packageName, className), entityConfig)
    }

    private fun addCreateColumnConfigsMethod(entityClassBuilder: TypeSpec.Builder, entityConfig: EntityConfig, context: SourceCodeGeneratorContext) {
        val columnConfigName = ClassName.get(ColumnConfig::class.java)

        val createColumnConfigsBuilder = MethodSpec.methodBuilder("createColumnConfigs")
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception::class.java)

        val targetEntityParameterNames = LinkedHashSet<String>()
        val targetEntities = LinkedHashSet<EntityConfig>()

        for(columnConfig in entityConfig.columns) {
            val createColumnConfigMethodName = "create" + (columnConfig.columnName.substring(0, 1).toUpperCase() + columnConfig.columnName.substring(1)) + "ColumnConfig"
            addCreateColumnConfigMethod(createColumnConfigMethodName, columnConfigName, entityClassBuilder, columnConfig)

            var targetEntityParameterValue = "null"
            columnConfig.targetEntity?.let { targetEntity ->
                targetEntityParameterValue = getEntityConfigVariableName(targetEntity)
                targetEntityParameterNames.add(targetEntityParameterValue)
                targetEntities.add(targetEntity)
            }

            if(columnConfig.isId) {
                createColumnConfigsBuilder.addStatement("\$T idColumn = \$N(\$L)", columnConfigName, createColumnConfigMethodName, targetEntityParameterValue)
                createColumnConfigsBuilder.addStatement("addColumn(idColumn)")
                createColumnConfigsBuilder.addStatement("setIdColumnAndSetItOnChildEntities(idColumn)")
            }
            else if(columnConfig.isVersion) {
                createColumnConfigsBuilder.addStatement("\$T versionColumn = \$N(\$L)", columnConfigName, createColumnConfigMethodName, targetEntityParameterValue)
                createColumnConfigsBuilder.addStatement("addColumn(versionColumn)")
                createColumnConfigsBuilder.addStatement("setVersionColumnAndSetItOnChildEntities(versionColumn)")
            }
            else {
                createColumnConfigsBuilder.addStatement("addColumn(\$N(\$L))", createColumnConfigMethodName, targetEntityParameterValue)
            }
        }

        for(targetEntityParameterName in targetEntityParameterNames) {
            createColumnConfigsBuilder.addParameter(EntityConfig::class.java, targetEntityParameterName)
        }

        entityClassBuilder.addMethod(createColumnConfigsBuilder.build())

        context.addTargetEntities(entityConfig, targetEntities)
    }

    private fun addCreateColumnConfigMethod(createColumnConfigMethodName: String, columnConfigName: ClassName, entityClassBuilder: TypeSpec.Builder, columnConfig: ColumnConfig) {
        val property = columnConfig.property
        val createGetterStatement = if(property.getter == null) {
            "null"
        }
        else {
            "this.getEntityClass().getDeclaredMethod(\"" + property.getter?.name + "\")"
        }
        val createSetterStatement = if(property.setter == null) {
            "null"
        }
        else {
            var parameterType = property.field.type.name
            if(parameterType == "[B") { // Kotlin ByteArray states itself as "[B"
                parameterType = "byte[]"
            }
            "this.getEntityClass().getDeclaredMethod(\"" + property.setter?.name + "\", " + parameterType + ".class)"
        }

        val createColumnConfigMethodBuilder = MethodSpec.methodBuilder(createColumnConfigMethodName)
                .addModifiers(Modifier.PRIVATE)
                .addException(ReflectiveOperationException::class.java)
                .addParameter(EntityConfig::class.java, "targetEntity")
                .returns(ColumnConfig::class.java)

                .addStatement("\$T column = new \$T(this, new \$T(this.getEntityClass().getDeclaredField(\$S), \$L, \$L))", columnConfigName, columnConfigName,
                        ClassName.get(Property::class.java), property.field.name, createGetterStatement, createSetterStatement)
                .addStatement("reflectionHelper.makeAccessible(column.getProperty())")
                .addCode(System.lineSeparator())

                .addStatement("column.setColumnName(\$S)", columnConfig.columnName)
                .addStatement("column.setTableName(\$S)", columnConfig.tableName)

                .addStatement("column.setDataType(" + if(columnConfig.dataType == null) "null)" else "\$T.\$L)", ClassName.get(DataType::class.java), columnConfig.dataType)
                .addCode(System.lineSeparator())

                .addStatement("column.setId(\$L)", columnConfig.isId)
                .addStatement("column.setGeneratedId(\$L)", columnConfig.isGeneratedId)
                .addStatement("column.setGeneratedIdType(\$T.\$L)", ClassName.get(GenerationType::class.java), columnConfig.generatedIdType)
                .addStatement("column.setIdGenerator(\$S)", columnConfig.idGenerator)
                .addStatement("column.setGeneratedIdSequence(\$S)", columnConfig.generatedIdSequence)
                .addCode(System.lineSeparator())

                .addStatement("column.setVersion(\$L)", columnConfig.isVersion)
                .addStatement("column.setLob(\$L)", columnConfig.isLob)
                .addCode(System.lineSeparator())

                .addStatement("column.setColumnDefinition(\$S)", columnConfig.columnDefinition)
                .addStatement("column.setLength(\$L)", columnConfig.length)
                .addStatement("column.setScale(\$L)", columnConfig.scale)
                .addStatement("column.setPrecision(\$L)", columnConfig.precision)
                .addCode(System.lineSeparator())

                .addStatement("column.setCanBeNull(\$L)", columnConfig.canBeNull)
                .addStatement("column.setUnique(\$L)", columnConfig.unique)
                .addStatement("column.setInsertable(\$L)", columnConfig.insertable)
                .addStatement("column.setUpdatable(\$L)", columnConfig.updatable)
                .addStatement("column.setFetch(\$T.\$L)", ClassName.get(FetchType::class.java), columnConfig.fetch)
                .addCode(System.lineSeparator())

                .addStatement("column.setRelationType(\$T.\$L)", ClassName.get(RelationType::class.java), columnConfig.relationType)
                .addCode(System.lineSeparator())

                .addStatement("column.setTargetEntity(targetEntity)")
                .addCode(System.lineSeparator())

                .addStatement("column.setOrphanRemoval(\$L)", columnConfig.orphanRemoval)
                .addStatement("column.setReferencedColumnName(\$S)", columnConfig.referencedColumnName)
                .addCode(System.lineSeparator())

                .addStatement("column.setJoinColumn(\$L)", columnConfig.isJoinColumn)

                .addStatement("column.setCascade(new \$T[] { \$N })", ClassName.get(CascadeType::class.java), columnConfig.cascade.joinToString { "CascadeType." + it.toString() })
                .addCode(System.lineSeparator())

                .addStatement("return column")

        entityClassBuilder.addMethod(createColumnConfigMethodBuilder.build())
    }

    private fun addLifeCycleMethods(entityClassBuilder: TypeSpec.Builder, constructorBuilder: MethodSpec.Builder, entityConfig: EntityConfig) {
        // TODO: make life cycle methods accessible

        val addLifeCycleMethodsBuilder = MethodSpec.methodBuilder("addLifeCycleMethods")
                .addModifiers(Modifier.PRIVATE)
                .addException(NoSuchMethodException::class.java)

        entityConfig.prePersistLifeCycleMethods.forEach {
            addLifeCycleMethodsBuilder.addStatement("addPrePersistLifeCycleMethod(this.getEntityClass().getDeclaredMethod(\"" + it.name + "\"))")
        }
        entityConfig.postPersistLifeCycleMethods.forEach {
            addLifeCycleMethodsBuilder.addStatement("addPostPersistLifeCycleMethod(this.getEntityClass().getDeclaredMethod(\"" + it.name + "\"))")
        }

        entityConfig.postLoadLifeCycleMethods.forEach {
            addLifeCycleMethodsBuilder.addStatement("addPostLoadLifeCycleMethod(this.getEntityClass().getDeclaredMethod(\"" + it.name + "\"))")
        }

        entityConfig.preUpdateLifeCycleMethods.forEach {
            addLifeCycleMethodsBuilder.addStatement("addPreUpdateLifeCycleMethod(this.getEntityClass().getDeclaredMethod(\"" + it.name + "\"))")
        }
        entityConfig.postUpdateLifeCycleMethods.forEach {
            addLifeCycleMethodsBuilder.addStatement("addPostUpdateLifeCycleMethod(this.getEntityClass().getDeclaredMethod(\"" + it.name + "\"))")
        }

        entityConfig.preRemoveLifeCycleMethods.forEach {
            addLifeCycleMethodsBuilder.addStatement("addPreRemoveLifeCycleMethod(this.getEntityClass().getDeclaredMethod(\"" + it.name + "\"))")
        }
        entityConfig.postRemoveLifeCycleMethods.forEach {
            addLifeCycleMethodsBuilder.addStatement("addPostRemoveLifeCycleMethod(this.getEntityClass().getDeclaredMethod(\"" + it.name + "\"))")
        }

        entityClassBuilder.addMethod(addLifeCycleMethodsBuilder.build())

        constructorBuilder.addStatement("addLifeCycleMethods()")
    }


    private fun createEntityConfigClassesLoader(context: SourceCodeGeneratorContext, processingEnv: ProcessingEnvironment) {
        val generatedEntityConfigsClassName = GeneratedEntityConfigsClassName
        val generatedEntityConfigsPackageName = GeneratedEntityConfigsPackageName

        val entityConfigClassName = ClassName.get(EntityConfig::class.java)
        val list = ClassName.get("java.util", "List")
        val arrayList = ClassName.get("java.util", "ArrayList")
        val listOfEntityConfigs = ParameterizedTypeName.get(list, entityConfigClassName)

        val getGeneratedEntityConfigsBuilder = MethodSpec.methodBuilder(GetGeneratedEntityConfigsMethodName) // TODO: extract to globally readable constants in API project
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception::class.java)
                .returns(listOfEntityConfigs)
                .addStatement("\$T result = new \$T<>()", listOfEntityConfigs, arrayList)

        val entityConfigVariableNames = HashMap<EntityConfig, String>()

        for(entityConfig in context.getEntityConfigsOrderedHierarchically()) {
            val className = context.getClassName(entityConfig)
            val parentEntity = entityConfig.parentEntity
            val parentEntityVariableName = if(parentEntity == null) "null" else getEntityConfigVariableName(context.getClassName(parentEntity))

            val variableName = getEntityConfigVariableName(className)
            entityConfigVariableNames.put(entityConfig, variableName)

            addNewLine(getGeneratedEntityConfigsBuilder)
            getGeneratedEntityConfigsBuilder.addStatement("\$T \$N = new \$T(\$N)", className, variableName, className, parentEntityVariableName)
            getGeneratedEntityConfigsBuilder.addStatement("result.add(\$N)", variableName)
        }

        addNewLine(getGeneratedEntityConfigsBuilder)
        for((entityConfig, variableName) in entityConfigVariableNames) {
            val targetEntities = context.getTargetEntities(entityConfig)
            val parameterList = targetEntities?.map { getEntityConfigVariableName(context.getClassName(it)) }?.filterNotNull()?.joinToString(", ")

            getGeneratedEntityConfigsBuilder.addStatement("\$N.createColumnConfigs(\$L)", variableName, parameterList)
        }

        addNewLine(getGeneratedEntityConfigsBuilder)
        getGeneratedEntityConfigsBuilder.addStatement("return result")

        val getGeneratedEntityConfigs = getGeneratedEntityConfigsBuilder.build()


        val entityClass = TypeSpec.classBuilder(generatedEntityConfigsClassName)
                .superclass(EntityConfig::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(getGeneratedEntityConfigs)
                .build()

        val javaFile = JavaFile.builder(generatedEntityConfigsPackageName, entityClass)
                .build()

        javaFile.writeTo(processingEnv.filer)
    }

    fun getEntityConfigVariableName(entityConfig: EntityConfig): String {
        return getEntityConfigVariableName(ClassName.get(entityConfig.entityClass))
    }

    fun getEntityConfigVariableName(className: ClassName?): String {
        className?.let {
            return className.simpleName().substring(0, 1).toLowerCase() + className.simpleName().substring(1)
        }

        return "null"
    }

    private fun addNewLine(methodBuilder: MethodSpec.Builder) {
        methodBuilder.addCode(System.lineSeparator())
    }

}