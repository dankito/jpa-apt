package net.dankito.jpa.apt.configurationprocessor.json.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.IOException
import java.lang.reflect.Method


class MethodDeserializer : StdDeserializer<Method>(Method::class.java) {

    @Throws(JsonProcessingException::class, IOException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Method {
        p?.let { jsonParser ->
            try {
                return deserializeMethodNode(jsonParser)
            } catch(e: Exception) {
                throw IOException("Could not deserialize Method", e)
            }
        }

        throw IOException("Could not deserialize method, JsonParser is null")
    }

    private fun deserializeMethodNode(jsonParser: JsonParser): Method {
        val node = jsonParser.getCodec().readTree<ObjectNode>(jsonParser)

        val className = node.get(SerializerConfig.ClassNameFieldName).asText()
        val methodName = node.get(SerializerConfig.MethodNameFieldName).asText()

        try {
            val declaringClass = Class.forName(className)
            return declaringClass.getDeclaredMethod(methodName)
        } catch(e: Exception) {
            throw IOException("Could not deserialize Method from class name \'$className\' and method name \'$methodName\'", e)
        }
    }

}