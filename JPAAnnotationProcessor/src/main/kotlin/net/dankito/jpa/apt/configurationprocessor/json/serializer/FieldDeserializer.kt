package net.dankito.jpa.apt.configurationprocessor.json.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.IOException
import java.lang.reflect.Field


class FieldDeserializer : StdDeserializer<Field>(Field::class.java) {

    @Throws(JsonProcessingException::class, IOException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Field {
        p?.let { jsonParser ->
            try {
                return deserializeFieldNode(jsonParser)
            } catch(e: Exception) {
                throw IOException("Could not deserialize Field", e)
            }
        }

        throw IOException("Could not deserialize field, JsonParser is null")
    }

    private fun deserializeFieldNode(jsonParser: JsonParser): Field {
        val node = jsonParser.getCodec().readTree<ObjectNode>(jsonParser)

        val className = node.get(SerializerConfig.ClassNameFieldName).asText()
        val fieldName = node.get(SerializerConfig.FieldNameFieldName).asText()

        try {
            val declaringClass = Class.forName(className)
            return declaringClass.getDeclaredField(fieldName)
        } catch(e: Exception) {
            throw IOException("Could not deserialize Field from class name \'$className\' and field name \'$fieldName\'", e)
        }
    }

}