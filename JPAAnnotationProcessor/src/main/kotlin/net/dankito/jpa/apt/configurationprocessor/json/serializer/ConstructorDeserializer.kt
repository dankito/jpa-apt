package net.dankito.jpa.apt.configurationprocessor.json.serializer

import com.fasterxml.jackson.databind.node.ObjectNode
import java.lang.reflect.Constructor


class ConstructorDeserializer : ReflectionClassesDeserializerBase<Constructor<*>>(Constructor::class.java) {

    override fun instantiateObject(declaringClass: Class<*>, node: ObjectNode): Constructor<*> {
        return declaringClass.getDeclaredConstructor()
    }

}