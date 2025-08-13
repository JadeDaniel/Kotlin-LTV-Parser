package co.couldbe.demo.model

import co.couldbe.demo.tags.TagDefinition

sealed class Node {
    // we may not need to store parent, as the recursion stack will hold parents as function parameters
    data class Primitive(val tag: TagDefinition, val value: ByteArray): Node()
    data class Constructed(val tag: TagDefinition, val children: List<Node>): Node()
}
