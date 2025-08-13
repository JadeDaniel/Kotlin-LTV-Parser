package co.couldbe.demo

import co.couldbe.demo.model.Node
import co.couldbe.demo.tags.EMVTags
import co.couldbe.demo.tags.Tag
import co.couldbe.demo.tags.TagDefinition
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

@OptIn(ExperimentalUnsignedTypes::class)
fun parseTag(stream: ByteArrayInputStream): Tag {
    // TODO handle case where stream is exhausted
    // TODO validate that found tag has the appropriate template for the last item on the tag stack, if in a constructed tag
    var tagBytes = UByteArray(0)
    var tagDef: TagDefinition? = null

    while(tagBytes.size < EMVTags.MAX_TAG_LENGTH) { // only look for definitions up to N bytes long
        tagBytes += stream.read().toUByte() // read next byte and add to buffer
        tagDef = EMVTags.fromBytes(tagBytes) // attempt to find a matching tag definition
        if (tagDef != null) break // if found, stop looking
    }
    if (tagDef == null) { // no tag found
        throw IllegalArgumentException("Unknown tag ${tagBytes.prettyHex()}")
    }

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

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.prettyHex(): String = this.joinToString(" ") { it.toString(16).uppercase().padStart(2, '0') }