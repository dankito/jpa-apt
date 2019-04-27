package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.*
import net.dankito.jpa.apt.config.Modifier
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.NoType
import javax.lang.model.type.TypeMirror
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Transient
import javax.tools.Diagnostic


open class AptAnnotationReader(protected val processingEnv: ProcessingEnvironment,
                               protected val roundEnv: RoundEnvironment) : IAnnotationReader {

    protected val entityTypes = HashMap<Type, EntityTypeInfo>()

    protected val elementsForEntityTypes = HashMap<Type, TypeElement>()

    protected val entityConfigRegistry = LinkedHashMap<Type, EntityConfig>()

    protected val entityConfigsInOrderAdded = ArrayList<EntityConfig>() // TODO: may could be removed

    // it's very important to keep order so that later on EntityConfigs get created according to their hierarchy (EntityConfigs of base classes have to be created first)
    protected val entityConfigsFromPreviousBuiltProjects = LinkedHashMap<Type, EntityConfig>()


    protected var methods: List<Method>? = null


    protected val propertyRegistry = HashMap<Field, Property>() // TODO: propertyRegistry is actually never used

    protected val columnRegistry = HashMap<Property, ColumnConfig>()


    init {
        // TODO
        entityConfigsFromPreviousBuiltProjects.forEach { // also add previously created EntityConfigs so we know them if they are parent classes of this module's entities.
            registerEntityType(it.key, EntityTypeInfo(it.key, null), null) // entityElement can be set to null here as EntityConfig is already known and we don't need to re-read it
        }

        createEntityTypesHierarchically()
    }


    // TODO: may move to EntityConfigurationReader
    override fun getTopLevelEntities(): List<EntityTypeInfo> {
        return entityTypes.values.filter { it.superClass == null }
    }


    override fun registerEntityConfig(entityConfig: EntityConfig) {
        entityConfigsInOrderAdded.add(entityConfig)
        entityConfigRegistry.put(entityConfig.type, entityConfig)
    }

    override fun getEntityConfigForType(type: Type) : EntityConfig? {
        entityConfigRegistry[type]?.let {
            return it
        }

        return entityConfigsFromPreviousBuiltProjects[type]
    }

    override fun getEntityConfigsInOrderAdded() : List<EntityConfig> {
//        return entityConfigsInOrderAdded
        return entityConfigRegistry.values.toList() // should be in the same order as entityConfigsInOrderAdded
    }


    override fun registerProperty(property: Property) {
        propertyRegistry.put(property.field, property)
    }

    override fun registerColumn(columnConfig: ColumnConfig) {
        columnRegistry.put(columnConfig.property, columnConfig)
    }

    override fun getColumnConfiguration(property: Property) : ColumnConfig? {
        return columnRegistry[property]
    }


    // TODO: try to get rid of MappedSuperclass and Entity, e.g. via calling a method createEntityConfigForAnnotation()
    //  from EntityConfigurationReader to get independent from javax.persistence dependency
    protected open fun createEntityTypesHierarchically() {
        val mappedSuperclasses = roundEnv.getElementsAnnotatedWith(MappedSuperclass::class.java)
        createEntityTypeInfosForElements(mappedSuperclasses)

        val entityClasses = roundEnv.getElementsAnnotatedWith(Entity::class.java)
        createEntityTypeInfosForElements(entityClasses)

        mapEntitiesHierarchy(mappedSuperclasses, entityClasses)
    }

    protected open fun createEntityTypeInfosForElements(elements: Collection<Element>) {
        elements.mapNotNull { it as? TypeElement }.forEach { typeElement ->
            try {
                createEntityTypeInfoForElement(typeElement)
            } catch(e: Exception) {
                logError("Could not create EntityTypeInfo for element $typeElement", e)
            }
        }
    }

    protected open fun createEntityTypeInfoForElement(element: TypeElement) {
        val entityClassName = element.qualifiedName.toString()

        logInfo("Trying to load element with qualifiedName $entityClassName and simpleName ${element.simpleName}. Super class: ${element.superclass} (${element.superclass.javaClass})")

        val type = typeFromElement(element)

        val classAnnotations = element.annotationMirrors.associateBy( { it.annotationType }, { it.elementValues })
        val properties = element.enclosedElements.filter { it.kind == ElementKind.FIELD }
                .mapNotNull { it as? VariableElement }.associateBy( { it.simpleName.toString() }, { it })
        val methods = element.enclosedElements.filter { it.kind == ElementKind.METHOD }
                .mapNotNull { it as? ExecutableElement }.associateBy( { it.simpleName.toString() }, { it })

        val entity = EntityTypeInfo(type, element, classAnnotations, properties, methods)

        registerEntityType(type, entity, element)
    }

    protected open fun registerEntityType(type: Type, entity: EntityTypeInfo, element: TypeElement?) {
        entityTypes.put(type, entity)

        element?.let {
            elementsForEntityTypes.put(type, element)
        }
    }

    protected open fun mapEntitiesHierarchy(mappedSuperclasses: MutableSet<out Element>, entityClasses: MutableSet<out Element>) {
        mappedSuperclasses.mapNotNull { it as? TypeElement }.forEach { entity ->
            findAndSetSuperclass(entity)
        }

        entityClasses.mapNotNull { it as? TypeElement }.forEach { entity ->
            findAndSetSuperclass(entity)
        }
    }

    protected open fun findAndSetSuperclass(entity: TypeElement) {
        entity.superclass?.let { superclass ->
            ((superclass as? DeclaredType)?.asElement() as? TypeElement)?.let { superclassElement ->

                if (superclassElement.superclass is NoType == false &&
                        superclassElement.qualifiedName.toString() != "java.lang.Object") {
                    setSuperclass(superclassElement, entity)
                }
            }
        }
    }

    protected open fun setSuperclass(superclassElement: TypeElement, entity: TypeElement) {
        getTypeFor(superclassElement)?.let { superClassEntityType ->
            getTypeFor(entity)?.let { entityType ->
                entityType.superClass = superClassEntityType
                superClassEntityType.addChildClass(entityType)
            }
        }
    }

    protected open fun getTypeFor(element: TypeElement): EntityTypeInfo? {
        return entityTypes[typeFromElement(element)]
    }

    protected open fun typeFromElement(element: QualifiedNameable, genericArguments: List<Type> = listOf()): Type {
        val packageElement = getPackageElement(element)

        val packageName = packageElement?.qualifiedName?.toString() ?: getPackageNameFromQualifiedName(element)

        return Type(element.simpleName.toString(), packageName, element.qualifiedName.toString(), genericArguments,
                element.kind == ElementKind.ENUM)
    }

    protected open fun typeFromTypeMirror(typeMirror: TypeMirror): Type {
        val typeArguments = (typeMirror as? DeclaredType)?.typeArguments ?: listOf() // e.g. generic arguments
        val genericArguments = typeArguments.map { typeFromTypeMirror(it) }

        if (typeMirror is DeclaredType) {
            val element = typeMirror.asElement() // Element doesn't have (generic) type information anymore

            if (element is QualifiedNameable) {
                return typeFromElement(element, genericArguments)
            }
        }

        return typeFromQualifiedName(typeMirror.toString(), genericArguments)
    }

    override fun typeFromQualifiedName(qualifiedName: String): Type {
        return typeFromQualifiedName(qualifiedName, listOf())
    }

    fun typeFromQualifiedName(qualifiedName: String, genericArguments: List<Type>): Type {
        val adjustedQualifiedName = qualifiedName.replace("? extends ", "") // generic arguments are prepended with '? extends '

        var className = adjustedQualifiedName
        var packageName = ""

        try { // for method parameters class name and package name really aren't that important
            val dollarIndex = adjustedQualifiedName.lastIndexOf('$')
            if (dollarIndex > 0) {
                packageName = adjustedQualifiedName.substring(0, dollarIndex)
                className = adjustedQualifiedName.substring(dollarIndex + 1)
            }
            else {
                val lastDotIndex = adjustedQualifiedName.lastIndexOf('.')
                if (lastDotIndex > 0) {
                    packageName = adjustedQualifiedName.substring(0, lastDotIndex)
                    className = adjustedQualifiedName.substring(lastDotIndex + 1)
                }
            }
        } catch (e: Exception) {
            logWarn("Could not get class name and package name from full qualified type $qualifiedName." +
                    "But as class name and package name for method parameters aren't that important, ignoring " +
                    "exception and continuing without ...", e)
        }

        return Type(className, packageName, adjustedQualifiedName, genericArguments)
    }

    protected open fun getPackageElement(element: QualifiedNameable): PackageElement? {
        var enclosingElement = element.enclosingElement

        while (enclosingElement != null && enclosingElement is PackageElement == false) {
            enclosingElement = enclosingElement.enclosingElement
        }

        return enclosingElement as? PackageElement
    }

    protected open fun getPackageNameFromQualifiedName(element: QualifiedNameable): String {
        return element.qualifiedName.substring(0, element.qualifiedName.length - (element.simpleName.length + 1))
    }


    /**
     * Locate the no arg constructor for the class.
     */
    @Throws(IllegalArgumentException::class)
    override fun checkIfHasNoArgConstructor(entity: EntityTypeInfo): Boolean {

        val noArgConstructor = findNoArgConstructor(entity)

        if (noArgConstructor != null) {
            return true
        }

        val errorMessage = "Can't find a no-arg constructor for ${entity.type.qualifiedName}"

        if (entity.entityElement?.enclosingElement == null) { // but enclosingElement should be package, isn't it?
            logError(errorMessage)
        }
        else {
            logError("$errorMessage. Missing static on inner class?")
        }

        throw IllegalArgumentException(errorMessage)
    }

    protected open fun findNoArgConstructor(entity: EntityTypeInfo): Element? {
        return entity.entityElement?.enclosedElements
                ?.filter { it.kind == ElementKind.CONSTRUCTOR }
                ?.firstOrNull { it.enclosedElements.isEmpty() }
    }


    override fun <T : Annotation> getAnnotation(entityConfig: EntityConfig, annotationType: Class<T>): T? {
        elementsForEntityTypes[entityConfig.type]?.let { element ->
            return element.getAnnotation(annotationType)
        }

        return null
    }


    override fun getMethods(entityConfig: EntityConfig): List<Method> {
        methods?.let {
            return it
        }

        val retrievedMethods =  getMethodElements(entityConfig)
                .mapNotNull { it as? ExecutableElement }
                .map { createMethod(it) }

        this.methods = retrievedMethods

        return retrievedMethods
    }

    protected open fun getMethodElements(entityConfig: EntityConfig): List<Element> {
        elementsForEntityTypes[entityConfig.type]?.let { element ->
            return element.enclosedElements.filter { it.kind == ElementKind.METHOD }
        }

        return listOf()
    }

    protected open fun createMethod(element: ExecutableElement): Method {
        val paramClass = element.parameters.map { it.asType() }

        return Method(element.simpleName.toString(), typeFromTypeMirror(element.returnType),
                paramClass.map { typeFromTypeMirror(it) }, readAnnotations(element), mapModifiers(element))
    }

    override fun <T : Annotation> getAnnotation(entityConfig: EntityConfig, method: Method, annotationType: Class<T>): T? {
        getMethodElements(entityConfig).firstOrNull { it.simpleName.toString() == method.name }?.let { methodElement -> // TODO: also check arguments
            return methodElement.getAnnotation(annotationType)
        }

        return null
    }


    override fun getNonStaticNonTransientFields(entityConfig: EntityConfig): List<Field> {
        // TODO: may use
//        val fields = ElementFilter.fieldsIn(listOf(element))

        return elementsForEntityTypes[entityConfig.type]?.enclosedElements
                ?.filter { it.kind == ElementKind.FIELD }
                ?.mapNotNull { it as? VariableElement }
                ?.map { Field(it.simpleName.toString(), typeFromTypeMirror(it.asType()),
                        readAnnotations(it), mapModifiers(it)) }
                ?.filter { isStaticOrTransient(it) == false }
                ?: listOf()
    }

    protected open fun readAnnotations(element: Element): List<Annotation> {
        val types = element.annotationMirrors.mapNotNull { it.annotationType.asElement() as? TypeElement }

        return types.mapNotNull { annotationType ->
            // TODO: this works only for already compiled annotations, that is for annotations that are not defined in
            //  the same library / project that calls (k)apt!
            try {
                (Class.forName(annotationType.qualifiedName.toString()) as? Class<Annotation>)?.let { annotationClass ->
                    return@mapNotNull element.getAnnotation(annotationClass)
                }
            } catch (e: Exception) {
                logWarn("Could not get Annotation of type ${annotationType.qualifiedName}")
            }

            null
        }
    }

    override fun getNonStaticNonAbstractNonTransientMethodsMap(entityConfig: EntityConfig): MutableMap<String, Method> {

        return getMethods(entityConfig)
                .filter { isNonStaticNonAbstractNonTransient(it) }
                .associateBy( { it.name }, { it } )
                .toMutableMap()
    }

    protected open fun mapModifiers(element: Element): List<Modifier> {
        return element.modifiers.mapNotNull { modifier ->
            when (modifier) {
                javax.lang.model.element.Modifier.PUBLIC -> Modifier.Public
                javax.lang.model.element.Modifier.PROTECTED -> Modifier.Protected
                javax.lang.model.element.Modifier.PRIVATE -> Modifier.Private
                javax.lang.model.element.Modifier.ABSTRACT -> Modifier.Abstract
                javax.lang.model.element.Modifier.STATIC -> Modifier.Static
                javax.lang.model.element.Modifier.FINAL -> Modifier.Final
                javax.lang.model.element.Modifier.TRANSIENT -> Modifier.Transient
                else -> null
            }
        }
    }

    protected open fun isStaticOrTransient(type: ElementBase): Boolean {
        return type.isStatic || type.isTransient
    }

    protected open fun isNonStaticNonAbstractNonTransient(type: ElementBase): Boolean {
        return type.isStatic == false && type.isAbstract == false && type.isTransient == false
    }

    // TODO: also call for isTransient()
    protected open fun isTransient(element: Element): Boolean {
        return element.getAnnotation(Transient::class.java) != null // TODO: get rid of javax.persistence.Transient, e. g. via a Class.forName("javax.persistence.Transient") to be independent from javax.persistence dependency
                || element.getAnnotation(java.beans.Transient::class.java) != null
                || element.getAnnotation(kotlin.jvm.Transient::class.java) != null
    }


    override fun logInfo(message: String) {
        logMessage(Diagnostic.Kind.NOTE, message)
    }

    override fun logWarn(message: String) {
        logMessage(Diagnostic.Kind.WARNING, message)
    }

    override fun logWarn(message: String, exception: Exception) {
        logWarn("$message: $exception") // TODO: also log stack trace?
    }

    override fun logError(message: String) {
        logMessage(Diagnostic.Kind.ERROR, message)
    }

    override fun logError(message: String, exception: Exception) {
        logError("$message: $exception") // TODO: also log stack trace?
    }

    protected open fun logMessage(kind: Diagnostic.Kind, message: String) {
        processingEnv.messager.printMessage(kind, message)
    }

}