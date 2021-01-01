
package com.github.rubyu.ebquery

import io.github.eb4j.{Book, EntryEnumerator}
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

    if (option.ebMap == null) {
      printError(System.err, "option '--ebmap' is required")
      System.exit(1)
    }

    printResult(System.out, option.dir, option.ebMap)
    System.exit(0)
  }

  def printUsage(stream: PrintStream, parser: CmdLineParser) {
    stream.println(List(
      "NAME",
      " eb2tsv -- Dump Tool for EPWING(Electronic Publishing-WING) Dictionary",
      "",
      "SYNOPSIS",
      " java -jar eb2tsv.jar [-h | --help]",
      " java -jar eb2tsv.jar --ebmap map_file -d dictionary_dir",
      "",
      "DESCRIPTION",
      "Dump all entries in a EPWING dictionary, and prints results to standard output. " +
        "The options are as follows:",
      "",
      " -d dir       : Path to the directory containing Epwing's CATALOGS file.",
      " --ebmap file : Path to the EBWin's GAIJI mapping file.",
      " -h (--help)  : Print help.").mkString(System.lineSeparator))
  }

  def printError(stream: PrintStream, text: String) {
    stream.println(text)
  }

  def printResult(stream: PrintStream, dir: String, map: String) {
    val subBook = new Book(dir).getSubBook(0)
    val mapper = new ExternalCharacterMapper(map)
    val proc = new EBProcessor
    proc.newline = new EBProcessorImpl.text.Newline
    proc.externalCharacter = new EBProcessorImpl.text.ReplacementCharacter
    proc.text = new EBProcessorImpl.text.Text

    val hook = new EBProcessorAdapter(subBook, mapper, proc)
    val exporter = new TSVExporter(new PrintWriter(stream))

    val enumerator = EntryEnumerator.Create(subBook, hook, exporter)
    enumerator.getNextResult();
  }
}