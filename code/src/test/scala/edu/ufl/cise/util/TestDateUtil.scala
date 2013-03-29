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
      DateUtil.incDate("2012-05-02")
    }
  }
  
  test("Test date last date of year increment") {
    expect("2013-01-01") {
      DateUtil.incDate("2012-12-31")
    }
  }
  
//  test("Test date to formatted string") {
//    expect("1") {
//      val d = new Date
//      assert()
//      "1"
//    }
//  }
  
  test("Test date proper date parsing and reformatting") {
    expect("2013-01-01") {
      DateUtil.dateStringToString("2013-01-01")
    }
    
    expect("2012-12-31") {
      DateUtil.dateStringToString("2012-12-31")
    }
  }
  

}