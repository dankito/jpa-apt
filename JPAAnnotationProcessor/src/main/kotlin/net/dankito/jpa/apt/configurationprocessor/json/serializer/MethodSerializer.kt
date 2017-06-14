package net.dankito.jpa.apt.configurationprocessor.json.serializer

import com.fasterxml.jackson.core.JsonGenerator
import java.lang.reflect.Method


class MethodSerializer : ReflectionClassesSerializerBase<Method>(Method::class.java) {

    override fun writeInstanceIdentifier(jsonGenerator: JsonGenerator, value: Method) {
        jsonGenerator.writeStringField(SerializerConfig.MethodNameFieldName, value.name)
    }

}