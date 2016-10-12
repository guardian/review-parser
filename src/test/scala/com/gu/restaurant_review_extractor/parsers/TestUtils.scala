package com.gu.restaurant_review_extractor.parsers

import java.nio.file._

object TestUtils {

  def resourceToString(path: String): String = new String(Files.readAllBytes(Paths.get(getClass.getClassLoader.getResource(path).toURI())))

}
