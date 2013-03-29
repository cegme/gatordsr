package edu.ufl.cise.util

import org.scalatest.FunSuite
//import edu.ufl.cise.util.DateUtil

class TestDateUtil extends FunSuite {

  test("shout says HELLO with hello") {
    expect("HELLO") {
      DateUtil.shoutMyMessage("hello")
    }
  }

}