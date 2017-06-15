package net.dankito.jpa.apt.serializer.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.IOException
import java.lang.reflect.Member


abstract class ReflectionClassesDeserializerBase<T : Member>(clazz: Class<T>) : StdDeserializer<T>(clazz) {

    @Throws(JsonProcessingException::class, IOException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): T {
        p?.let { jsonParser ->
            try {
                return deserializeReflectionClassNode(jsonParser)
            } catch(e: Exception) {
                throw IOException("Could not deserialize Reflection class", e)
            }
        }

        throw IOException("Could not deserialize Reflection class, JsonParser is null")
    }

    private fun deserializeReflectionClassNode(jsonParser: JsonParser): T {
        val node = jsonParser.getCodec().readTree<ObjectNode>(jsonParser)

        val className = node.get(SerializerConfig.Companion.ClassNameFieldName).asText()

        try {
            val declaringClass = Class.forName(className)

            return instantiateObject(declaringClass, node)
        } catch(e: Exception) {
            throw IOException("Could not deserialize Reflection class from declaring class \'$className\'", e)
        }
    }

    abstract fun instantiateObject(declaringClass: Class<*>, node: ObjectNode): T

}