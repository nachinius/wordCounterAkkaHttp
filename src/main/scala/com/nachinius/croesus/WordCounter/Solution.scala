package com.nachinius.croesus.WordCounter

final case class Solution(totalWordCount: Int, dictionaryCount: Map[String, Int]) {
  def countOf(word: String) = dictionaryCount.getOrElse(word, 0)
}

object Solution {

  def apply(dict: Map[String, Int]): Solution = Solution(totalWords(dict), dict)

  def totalWords(dict: Map[String, Int]): Int = {
    dict.aggregate(0)((prevCount, nextKV) => prevCount + nextKV._2, _ + _)
  }
  
  def accumulator(dictCounter: Map[String,Int], next: String): Map[String, Int] = {
    dictCounter.updated(next, 1 + dictCounter.getOrElse(next, 0))
  }
}
