package org.guy.library.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Builds the system instruction prompt telling the AI how to format the JSON
 * based on the provided generic Type.
 */
internal fun buildSystemInstruction(type: Type): String {
    val isCollection = isCollectionType(type)
    val rootInstruction = if (isCollection) {
        "Because some APIs require the root of a JSON response to be an Object, you MUST wrap the array in a root JSON object with a single key named `data`."
    } else {
        ""
    }

    return """
        You are a strict data-generator. 
        You MUST return ONLY valid JSON matching this schema/structure based on the requested type.
        Do not wrap the JSON in markdown code blocks.
        Never include commas or currency symbols (like $, €, £) inside numeric fields, return pure raw numbers (e.g. 1000.50 instead of "$1,000.50").
        $rootInstruction
        
        ${describeType(type)}
    """.trimIndent()
}

internal fun isCollectionType(type: Type): Boolean {
    if (type is Class<*> && type.isArray) return true
    if (type is ParameterizedType) {
        val rawType = type.rawType as? Class<*>
        if (rawType != null && Collection::class.java.isAssignableFrom(rawType)) {
            return true
        }
    }
    return false
}

/**
 * Recursively describes a JVM Type (Class or ParameterizedType) so the AI
 * knows what JSON properties to generate.
 */
private fun describeType(type: Type): String {
    if (type is Class<*>) {
        if (type.isArray) {
            return "A JSON array containing objects of the following type:\n" + describeType(type.componentType)
        }
        if (type.isPrimitive || type == String::class.java || Number::class.java.isAssignableFrom(type) || type == Boolean::class.javaObjectType || type == Boolean::class.javaPrimitiveType) {
            val baseDesc = "Primitive/Simple Type: ${type.simpleName}"
            if (type == Double::class.javaObjectType || type == Double::class.javaPrimitiveType || type == Float::class.javaObjectType || type == Float::class.javaPrimitiveType) {
                return "$baseDesc (MUST be a raw decimal number, e.g 1000.50. Extract the FULL numeric value from the text. DO NOT truncate it. DO NOT use commas or currency signs)"
            }
            return baseDesc
        }
        
        val fieldsDesc = type.declaredFields.joinToString("\n") { field ->
            val fieldTypeDesc = describeType(field.genericType).replace("\n", " ")
            "- \"${field.name}\": $fieldTypeDesc"
        }
        
        return "JSON Object Name: ${type.simpleName}\nRequired Properties (You MUST include ALL of these properties, they CANNOT be null or omitted. For missing text/strings, use \"\". For missing arrays/lists, you MUST use `[]`): \n$fieldsDesc"
        
    } else if (type is ParameterizedType) {
        val rawType = type.rawType as? Class<*>
        if (rawType != null && Collection::class.java.isAssignableFrom(rawType)) {
            val typeArg = type.actualTypeArguments.firstOrNull()
            if (typeArg != null) {
                return "A JSON array `[ ... ]` containing objects of the following type: " + describeType(typeArg)
            }
        }
        return "Generic Type structure: $type"
    }
    return "Type: $type"
}
