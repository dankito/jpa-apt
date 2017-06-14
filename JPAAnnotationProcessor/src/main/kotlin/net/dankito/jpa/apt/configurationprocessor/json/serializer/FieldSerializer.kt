package net.dankito.jpa.apt.configurationprocessor.json.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.Field


class FieldSerializer : StdSerializer<Field>(Field::class.java) {

    override fun serialize(value: Field?, gen: JsonGenerator?, provider: SerializerProvider?) {
        value?.let { field ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(SerializerConfig.ClassNameFieldName, field.declaringClass.name)
                jsonGenerator.writeStringField(SerializerConfig.FieldNameFieldName, field.name)

                jsonGenerator.writeEndObject()
            }
        }
    }

}