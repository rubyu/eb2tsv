
package com.github.rubyu.ebquery

import io.github.eb4j.Book
import org.kohsuke.args4j.CmdLineParser
import scala.collection.JavaConversions._
import scala.util.control.Exception._
import java.io._


object Main {
  def main(args: Array[String]) {
    val option = new CliOption
    val parser = new CmdLineParser(option)
    allCatch either parser.parseArgument(args toList)

    if (option.help) {
      printUsage(System.out, parser)
      System.exit(0)
    }

    if (option.dir == null) {
      printError(System.err, "option '-d' is required")
      System.exit(1)
    }

    val proc = allCatch opt option.format.toLowerCase getOrElse("text") match {
      case "text" => getProcessorForText(option.modules)
      case "html" => getProcessorForHtml(option.modules)
      case x: String => {
        printError(System.err, "'%s' is invalid format".format(x))
        System.exit(1)
        return //without this, compiler doesn't understand this match's type
      }
    }

    val words = getInputWords(option.words)
    if (words.isEmpty) {
      printError(System.err, "'words' or STDIN are required")
      System.exit(1)
    }

    val mapper = Option(option.ebMap) match {
      case Some(x) => new ExternalCharacterMapper(x)
      case None => new ExternalCharacterMapper
    }

    printResult(System.out, option.dir, words, mapper, proc)
    System.exit(0)
  }

  def getProcessorForText(modules: String) = {
    val proc = new EBProcessor
    for (m <- allCatch opt modules.toLowerCase getOrElse("tx,ec,ls") split(",")) {
      m match {
        case "ls" => proc.newline = new EBProcessorImpl.text.Newline
        case "ec" => proc.externalCharacter = new EBProcessorImpl.text.ReplacementCharacter
        case "tx" => proc.text = new EBProcessorImpl.text.Text
        case _ => {}
      }
    }
    proc
  }

  def getProcessorForHtml(modules: String) = {
    val proc = new EBProcessor
    for (m <- allCatch opt modules.toLowerCase getOrElse("tx,ec,ls") split(",")) {
      m match {
        case "en" => proc.entry = new EBProcessorImpl.html.Entry
        case "sb" => proc.subscript = new EBProcessorImpl.html.Subscript
        case "sp" => proc.superscript = new EBProcessorImpl.html.Superscript
        case "in" => proc.indent = new EBProcessorImpl.html.Indent
        case "nb" => proc.noNewline = new EBProcessorImpl.html.NoNewline
        case "em" => proc.emphasis = new EBProcessorImpl.html.Emphasis
        case "dc" => proc.decoration = new EBProcessorImpl.html.Decoration
        case "kw" => proc.keyword = new EBProcessorImpl.html.Keyword
        case "ls" => proc.newline = new EBProcessorImpl.html.Newline
        case "ec" => proc.externalCharacter = new EBProcessorImpl.html.ExternalCharacter
        case "mg" => proc.monoGraphic = new EBProcessorImpl.html.MonoGraphic
        case "cg" => proc.colorGraphic = new EBProcessorImpl.html.ColorGraphic
        case "sd" => proc.sound = new EBProcessorImpl.html.Sound
        case "tx" => proc.text = new EBProcessorImpl.html.Text
        case _ => {}
      }
    }
    proc
  }


  def getInputWords(words: Array[String]) = Option(words) getOrElse(getWordsFromSTDIN()) toList

  def getWordsFromSTDIN(): Array[String] = {
    if (System.in.available() == 0) {
      Array()
    } else {
      val reader = new BufferedReader(new InputStreamReader(System.in)) //デフォルトのエンコーディングを使用する
      try {
        Iterator.continually(reader.readLine()) takeWhile(_ != null) toArray
      } finally {
        reader.close()
      }
    }
  }

  def printUsage(stream: PrintStream, parser: CmdLineParser) {
    stream.println(List(
      "NAME",
      " qbquery -- Command Line Interface for Epwing(Electronic Publishing-WING)",
      "",
      "SYNOPSIS",
      " java -jar ebquery.jar [-h | --help]",
      " java -jar ebquery.jar [-f format] [-m modules] [--ebmap file] -d dir word [word ...]",
      "",
      "DESCRIPTION",
      "Executes a query for an Epwing dictionary, and prints results to standard output. " +
        "The options are as follows:",
      "",
      " -d dir       : Path to the directory containing Epwing's CATALOGS file.",
      " words        : Query words.",
      " -f format    : Output format. The default value is 'text'." +
        "Allowed values are 'text' and 'html'.",
      " -m modules   : Modules used for rendering the output. " +
        "The default value is 'tx,ec,ls'. " +
        "Modules are en, sb, sp, in, nb, em, dc, kw, ls, ec, mg, cg, sd and tx.",
      " --ebmap file : Path to the EBWin's GAIJI mapping file.",
      " -h (--help)  : Print help.",
      "",
      " If no word is specified, then the standard input is read.").mkString(System.lineSeparator))
  }

  def printError(stream: PrintStream, text: String) {
    stream.println(text)
  }

  def printResult(stream: PrintStream, dir: String, words: List[String],
                  mapper: ExternalCharacterMapper, proc: EBProcessor) {
    val dic = new Book(dir).getSubBook(0)
    val hook = new EBProcessorAdapter(dic, mapper, proc)

    for (word <- words) {
      val searcher = dic.searchExactword(word)
      Iterator.continually(searcher.getNextResult()) takeWhile(_ != null) foreach { result =>
        stream.print(result.getText(hook))
      }
    }
  }
}