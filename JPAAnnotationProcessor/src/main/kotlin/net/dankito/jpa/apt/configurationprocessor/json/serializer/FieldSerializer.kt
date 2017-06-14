package net.dankito.jpa.apt.configurationprocessor.json.serializer

import java.lang.reflect.Field


class FieldSerializer : com.fasterxml.jackson.databind.ser.std.StdSerializer<Field>(java.lang.reflect.Field::class.java) {

    override fun serialize(value: java.lang.reflect.Field?, gen: com.fasterxml.jackson.core.JsonGenerator?, provider: com.fasterxml.jackson.databind.SerializerProvider?) {
        value?.let { field ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(SerializerConfig.Companion.ClassNameFieldName, field.declaringClass.name)
                jsonGenerator.writeStringField(SerializerConfig.Companion.FieldNameFieldName, field.name)

                jsonGenerator.writeEndObject()
            }
        }
    }

}