package net.dankito.jpa.apt.serializer.json

import com.fasterxml.jackson.databind.node.ObjectNode
import java.lang.reflect.Method


class MethodDeserializer : ReflectionClassesDeserializerBase<Method>(Method::class.java) {

    override fun instantiateObject(declaringClass: Class<*>, node: ObjectNode): Method {
        val methodName = node.get(SerializerConfig.MethodNameFieldName).asText()
        return declaringClass.getDeclaredMethod(methodName)
    }

}