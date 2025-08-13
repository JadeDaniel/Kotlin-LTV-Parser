package co.couldbe.demo

import co.couldbe.demo.model.Node
import co.couldbe.demo.tags.EMVTags
import co.couldbe.demo.tags.Tag
import java.io.ByteArrayInputStream
import java.util.Base64

// Press <shortcut actionId="Debug"/> to start debugging your code.
// Press <shortcut actionId="Run"/> to run your code.

fun main() {
    val byteStream = getSampleTlv()

    val nodes = parseTags(ByteArrayInputStream(byteStream)).map { tag -> buildNode(tag) }.toList()
    println(nodes)
}

fun parseTags(stream: ByteArrayInputStream): Sequence<Tag> =
    generateSequence {
        if (stream.available() > 0) {
            parseTag(stream)
        } else {
            null
        }
    }

fun parseTag(stream: ByteArrayInputStream): Tag {
    // TODO handle case where stream is exhausted
    // TODO validate that found tag has the appropriate template for the last item on the tag stack, if in a constructed tag
    val byte = stream.read().toUByte()

    val tagDef = EMVTags.fromByte(byte) ?: throw IllegalArgumentException("Unknown tag ${byte.toString(16).uppercase()}")
    val length = stream.read()

    val value = ByteArray(length)
    stream.read(value)

    return Tag(tagDef, length, value)
}

fun buildNode(tag: Tag, parentTag: Tag? = null): Node {
    if (tag.isConstructed()) {
        val childrenNodes = parseTags(ByteArrayInputStream(tag.value)).map { childTag ->
            buildNode(childTag, tag)
        }
        return Node.Constructed(tag.definition, childrenNodes.toList())
    } else {
        return Node.Primitive(tag.definition, tag.value)
    }
}

fun getSampleTlv(): ByteArray {
    return Base64.getDecoder().decode("bxqEDjFQQVkuU1lTLkRERjAxpQiIAQJfLQJlbg==")
}