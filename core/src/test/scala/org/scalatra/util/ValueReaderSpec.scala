package org.scalatra
package util

import conversion.DefaultImplicitConversions
import org.specs2.mutable.Specification


class ValueReaderSpec extends Specification with DefaultImplicitConversions {

  val params = Map(
    "hello" -> "world",
    "int" -> "1",
    "long" -> "1239485775745309200",
    "date" -> "29/01/2012",
    "bool" -> "true")

  val multiParams = Map(
    "hello" -> Seq("world"),
    "int" -> Seq("1"),
    "long" -> Seq("1239485775745309200"),
    "date" -> Seq("29/01/2012"),
    "bool" -> Seq("true"),
    "stringlist" -> Seq("hello", "world"),
    "intlist" -> Seq("1", "2"),
    "longlist" -> Seq("1239485775745309200", "1239485775745309299"),
    "booleanlist" -> Seq("false", "true")
  )
  "A StringMapValueReader" should {
    "read a string value" in {
      new StringMapValueReader(params).read("hello") must beSome("world")
    }
    "read an int value" in {
      new StringMapValueReader(params).read("int") must beSome("1")
    }
    "read a long value" in {
      new StringMapValueReader(params).read("long") must beSome("1239485775745309200")
    }
    "read a boolean value" in {
      new StringMapValueReader(params).read("bool") must beSome("true")
    }
  }
  "A MultiParamsValueReader" should {
    "read a string value" in {
      new MultiParamsValueReader(multiParams).read("hello") must beSome(Seq("world"))
    }
    "read an int value" in {
      new MultiParamsValueReader(multiParams).read("int") must beSome(Seq("1"))
    }
    "read a long value" in {
      new MultiParamsValueReader(multiParams).read("long") must beSome(Seq("1239485775745309200"))
    }
    "read a boolean value" in {
      new MultiParamsValueReader(multiParams).read("bool") must beSome(Seq("true"))
    }
    "read a stringlist value" in {
      new MultiParamsValueReader(multiParams).read("stringlist") must beSome(Seq("hello", "world"))
    }
    "read an intlist value" in {
      new MultiParamsValueReader(multiParams).read("intlist") must beSome(Seq("1", "2"))
    }
    "read a longlist value" in {
      new MultiParamsValueReader(multiParams).read("longlist") must beSome(Seq("1239485775745309200", "1239485775745309299"))
    }
    "read a booleanlist value" in {
      new MultiParamsValueReader(multiParams).read("booleanlist") must beSome(Seq("false", "true"))
    }
  }
}
