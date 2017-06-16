package net.dankito.jpa.apt.configurationprocessor.source

import com.squareup.javapoet.*
import net.dankito.jpa.apt.config.*
import net.dankito.jpa.apt.configurationprocessor.IEntityConfigurationProcessor
import java.lang.Exception
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.persistence.AccessType
import javax.persistence.FetchType
import javax.persistence.GenerationType


class SourceCodeGeneratorEntityConfigurationProcessor : IEntityConfigurationProcessor {

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

        val constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception::class.java)
                .addParameter(EntityConfig::class.java, "parentEntity")
                .addStatement("super(\$T.class)", entityClassName)
                .beginControlFlow("if(\$N != null)", "parentEntity")
                .addStatement("\$N.addChildEntityConfig(this)", "parentEntity")
                .addStatement("setIdColumnAndSetItOnChildEntities(\$N.getIdColumn())", "parentEntity")
                .addStatement("setVersionColumnAndSetItOnChildEntities(\$N.getVersionColumn())", "parentEntity")
                .endControlFlow()
                .addStatement("this.\$N = \$S", "tableName", entityConfig.tableName)

        if(entityConfig.access == null) {
            constructorBuilder.addStatement("this.setAccess(null)")
        }
        else {
            constructorBuilder.addStatement("this.setAccess(\$T.\$L)", ClassName.get(AccessType::class.java), entityConfig.access.toString())
        }

        addColumnConfigs(entityClassBuilder, constructorBuilder, entityConfig)

        val constructor = constructorBuilder.build()

        val entityClass = entityClassBuilder
                .addMethod(constructor)
                .build()

        val javaFile = JavaFile.builder(packageName, entityClass)
                .build()

        javaFile.writeTo(processingEnv.filer)

        context.addEntityConfig(ClassName.get(packageName, className), entityConfig)
    }

    private fun addColumnConfigs(entityClassBuilder: TypeSpec.Builder, constructorBuilder: MethodSpec.Builder, entityConfig: EntityConfig) {
        val columnConfigName = ClassName.get(ColumnConfig::class.java)

        for(columnConfig in entityConfig.columns) {
            val createColumnConfigMethodName = "create" + (columnConfig.columnName.substring(0, 1).toUpperCase() + columnConfig.columnName.substring(1)) + "ColumnConfig"
            addCreateColumnConfigMethod(createColumnConfigMethodName, columnConfigName, entityClassBuilder, columnConfig)

            if(columnConfig.isId) {
                constructorBuilder.addStatement("\$T idColumn = \$N()", columnConfigName, createColumnConfigMethodName)
                constructorBuilder.addStatement("addColumn(idColumn)")
                constructorBuilder.addStatement("setIdColumnAndSetItOnChildEntities(idColumn)")
            }
            else if(columnConfig.isVersion) {
                constructorBuilder.addStatement("\$T versionColumn = \$N()", columnConfigName, createColumnConfigMethodName)
                constructorBuilder.addStatement("addColumn(versionColumn)")
                constructorBuilder.addStatement("setVersionColumnAndSetItOnChildEntities(versionColumn)")
            }
            else {
                constructorBuilder.addStatement("addColumn(\$N())", createColumnConfigMethodName)
            }
        }
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
            "this.getEntityClass().getDeclaredMethod(\"" + property.setter?.name + "\")"
        }

        val createColumnConfigMethodBuilder = MethodSpec.methodBuilder(createColumnConfigMethodName)
                .addException(ReflectiveOperationException::class.java)
                .returns(ColumnConfig::class.java)
                .addStatement("\$T column = new \$T(this, new \$T(this.getEntityClass().getDeclaredField(\$S), \$L, \$L))", columnConfigName, columnConfigName,
                        ClassName.get(Property::class.java), property.field.name, createGetterStatement, createSetterStatement)
                .addStatement("column.setColumnName(\$S)", columnConfig.columnName)
                .addStatement("column.setTableName(\$S)", columnConfig.tableName)

                .addStatement("column.setDataType(" + if(columnConfig.dataType == null) "null)" else "\$T.\$L)", ClassName.get(DataType::class.java), columnConfig.dataType)

                .addStatement("column.setId(\$L)", columnConfig.isId)
                .addStatement("column.setGeneratedId(\$L)", columnConfig.isGeneratedId)
                .addStatement("column.setGeneratedIdType(\$T.\$L)", ClassName.get(GenerationType::class.java), columnConfig.generatedIdType)
                .addStatement("column.setIdGenerator(\$S)", columnConfig.idGenerator)
                .addStatement("column.setGeneratedIdSequence(\$S)", columnConfig.generatedIdSequence)

                .addStatement("column.setVersion(\$L)", columnConfig.isVersion)
                .addStatement("column.setLob(\$L)", columnConfig.isLob)

                .addStatement("column.setColumnDefinition(\$S)", columnConfig.columnDefinition)
                .addStatement("column.setLength(\$L)", columnConfig.length)
                .addStatement("column.setScale(\$L)", columnConfig.scale)
                .addStatement("column.setPrecision(\$L)", columnConfig.precision)

                .addStatement("column.setCanBeNull(\$L)", columnConfig.canBeNull)
                .addStatement("column.setUnique(\$L)", columnConfig.unique)
                .addStatement("column.setInsertable(\$L)", columnConfig.insertable)
                .addStatement("column.setUpdatable(\$L)", columnConfig.updatable)
                .addStatement("column.setFetch(\$T.\$L)", ClassName.get(FetchType::class.java), columnConfig.fetch)

                .addStatement("column.setRelationType(\$T.\$L)", ClassName.get(RelationType::class.java), columnConfig.relationType)

                .addStatement("return column")

        entityClassBuilder.addMethod(createColumnConfigMethodBuilder.build())
    }


    private fun createEntityConfigClassesLoader(context: SourceCodeGeneratorContext, processingEnv: ProcessingEnvironment) {
        val generatedEntityConfigsClassName = "GeneratedEntityConfigs" // TODO: extract to globally readable constants in API project
        val generatedEntityConfigsPackageName = "net.dankito.jpa.apt.generated" // TODO: extract to globally readable constants in API project

        val entityConfigClassName = ClassName.get(EntityConfig::class.java)
        val list = ClassName.get("java.util", "List")
        val arrayList = ClassName.get("java.util", "ArrayList")
        val listOfEntityConfigs = ParameterizedTypeName.get(list, entityConfigClassName)

        val getGeneratedEntityConfigsBuilder = MethodSpec.methodBuilder("getGeneratedEntityConfigs") // TODO: extract to globally readable constants in API project
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception::class.java)
                .returns(listOfEntityConfigs)
                .addStatement("\$T result = new \$T<>()", listOfEntityConfigs, arrayList)

        for(entityConfig in context.getEntityConfigsOrderedHierarchically()) {
            val className = context.getClassName(entityConfig)
            val parentEntity = entityConfig.parentEntity
            val parentEntityVariableName = if(parentEntity == null) "null" else getEntityConfigVariableName(context.getClassName(parentEntity))

            if(entityConfig.childEntities.size == 0) {
                getGeneratedEntityConfigsBuilder.addStatement("result.add(new \$T(\$N))", className, parentEntityVariableName)
            }
            else {
                val variableName = getEntityConfigVariableName(className)
                getGeneratedEntityConfigsBuilder.addStatement("\$T \$N = new \$T(\$N)", className, variableName, className, parentEntityVariableName)
                getGeneratedEntityConfigsBuilder.addStatement("result.add(\$N)", variableName)
            }
        }

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

    fun getEntityConfigVariableName(className: ClassName?): String {
        className?.let {
            return className.simpleName().substring(0, 1).toLowerCase() + className.simpleName().substring(1)
        }

        return "null"
    }

}