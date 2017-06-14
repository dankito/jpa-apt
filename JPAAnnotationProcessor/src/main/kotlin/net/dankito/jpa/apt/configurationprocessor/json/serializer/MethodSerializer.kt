package net.dankito.jpa.apt.configurationprocessor.json.serializer

import java.lang.reflect.Method


class MethodSerializer : com.fasterxml.jackson.databind.ser.std.StdSerializer<Method>(java.lang.reflect.Method::class.java) {

    override fun serialize(value: java.lang.reflect.Method?, gen: com.fasterxml.jackson.core.JsonGenerator?, provider: com.fasterxml.jackson.databind.SerializerProvider?) {
        value?.let { method ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(SerializerConfig.Companion.ClassNameFieldName, method.declaringClass.name)
                jsonGenerator.writeStringField(SerializerConfig.Companion.MethodNameFieldName, method.name)

                jsonGenerator.writeEndObject()
            }
        }
    }

}