
package com.github.rubyu.ebquery

import EBProcessor._


object EBProcessorImpl {

  object nop {
    class Text extends EBProcessor.Text {
      def process(c: EBContext, str: String) {}
    }

    class ExternalCharacter extends EBProcessor.ExternalCharacter {
      def process(c: EBContext, _type: ExternalCharacterType, code: Int) {}
    }

    class Subscript extends EBProcessor.Subscript {
      def preProcess(c: EBContext) {}
      def postProcess(c: EBContext) {}
    }

    class Superscript extends EBProcessor.Superscript {
      def preProcess(c: EBContext) {}
      def postProcess(c: EBContext) {}
    }

    class NoNewline extends EBProcessor.NoNewline {
      def preProcess(c: EBContext) {}
      def postProcess(c: EBContext) {}
    }

    class Emphasis extends EBProcessor.Emphasis {
      def preProcess(c: EBContext) {}
      def postProcess(c: EBContext) {}
    }

    class Decoration extends EBProcessor.Decoration {
      def preProcess(c: EBContext, _type: Int) {}
      def postProcess(c: EBContext) {}
    }

    class Keyword extends EBProcessor.Keyword {
      def preProcess(c: EBContext) {}
      def postProcess(c: EBContext) {}
    }

    class Newline extends EBProcessor.Newline {
      def process(c: EBContext) {}
    }

    class Indent extends EBProcessor.Indent {
      def preProcess(c: EBContext) {}
      def postProcess(c: EBContext) {}
    }

    class MonoGraphic extends EBProcessor.MonoGraphic {
      def preProcess(c: EBContext, width: Int, height: Int) {}
      def postProcess(c: EBContext, pos: Long) {}
    }

    class ColorGraphic extends EBProcessor.ColorGraphic {
      def preProcess(c: EBContext, format: Int, pos: Long) {}
      def postProcess(c: EBContext) {}
    }

    class Sound extends EBProcessor.Sound {
      def preProcess(c: EBContext, format: Int, start: Long, end: Long) {}
      def postProcess(c: EBContext) {}
    }

    class Entry extends EBProcessor.Entry {
      def process(c: EBContext) {}
    }
  }

  object text {
    class Text extends EBProcessor.Text {
      def process(c: EBContext, str: String) {
        c.tree.append(str)
      }
    }

    /**
     * unicode 0xFFFD(REPLACEMENT CHARACTER)を、EBProcessor.textに移譲する。
     */
    class ReplacementCharacter extends EBProcessor.ExternalCharacter {
      def process(c: EBContext, _type: ExternalCharacterType, code: Int) { c.proc.text.process(c, "\uFFFD") }
    }

    /**
     * 環境に準じた改行を、EBProcessor.textに移譲する。
     */
    class Newline extends EBProcessor.Newline {
      def process(c: EBContext) { c.proc.text.process(c, System.lineSeparator) }
    }
  }

  object html {
    class Text extends EBProcessor.Text {
      def process(c: EBContext, str: String) { c.tree.append(Util.escape(str)) }
    }

    class ExternalCharacter extends EBProcessor.ExternalCharacter {
      def process(c: EBContext, _type: ExternalCharacterType, code: Int) {
        Util.externalCharacterImage(c.dic, _type, code) match {
          case Some(data) => {
            val tag = new EBLeaf("img")
            tag.property.update("src", "data:image/png;base64," + Util.base64str(data))
            tag.property.update("alt", (_type match {
              case Narrow => "h"
              case Wide => "z"
            }) + code.toHexString.toUpperCase)
            tag.property.update("class", "ebec")
            c.tree.append(tag)
          }
          case None => {}
        }
      }
    }

    class Subscript extends EBProcessor.Subscript {
      def preProcess(c: EBContext) {
        val tag = new EBNode("span")
        tag.property.update("class", "ebsb")
        c.tree.open(tag)
      }
      def postProcess(c: EBContext) { c.tree.close() }
    }

    class Superscript extends EBProcessor.Superscript {
      def preProcess(c: EBContext) {
        val tag = new EBNode("span")
        tag.property.update("class", "ebsp")
        c.tree.open(tag)
      }
      def postProcess(c: EBContext) { c.tree.close() }
    }

    class NoNewline extends EBProcessor.NoNewline {
      def preProcess(c: EBContext) {
        val tag = new EBNode("span")
        tag.property.update("class", "ebnb")
        c.tree.open(tag)
      }
      def postProcess(c: EBContext) { c.tree.close() }
    }

    class Emphasis extends EBProcessor.Emphasis {
      def preProcess(c: EBContext) {
        val tag = new EBNode("span")
        tag.property.update("class", "ebem")
        c.tree.open(tag)
      }
      def postProcess(c: EBContext) { c.tree.close() }
    }

    class Decoration extends EBProcessor.Decoration {
      def preProcess(c: EBContext, _type: Int) {
        val tag = new EBNode("span")
        _type match {
          case 0x01 => tag.property.update("class", "ebit")
          case 0x03 => tag.property.update("class", "ebbo")
          case _ => tag.property.update("class", "ebul")
        }
        c.tree.open(tag)
      }
      def postProcess(c: EBContext) { c.tree.close() }
    }

    class Keyword extends EBProcessor.Keyword {
      def preProcess(c: EBContext) {
        val tag = new EBNode("span")
        tag.property.update("class", "ebkw")
        c.tree.open(tag)
      }
      def postProcess(c: EBContext) { c.tree.close() }
    }

    class Newline extends EBProcessor.Newline {
      def process(c: EBContext) { c.tree.append(new EBLeaf("br")) }
    }

    class Indent extends EBProcessor.Indent {
      def preProcess(c: EBContext) {
        val tag = new EBNode("span")
        tag.property.update("class", "ebin")
        c.tree.open(tag)
      }
      def postProcess(c: EBContext) { c.tree.close() }
    }

    class MonoGraphic extends EBProcessor.MonoGraphic {
      def preProcess(c: EBContext, width: Int, height: Int) {
        //not append but open, because MonoGraphicNode may have children
        c.tree.open(new MonoGraphicNode(width, height))
      }
      def postProcess(c: EBContext, pos: Long) {
        c.tree.current match {
          case x: MonoGraphicNode => {
            Util.monoGraphicImage(c.dic, pos, x.width, x.height) match {
              case Some(data) => {
                x.property.update("alt", pos.toHexString.toUpperCase)
                x.property.update("class", "ebmg")
                x.property.update("src", "data:image/png;base64," + Util.base64str(data))
              }
              case None => {}
            }
          }
          case _ => {}
        }
        c.tree.close()
      }
    }

    class ColorGraphic extends EBProcessor.ColorGraphic {
      def preProcess(c: EBContext, format: Int, pos: Long) {
        //not append but open, because ColorGraphicNode may have children
        c.tree.open(new ColorGraphicNode(format, pos))
      }
      def postProcess(c: EBContext) {
        c.tree.current match {
          case x: ColorGraphicNode => {
            Util.colorGraphicImage(c.dic, x.pos, x.format) match {
              case Some((ext: String, data: Array[Byte])) => {
                x.property.update("alt", x.pos.toHexString.toUpperCase)
                x.property.update("class", "ebcg")
                x.property.update("src", "data:image/%s;base64,%s".format(ext, Util.base64str(data)))
              }
              case None => {}
            }
          }
          case _ => {}
        }
        c.tree.close()
      }
    }

    class Sound extends EBProcessor.Sound {
      def preProcess(c: EBContext, format: Int, start: Long, end: Long) {
        //not append but open, because AudioNode may have children
        c.tree.open(new AudioNode(format, start, end))
      }
      def postProcess(c: EBContext) {
        c.tree.current match {
          case x: AudioNode => {
            Util.soundData(c.dic, x.format, x.start, x.end) match {
              case Some(("wav", data: Array[Byte])) => {
                x.property.update("class", "ebsd")
                x.property.update("controls", "controls")
                x.property.update("src", "data:audio/x-wav;base64,%s".format(Util.base64str(data)))
              }
              case _ => {} //midiはいらない…
            }
          }
          case _ => {}
        }
        c.tree.close()
      }
    }

    class Entry extends EBProcessor.Entry {
      def process(c: EBContext) {
        val tag = new EBNode("span")
        tag.property.update("class", "ebquery %s".format(c.dic.getName))
        c.tree.open(tag)
      }
    }
  }
}