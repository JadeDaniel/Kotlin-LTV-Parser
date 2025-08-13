package co.couldbe.demo

import co.couldbe.demo.model.Node
import co.couldbe.demo.tags.TagDefinitions
import co.couldbe.demo.tags.Tag
import co.couldbe.demo.tags.TagDefinition
import java.io.ByteArrayInputStream
import java.util.Base64

/*
    TODO consider a CLI interface and even JSON output
 */

fun main() {
    val bytes = getSampleTlv()
    val nodes = parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
    prettyPrintNodes(nodes)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun prettyPrintNodes(nodes: List<Node>, level: Int = 0) {
    nodes.forEach { node ->
        val indent = "  ".repeat(level)
        val nodeValue = if (node is Node.Primitive) { " value [${node.value.toUByteArray().prettyHex()}]" } else ""
        println("$indent${node.tag.tag.prettyTagNameHex()} $nodeValue")
        if (node is Node.Constructed) {
            prettyPrintNodes(node.children, level + 1)
        }
    }
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
    var tagBytes = UByteArray(0)
    var tagDef: TagDefinition? = null

    while(tagBytes.size < TagDefinitions.MAX_TAG_LENGTH) { // only look for definitions up to N bytes long
        tagBytes += stream.read().toUByte() // read next byte and add to buffer
        tagDef = TagDefinitions.fromBytes(tagBytes) // attempt to find a matching tag definition
        if (tagDef != null) break // if found, stop looking
    }
    if (tagDef == null) { // no tag found
        throw IllegalArgumentException("Unknown tag ${tagBytes.prettyTagNameHex()}")
    }

    if (stream.available() < 1) throw IllegalArgumentException("Unexpected end of stream, no length value found following tag ${tagDef.tag.prettyTagNameHex()}")
    val length = stream.read()

    val value = ByteArray(length)
    if (stream.available() < length) throw IllegalArgumentException("Unexpected end of stream, expected ${length - stream.available()} more byte(s)")
    stream.read(value)

    return Tag(tagDef, length, value)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun buildNode(tag: Tag, parentTag: Tag? = null): Node {
    if (tag.template() != null && !tag.template()?.tag.contentEquals(parentTag?.tag())) {
        if (parentTag == null) {
            throw IllegalArgumentException("Tag ${tag.definition.tag.prettyTagNameHex()} expected to be nested under ${tag.template()?.tag?.prettyTagNameHex()}, but was at the root level")
        }
        throw IllegalArgumentException("Tag ${tag.definition.tag.prettyTagNameHex()} expected to be nested under ${tag.template()?.tag?.prettyTagNameHex()}, but was nested under ${parentTag.tag()?.prettyTagNameHex()}")
    }

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
fun UByteArray.prettyTagNameHex(): String {
    val hex =  this.joinToString(" ") { it.toString(16).uppercase().padStart(2, '0') }
    val tagName = TagDefinitions.entryName(this)
    if (tagName == null) {
        return hex
    } else {
        return "$tagName ($hex)"
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.prettyHex(): String {
    val hex =  this.joinToString(" ") { it.toString(16).uppercase().padStart(2, '0') }
    val tagName = TagDefinitions.entryName(this)
    return "${if (tagName != null) "$tagName " else ""}$hex"
}