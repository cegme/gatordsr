package edu.ufl.cise.util

import org.scalatest.FunSuite
//import edu.ufl.cise.util.DateUtil

class TestDateUtil extends FunSuite {

  test("shout says HELLO with hello") {
    expect("HELLO") {
      DateUtil.shoutMyMessage("hello")
    }
  }
  
  test("Test simple date increment") {
    expect("2012-05-03") {
      DateUtil.toDate("2012-05-02")
    }
  }
  
  test("Test date last date of year increment") {
    expect("2013-01-01") {
      DateUtil.toDate("2012-12-31")
    }
  }

}