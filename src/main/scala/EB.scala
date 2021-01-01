
package com.github.rubyu.ebquery

import io.github.eb4j.SubBook
import io.github.eb4j.hook.HookAdapter
import io.github.eb4j.util.ByteUtil


class EBProcessorAdapter(val dic: SubBook,
                          val mapper: ExternalCharacterMapper,
                          val proc: EBProcessor) extends HookAdapter[String] {

  val context = new EBContext(dic, new EBElemTree, proc)
  var narrow = false
  var indent = 0
  proc.entry.process(context)

  override def clear() {
    context.reset()
    narrow = false
    indent = 0
    proc.entry.process(context)
  }

  override def isMoreInput = true

  override def getObject: String = context.tree.root.toString

  override def append(str: String) {
    proc.text.process(context, if (narrow) ByteUtil.wideToNarrow(str) else str)
  }

  override def beginNarrow() { narrow = true }
  override def endNarrow() { narrow = false }

  def externalCharacterString(code: Int): Option[String] = {
    def mapping =
      if (narrow) mapper.getHalfOption(code)
      else mapper.getFullOption(code)

    mapping match {
      //外字そのものを無効にするため、ダミーの書き込みを行う
      case Some(mapper.NullValue) => Some("")
      //置換文字列の割り当てがない。フォールバックし、画像を出力させる
      case Some(mapper.SkippedValue) => None
      //この文字列に置換する
      case Some(mapper.StringValue(str)) => Some(str)
      //マッピングされていない
      case None => None
    }
  }

  /**
   * マッピングが与えられている場合は、外字を文字として扱う。
   * @param code
   */
  override def append(code: Int) {
    externalCharacterString(code) match {
      case Some(x) => append(x)
      case None => narrow match {
        case true => proc.externalCharacter.process(context, EBProcessor.Narrow, code)
        case false => proc.externalCharacter.process(context, EBProcessor.Wide, code)
      }
    }
  }

  override def beginSubscript() { proc.subscript.preProcess(context) }
  override def endSubscript() { proc.subscript.postProcess(context) }

  override def beginSuperscript() { proc.superscript.preProcess(context) }
  override def endSuperscript() { proc.superscript.postProcess(context) }

  override def setIndent(n: Int) {
    (n - indent) match {
      case x if 0 < x => (0 until x) foreach (_ => proc.indent.preProcess(context))
      case x if 0 > x => (0 until x) foreach (_ => proc.indent.postProcess(context))
      case _ => {}
    }
    indent = n
  }

  override def beginNoNewLine() { proc.noNewline.preProcess(context) }
  override def endNoNewLine() { proc.noNewline.postProcess(context) }

  override def beginEmphasis() { proc.emphasis.preProcess(context) }
  override def endEmphasis() { proc.emphasis.postProcess(context) }

  override def beginDecoration(code: Int) { proc.decoration.preProcess(context, code) }
  override def endDecoration() { proc.decoration.postProcess(context) }

  override def beginKeyword() { proc.keyword.preProcess(context) }
  override def endKeyword() { proc.keyword.postProcess(context) }

  override def newLine() { proc.newline.process(context) }

  override def beginMonoGraphic(width: Int, height: Int) { proc.monoGraphic.preProcess(context, width, height) }
  override def endMonoGraphic(pos: Long) { proc.monoGraphic.postProcess(context, pos) }

  override def beginInlineColorGraphic(format: Int, pos: Long) { proc.colorGraphic.preProcess(context, format, pos) }
  override def endInlineColorGraphic() { proc.colorGraphic.postProcess(context) }

  override def beginColorGraphic(format: Int, pos: Long) { proc.colorGraphic.preProcess(context, format, pos) }
  override def endColorGraphic() { proc.colorGraphic.postProcess(context) }

  override def beginSound(format: Int, start: Long, end: Long) { proc.sound.preProcess(context, format, start, end) }
  override def endSound() { proc.sound.postProcess(context) }
}


class EBContext(val dic: SubBook, val tree: EBElemTree, val proc: EBProcessor) {
  def reset() {
    tree.clear()
  }
}


object EBProcessor {

  trait ExternalCharacterType
  case object Wide extends ExternalCharacterType
  case object Narrow extends ExternalCharacterType

  trait Text {
    def process(c: EBContext, str: String): Unit
  }

  trait ExternalCharacter {
    def process(c: EBContext, _type: ExternalCharacterType, code: Int): Unit
  }

  trait Subscript {
    def preProcess(c: EBContext): Unit
    def postProcess(c: EBContext): Unit
  }

  trait Superscript {
    def preProcess(c: EBContext): Unit
    def postProcess(c: EBContext): Unit
  }

  trait NoNewline {
    def preProcess(c: EBContext): Unit
    def postProcess(c: EBContext): Unit
  }

  trait Emphasis {
    def preProcess(c: EBContext): Unit
    def postProcess(c: EBContext): Unit
  }

  trait Decoration {
    def preProcess(c: EBContext, _type: Int): Unit
    def postProcess(c: EBContext): Unit
  }

  trait Keyword {
    def preProcess(c: EBContext): Unit
    def postProcess(c: EBContext): Unit
  }

  trait Newline {
    def process(c: EBContext): Unit
  }

  trait Indent {
    def preProcess(c: EBContext): Unit
    def postProcess(c: EBContext): Unit
  }

  trait Entry {
    def process(c: EBContext): Unit
  }

  trait MonoGraphic {
    def preProcess(c: EBContext, width: Int, height: Int): Unit
    def postProcess(c: EBContext, pos: Long): Unit
  }

  trait ColorGraphic {
    def preProcess(c: EBContext, format: Int, pos: Long): Unit
    def postProcess(c: EBContext): Unit
  }

  trait Sound {
    def preProcess(c: EBContext, format: Int, start: Long, end: Long): Unit
    def postProcess(c: EBContext): Unit
  }
}

class EBProcessor {
  var text: EBProcessor.Text = new EBProcessorImpl.nop.Text
  var externalCharacter: EBProcessor.ExternalCharacter = new EBProcessorImpl.nop.ExternalCharacter
  var subscript: EBProcessor.Subscript = new EBProcessorImpl.nop.Subscript
  var superscript: EBProcessor.Superscript = new EBProcessorImpl.nop.Superscript
  var noNewline: EBProcessor.NoNewline = new EBProcessorImpl.nop.NoNewline
  var emphasis: EBProcessor.Emphasis = new EBProcessorImpl.nop.Emphasis
  var decoration: EBProcessor.Decoration = new EBProcessorImpl.nop.Decoration
  var keyword: EBProcessor.Keyword = new EBProcessorImpl.nop.Keyword
  var newline: EBProcessor.Newline = new EBProcessorImpl.nop.Newline
  var indent: EBProcessor.Indent = new EBProcessorImpl.nop.Indent
  var monoGraphic: EBProcessor.MonoGraphic = new EBProcessorImpl.nop.MonoGraphic
  var colorGraphic: EBProcessor.ColorGraphic = new EBProcessorImpl.nop.ColorGraphic
  var sound: EBProcessor.Sound = new EBProcessorImpl.nop.Sound
  var entry: EBProcessor.Entry = new EBProcessorImpl.nop.Entry
}
