package com.nachinius.croesus.WordCounter

final case class Solution(totalWordCount: Int, dictionaryCount: Map[String, Int]) {
  
  
    def countOf(word: String) = dictionaryCount.getOrElse(word, 0)
}


object Solution {
  
    def sumUp(dict: Map[String, Int]): Int = {
        dict.aggregate(0)((prevCount, nextKV) => prevCount + nextKV._2, _ + _)
    }
}
