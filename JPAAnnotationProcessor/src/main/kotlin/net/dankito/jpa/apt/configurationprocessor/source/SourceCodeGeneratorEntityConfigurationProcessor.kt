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
        val context = SourceCodeGeneratorContext()

        createEntityConfigClasses(entityConfiguration, context, processingEnv)

        createEntityConfigClassesLoader(context, processingEnv)
    }

    private fun createEntityConfigClasses(entityConfiguration: JPAEntityConfiguration, context: SourceCodeGeneratorContext, processingEnv: ProcessingEnvironment) {
        entityConfiguration.entities.forEach { createEntityConfigClass(it, context, processingEnv) }
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
                .addStatement("setParentEntity(\$N)", "parentEntity")
                .addStatement("this.\$N = \$S", "tableName", entityConfig.tableName)

        if(entityConfig.access == null) {
            constructorBuilder.addStatement("this.setAccess(null)")
        }
        else {
            constructorBuilder.addStatement("this.setAccess(\$T.\$L)", ClassName.get(AccessType::class.java), entityConfig.access.toString())
        }

        val constructor = constructorBuilder.build()

        val entityClass = entityClassBuilder
                .addMethod(constructor)
                .build()

        val javaFile = JavaFile.builder(packageName, entityClass)
                .build()

        javaFile.writeTo(processingEnv.filer)

        context.addEntityConfig(ClassName.get(packageName, className), entityConfig)
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

        for((className, entityConfig) in context.classNamesToEntityConfigsMap) {
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