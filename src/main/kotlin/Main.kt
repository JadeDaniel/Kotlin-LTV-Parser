package co.couldbe.demo

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.Base64

// Press <shortcut actionId="Debug"/> to start debugging your code.
// Press <shortcut actionId="Run"/> to run your code.

fun main() {
    val FCITemplate = Tag(TagType.CONSTRUCTED, 0x6F)
    val DF = Tag(TagType.PRIMITIVE, 0x84.toByte() /*why is to byte needed for 0x84, but not 0x6F? */, FCITemplate)
    val tags: List<Tag> = listOf(FCITemplate, DF)

    val bytes = getSampleTlv()
    val tag1 = 0x6F

    val tagStack: MutableList<Tag> = mutableListOf()
    var length: Int? = null
    var value: ByteArray? = null

    ByteArrayInputStream(bytes).use {
        val byte = it.read().toByte()
        if (tagStack.isEmpty()) {
            // TODO handle multi-byte tags
            val match = tags.find { it.tag == byte } // look in ALL tags

            if (match != null) {
                tagStack.add(match)
            }
        } else {
            if (tagStack.last().type == TagType.CONSTRUCTED) {
                // look for nesting tags
                val match = tags.find { it.template == tagStack.last() && it.tag == byte }
                if (match != null) {
                    tagStack.add(match)
                }
            }
        }
        if (length == null) {
            length = it.read()
        }
        if (value == null) {
            value = ByteArray(length)
            it.read(value)
            length -= 1
        }

        // TODO length needs to be tracked per level of the stack, I Guess...
        if (length == 0)
            tagStack.removeLast()
    }

    tagStack.forEach { println(it) }
}

fun getSampleTlv(): ByteArray {
    return Base64.getDecoder().decode("bxqEDjFQQVkuU1lTLkRERjAxpQiIAQJfLQJlbg==")
}



enum class TagType {
    PRIMITIVE,
    CONSTRUCTED
}

data class Tag (
    val type: TagType,
    val tag: Byte,
    /* template is a pointer to a tag that this tag appears on */
    val template: Tag? = null,
) {
    override fun toString(): String {
        return "Tag(type=$type, tag=${tag.toString(16)}, template=$template)"
    }
}

// TODO make ext fun to take bytes, turn them into toString(radix=16) uppercase