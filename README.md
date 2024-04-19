# Gly2Mdc v.2.0


Version 2.0 of the Gly2Mdc tool converts a .gly file produced with JSesh to 3 different texts:
                
1. cleaned Manuel de Codage encoding

2. stripped Manuel de Codage encoding in Ramses Transliteration Corpus style

3. unicode characters.

Additionally makes a JSON-format text where the signs have been annotated with encoding, Unicode, and Thot Sign List designations


### Gly2mdc2-2.0.jar can be opened by double-clicking.


## **NOTES on the tool:**

Tested only on a Mac

Build with Java JDK 15, but might work with earlier versions as well (not tested).

Some control characters are shown as question marks in the tool but work in the files created (on a Mac the control characters are shown but do not place the signs).

When selecting a file for the first time after launching, the tool opens the home directory of the user. Afterwards, it remembers the previous location.

Long files take time to process and make the tool unusable while this happens. After Manuel de Codage has loaded, it may still take time to process the other formats.

If the Unicode characters are not rendered, install the Aegyptus.otf (src/resources) to you fonts!

## **NOTES on the choices made for converting the encoding:**

Gly2Mdc does NOT support the absolute placing of the signs when converted to Unicode (and JSON). For converting files with manually placed signs for accurate Unicode, HieroJax can be used. Here the signs are simply presented one after another.

Since parentheses indicate croup in JSesh, the white spaces inside them have been changed to * (i.e. next to each other).

In Unicode, shading control characters targets only one character; therefore, the shading marked in JSesh has been converted to target each of the signs in a group. For example, p:n#12 (upper part of group shaded) becomes p#1234:n. Also characters between #b - #e get shaded with #1234.

All the different ways to indicate ligatures after the sign in JSesh (&&& && & _& **) are changed to &.

Uses the ligatures that are specified in the ligatures.txt file in the JSesh source code + D&d.

## **JSON format contains the following**

textName = 1. line starting in JSesh with +

infos 	= all the other lines starting with +

items	= signs

Each sign has (when applicable):

line		= line number

itemNr	= sign position on the line

encoding	= encoding as typed in JSesh

mdc		= Gardiner sign list designation of the sign

Unicode	= Unicode character of the sign if available, mdc otherwise

codepoint	= Unicode character's codepoint

tsl		= link to the sign in Thot Sign List

shading	= YES if the sign is followed by shading that applies to it, NO otherwise

controlCharacter	= YES if the sign is a Unicode control character

rotation	= if the sign has an attribute for rotating it, e.g. R270

size		= if the sign has an attribute that defines its size


## **TO DO:**

Add functionality to INFO button

Test on a Windows computer

Add red colour support

