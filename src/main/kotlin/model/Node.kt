package co.couldbe.demo.model

import co.couldbe.demo.prettyTagNameHex
import co.couldbe.demo.tags.TagDefinition

@OptIn(ExperimentalUnsignedTypes::class)
sealed class Node {
    abstract val tag: TagDefinition
    abstract override fun toString(): String

    data class Primitive(override val tag: TagDefinition, val value: ByteArray) : Node() {
        override fun toString(): String {
            val attributeString = listOfNotNull(
                "tag=${tag.tag.prettyTagNameHex()}",
                "value=${value.toUByteArray().prettyTagNameHex()}",
                tag.template?.let { "template=${tag.template.tag.prettyTagNameHex()}" }
            ).joinToString()

            return "Primitive($attributeString)"
        }
    }

    data class Constructed(override val tag: TagDefinition, val children: List<Node>) : Node() {
        override fun toString(): String {
            val attributeString = listOfNotNull(
                "tag=${tag.tag.prettyTagNameHex()}",
                tag.template?.let { "template=${tag.template.tag.prettyTagNameHex()}" },
                "children=${children}",
            ).joinToString()

            return "Constructed($attributeString)"
        }
    }
}
