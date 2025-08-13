Welcome to the Kotlin LTV Parser.

Length Tag Value parsing is a simple and effective way to send binary data over-the-wire. It prevents unnecssary look-ahead or reading without knoweldge, and allows the parser to know exactly what it's looking for.

Progress:
- [x] project created. Aug 12th.
- [x] sample binary TLV file created. Aug 12th.
- [x] inline binary TLV data created. Aug 12th.
- [x] sketched out tag reading, incl. nesting. Aug 12th.

3 step Plan:
- create a function that takes a binary stream and finds the first tag
How to know if the first tag is one byte or two??
- read the length
- Read the value

Requirements:
- nesting *is* supported/expected

Assumptions:
- the tag is a consistent length of 1 byte. IRL they can be longer!
- Bytes are little endian. Why assume this? Because Apple Silicon is little endian.
- We will be working with an EMV-TLV. This is payments related and as such the closet thing I could imagine being relelvant to TLV within Apple Pay that I could find on the internet.
- tags can ONLY appear on the template they are assigned, if they are assigned one. Otherwise they can only appear on the top level (?)
- length definition itself is never longer than a byte


## AI
- I was getting an unknown tag error for a byte that wasn't in the stream. AI explained that 0xA5 was being printed as -5B because it was a signed byte. I then switched all my Bytes to Unsigned Bytes and the error message printed correctly.
- 
## Sample EMV-TLV string
6F1A840E315041592E5359532E4444463031A5088801025F2D02656E
6F 1A 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 08 88 01 02 5F 2D 02 65 6E
decoded: https://emvlab.org/tlvutils/?data=6F1A840E315041592E5359532E4444463031A5088801025F2D02656E

First Tag: 6F

## tags:
Based on the EMV standard(?), tags can be either primitives or constructed. Constructed tags have nested tags within, and, it appears, no simple data on them. 

## Additional Information:
- A fantastic explainer on EMV-TLV https://skryvets.com/blog/2020/05/02/what-are-emv-tags/
- Wikipedia Entry on TLV
https://en.wikipedia.org/wiki/Type–length–value
- interesting article on Unicode and Cahracter Sets
- https://www.joelonsoftware.com/2003/10/08/the-absolute-minimum-every-software-developer-absolutely-positively-must-know-about-unicode-and-character-sets-no-excuses/
- using BinEd plugin in IntelliJ to edit and view sample TLV

## Working Questions
- Are length values in the binary format encoded in binary or hex?
- Does apple pay use TLV? If so, big endian or little endian data?
- Should I use a file, or simply create a function that takes a binary stream? Maybe the latter.
