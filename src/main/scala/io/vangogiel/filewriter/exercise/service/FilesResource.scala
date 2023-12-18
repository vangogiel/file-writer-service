package io.vangogiel.filewriter.exercise.service

import scala.io.Source
import scala.reflect.io.File

class FilesResource {
  val random: Seq[Char] = new scala.util.Random(30).alphanumeric

  def checkFilesExists(fileName: String): Boolean = {
    File(s"/tmp/$fileName").exists
  }

  def readFileContent(fileName: String): String = {
    val bufferedSource = Source.fromFile(s"/tmp/$fileName")
    val fileContents = bufferedSource.getLines().mkString
    bufferedSource.close()
    fileContents
  }

  def writeNewFile(fileName: String): Unit = {
    File(s"/tmp/$fileName").writeAll(random.take(300).mkString)
  }
}
