package co.couldbe.demo

import java.io.ByteArrayInputStream
import java.util.Base64

// Press <shortcut actionId="Debug"/> to start debugging your code.
// Press <shortcut actionId="Run"/> to run your code.

fun main() {
    val FCITemplate = TagDefinition(TagType.CONSTRUCTED, 0x6F)
    val DF = TagDefinition(TagType.PRIMITIVE, 0x84.toByte() /*why is to byte needed for 0x84, but not 0x6F? */, FCITemplate)

    val tagDefinitions: Map<Byte, TagDefinition> = mapOf(
        FCITemplate.tag to FCITemplate,
        DF.tag to DF
    )

    val bytestream = getSampleTlv()

    // Tag Stack tuple(int, tag). Int tracks current position in length value. When int > length we SHOULD BE done reading tag

    var contextWindow: ContextWindow? = null

    val contextStack: ArrayDeque<TagParsePointer> = ArrayDeque()

    val currentNode: TLVNode? = null

    ByteArrayInputStream(bytestream).use { stream ->
        while (stream.available() > 0) { // continue reading until stream is exhausted
            val byte = stream.read().toByte() // read first byte
            
            // If context window is empty, look for a new tag
            if (contextWindow == null) {
                val tagDef = tagDefinitions[byte] ?: throw IllegalArgumentException("Unknown tag $byte") // TEST CASE

                // TODO validate that found tag has the appropriate template for the last item on the tag stack, if in a constructed tag
                // ALSO consider that we could keep constructed tags in the window until a new tag is encountered, rather than grabbing the last item on the stack

                if (tagDef.type == TagType.CONSTRUCTED) {
                    // immediately push constructed tags onto the stack
                    contextStack.addLast(TagParsePointer(tagDef))
                    continue
                }
                contextWindow = ContextWindow(tag = tagDef.tag)
            }

            // context window is assumed to no longer be null here, which means we have finally reached
            //  a primitive tag and can read its value
            if (contextWindow.length == null) {
                contextWindow.length = byte.toInt() and 0xFF // TODO why 0xFF??
                continue
            }

            if (contextWindow.value == null) {
                contextWindow.value = ByteArray(contextWindow.length)
                stream.read(contextWindow.value) // TODO check if there's adqueate data OR catach exception and throw helpfully
                // Oh, we don't need to track our position in the value, because we intend to read the full value!
                // At this point, I've read N (length) bytes, and my primitive SHOULD be done.
                contextWindow = null
            }

            // I DO need to track bytes read for the outer layers of the stack, because otherwise I won't know when I'm done with that constructed tag
            // What would I have to change in order to read ALL the bytes for all tag types, incl. constructed, and then to dive in to constructed tags?
        }
    }

    contextStack.forEach { println(it) }
}

fun getSampleTlv(): ByteArray {
    return Base64.getDecoder().decode("bxqEDjFQQVkuU1lTLkRERjAxpQiIAQJfLQJlbg==")
}

enum class TagType {
    PRIMITIVE,
    CONSTRUCTED
}

data class ContextWindow(
    var tag: Byte? = null, // TODO since this is a var, should we use local vars instead?
    var length: Int? = null,
    var value: ByteArray? = null
)

data class TagParsePointer(
    val tag: TagDefinition,
    var currentPosition: Int = 0
)

data class TLVNode (
    val tag: TagDefinition,
    val length: Int,
    val value: ByteArray,
    val parent: TLVNode? = null,

    /* Constucted nodes have children */
    val isConstructed: Boolean = false,
    val children: List<TLVNode> = emptyList(),

    var remainingLength: Int? = null,
) {
    init {
        // Initialize length pointer to full length
        if (remainingLength == null) {
            remainingLength = length
        }
    }
}

data class TagDefinition (
    val type: TagType,
    val tag: Byte,
    /* template is a pointer to a tag that this tag appears on */
    val template: TagDefinition? = null,
) {
    override fun toString(): String {
        return "Tag(type=$type, tag=${tag.pretty()}, template=$template)"
    }
}

fun Byte.pretty(): String = this.toString(16).uppercase()
