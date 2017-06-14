package net.dankito.jpa.apt.configurationprocessor.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.Method


class MethodSerializer : StdSerializer<Method>(Method::class.java) {

    override fun serialize(value: Method?, gen: JsonGenerator?, provider: SerializerProvider?) {
        value?.let { method ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(SerializerConfig.ClassNameFieldName, method.declaringClass.name)
                jsonGenerator.writeStringField(SerializerConfig.MethodNameFieldName, method.name)

                jsonGenerator.writeEndObject()
            }
        }
    }

}