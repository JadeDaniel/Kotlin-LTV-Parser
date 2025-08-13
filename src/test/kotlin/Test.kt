import co.couldbe.demo.buildNode
import co.couldbe.demo.getSampleTlv
import co.couldbe.demo.model.Node
import co.couldbe.demo.model.Node.Primitive
import co.couldbe.demo.parseTags
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class Test {
    @Test
    fun defaultHappyPath() {
        val bytes = getSampleTlv()
        val nodes = parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
        assertEquals(1, nodes.size)
    }

    @Test
    fun multipleConsecutiveTagsParse() {
        val bytes =
            byteArrayOf(
                // tag 6F
                0x6F.toByte(),
                0x04.toByte(),
                // tag 84
                0x84.toByte(),
                0x02.toByte(),
                0x01.toByte(),
                0x02.toByte(),
                // tag 6F
                0x6F.toByte(),
                0x04.toByte(),
                // tag 84
                0x84.toByte(),
                0x02.toByte(),
                0x03.toByte(),
                0x04.toByte(),
            )
        val nodes = parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
        assertEquals(2, nodes.size)
    }

    @Test
    fun singleEmptyConstructedTagParses() {
        val bytes = byteArrayOf(0x6F.toByte(), 0x00.toByte())
        val nodes = parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
        assertEquals(1, nodes.size)
        assertIs<Node.Constructed>(nodes.first())
        with(nodes.first() as Node.Constructed) {
            assertEquals(0, children.size)
        }
    }

    @Test
    fun singlePrimitiveTagWithValueParses() {
        val bytes = byteArrayOf(0x6F.toByte(), 0x04.toByte(), 0x84.toByte(), 0x02.toByte(), 0x01.toByte(), 0x02.toByte())
        val nodes = parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
        assertEquals(1, nodes.size)
        val node = (nodes.first() as Node.Constructed).children.first()
        assertIs<Primitive>(node)
        with(node) {
            assertContentEquals(byteArrayOf(0x01.toByte(), 0x02.toByte()), value)
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun twoByteTagParses() {
        // 5F2D length 1 value 17
        val bytes =
            byteArrayOf(
                // tag 6F
                0x6F.toByte(),
                0x06.toByte(),
                // tag A5
                0xA5.toByte(),
                0x04.toByte(),
                // tag 5F2D
                0x5F.toByte(),
                0x2D.toByte(),
                0x01.toByte(),
                0x11.toByte(),
            )
        val nodes = parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
        assertEquals(1, nodes.size)
        val node =
            (nodes.first() as Node.Constructed)
                .children
                .first()
                .let { it as Node.Constructed }
                .children
                .first()
        assertIs<Primitive>(node)
        with(node) {
            assertContentEquals(byteArrayOf(0x11.toByte()), value)
        }
    }

    @Test
    fun exceptionIsThrownIfThereAreNotEnoughBytesToSatisfyLengthValue() {
        val bytes = byteArrayOf(0x88.toByte(), 0x02.toByte(), 0x01.toByte())
        val exception =
            assertFailsWith<IllegalArgumentException> {
                parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
            }
        assertContains(exception.message!!, "expected 1 more byte")
    }

    @Test
    fun exceptionIsThrownIfTagIsNotFollowedByALengthValue() {
        val bytes = byteArrayOf(0x88.toByte())
        val exception =
            assertFailsWith<IllegalArgumentException> {
                parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
            }
        assertContains(exception.message!!, "no length value found following tag ShortFileIdentifier (88)")
    }

    @Test
    fun exceptionThrownForAnUnknownTag() {
        val bytes = byteArrayOf(0x6F.toByte(), 0x01.toByte(), 0x89.toByte())
        val exception =
            assertFailsWith<IllegalArgumentException> {
                parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
            }
        assertContains(exception.message!!, "Unknown tag 89")
    }

    @Test
    fun exceptionThrownIfNestedTagIsNotNested() {
        val bytes = byteArrayOf(0x88.toByte(), 0x02.toByte(), 0x01.toByte(), 0x02.toByte())
        val exception =
            assertFailsWith<IllegalArgumentException> {
                parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
            }
        assertContains(
            exception.message!!,
            "Tag ShortFileIdentifier (88) expected to be nested under FCIProprietaryTemplate (A5), but was at the root level",
        )
    }

    @Test
    fun exceptionThrownIfTagIsNotNestedUnderProperTag() {
        val bytes = byteArrayOf(0x6F.toByte(), 0x04.toByte(), 0x88.toByte(), 0x02.toByte(), 0x01.toByte(), 0x02.toByte())
        val exception =
            assertFailsWith<IllegalArgumentException> {
                parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
            }
        assertContains(
            exception.message!!,
            "Tag ShortFileIdentifier (88) expected to be nested under FCIProprietaryTemplate (A5), but was nested under FCITemplate (6F)",
        )
    }

    @Test
    fun consecutiveTagsAreCorrectlyOrderedAndNestedTagsHaveTheCorrectHierarchy() {
        // 6F, with a nested 84, A5 (with a nested 88), and another 84
        val bytes =
            byteArrayOf(
                // tag 6F
                0x6F.toByte(),
                0x0E.toByte(),
                // tag 84
                0x84.toByte(),
                0x02.toByte(),
                0x01.toByte(),
                0x02.toByte(),
                // tag A5
                0xA5.toByte(),
                0x04.toByte(),
                // tag 88
                0x88.toByte(),
                0x02.toByte(),
                0x03.toByte(),
                0x04.toByte(),
                // tag 84
                0x84.toByte(),
                0x02.toByte(),
                0x05.toByte(),
                0x06.toByte(),
            )
        val nodes = parseTags(ByteArrayInputStream(bytes)).map { tag -> buildNode(tag) }.toList()
        assertEquals(1, nodes.size)

        val node6F = (nodes.first() as Node.Constructed)
        assertIs<Node.Constructed>(node6F)
        assertEquals(3, node6F.children.size)
        with(node6F) {
            val first84Node = children.first()
            assertIs<Primitive>(first84Node)
            assertContentEquals(byteArrayOf(0x01.toByte(), 0x02.toByte()), first84Node.value)

            val nodeA5 = children[1]
            assertIs<Node.Constructed>(nodeA5)

            val node88 = nodeA5.children.first()
            assertIs<Primitive>(node88)
            assertContentEquals(byteArrayOf(0x03.toByte(), 0x04.toByte()), node88.value)

            val second84Node = children.last()
            assertIs<Primitive>(second84Node)
            assertContentEquals(byteArrayOf(0x05.toByte(), 0x06.toByte()), second84Node.value)
        }
    }
}
