package co.couldbe.demo.model

import co.couldbe.demo.prettyHex
import co.couldbe.demo.tags.TagDefinition

sealed class Node {
    abstract override fun toString(): String

    @OptIn(ExperimentalUnsignedTypes::class)
    data class Primitive(val tag: TagDefinition, val value: ByteArray) : Node() {
        override fun toString(): String = "Primitive(tag=$tag, value=${value.toUByteArray().prettyHex()})"
    }

    data class Constructed(val tag: TagDefinition, val children: List<Node>) : Node() {
        override fun toString(): String = "Constructed(tag=$tag, children=$children)"
    }
}
