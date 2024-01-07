// Main Part 2 about Evil Wordle
//===============================

object M2 {

  import io.Source

  import scala.util.*

  // ADD YOUR CODE BELOW
  // ======================

  // (1)
  def get_wordle_list(url: String): List[String] =
    try {
      val response = io.Source.fromURL(url)
      val words    = response.getLines().toList
      response.close()
      words
    } catch {
      case e: Exception =>
        Nil
    }

  // val secrets = get_wordle_list("https://nms.kcl.ac.uk/christian.urban/wordle.txt")
  // secrets.length                // => 12972
  // secrets.filter(_.length != 5) // => Nil

  // (2)
  def removeN[A](xs: List[A], elem: A, n: Int): List[A] =
    n match {
      case 0 =>
        xs
      case _ =>
        val i = xs.indexOf(elem)
        i match {
          case -1 =>
            xs
          case _ =>
            val (first, last) = xs.splitAt(i)
            removeN(first ++ last.tail, elem, n - 1)
        }
    }

  // removeN(List(1,2,3,2,1), 3, 1)  // => List(1, 2, 2, 1)
  // removeN(List(1,2,3,2,1), 2, 1)  // => List(1, 3, 2, 1)
  // removeN(List(1,2,3,2,1), 1, 1)  // => List(2, 3, 2, 1)
  // removeN(List(1,2,3,2,1), 0, 2)  // => List(1, 2, 3, 2, 1)

  // (3)
  abstract class Tip
  case object Absent  extends Tip
  case object Present extends Tip
  case object Correct extends Tip

  def pool(secret: String, word: String): List[Char] = secret
    .indices
    .toList
    .filter(characterIDX => secret(characterIDX) != word(characterIDX))
    .map(characterIDX => secret(characterIDX))

  def rec_aux(secret: List[Char], word: List[Char], pool: List[Char], idx: Int = 0): List[Tip] =
    if idx == secret.length then
      Nil
    else if secret(idx) == word(idx) then
      Correct :: rec_aux(secret, word, pool, idx + 1)
    else if pool.contains(word(idx)) then {
      val (first, last) = pool.splitAt(pool.indexOf(word(idx)))
      Present :: rec_aux(secret, word, first ++ last.tail, idx + 1)
    } else
      Absent :: rec_aux(secret, word, pool, idx + 1)

  def aux(secret: List[Char], word: List[Char], pool: List[Char]): List[Tip] = rec_aux(secret, word, pool)

  def score(secret: String, word: String): List[Tip] = aux(secret.toList, word.toList, pool(secret, word))

//   score("chess", "caves") // => List(Correct, Absent, Absent, Present, Correct)
//   score("doses", "slide") // => List(Present, Absent, Absent, Present, Present)
//   score("chess", "swiss") // => List(Absent, Absent, Absent, Correct, Correct)
//   score("chess", "eexss") // => List(Present, Absent, Absent, Correct, Correct)

  // (4)
  def eval(t: Tip): Int =
    t match {
      case Correct =>
        10
      case Present =>
        1
      case Absent =>
        0
    }

  def iscore(secret: String, word: String): Int = score(secret, word).map(eval).sum

  // iscore("chess", "caves") // => 21
  // iscore("chess", "swiss") // => 20

  // (5)
  def rec_lowest(
      secrets: List[String],
      word: String,
      idx: Int = 0,
      current: Int,
      lstFinal: List[String],
  ): List[String] =
    if secrets.length == idx then
      lstFinal
    else {
      if iscore(secrets(idx), word) < current then
        rec_lowest(secrets, word, idx + 1, iscore(secrets(idx), word), secrets(idx) :: Nil)
      else if iscore(secrets(idx), word) == current then
        rec_lowest(secrets, word, idx + 1, current, lstFinal :+ secrets(idx))
      else
        rec_lowest(secrets, word, idx + 1, current, lstFinal)
    }

  def lowest(secrets: List[String], word: String, current: Int, acc: List[String]): List[String] = rec_lowest(
    secrets,
    word,
    0,
    current,
    acc,
  )

  def evil(secrets: List[String], word: String): List[String] = lowest(secrets, word, Int.MaxValue, Nil)

  // evil(secrets, "stent").length
  // evil(secrets, "hexes").length
  // evil(secrets, "horse").length
  // evil(secrets, "hoise").length
  // evil(secrets, "house").length

  // (6)
  def frequencies(secrets: List[String]): Map[Char, Double] = {
    val sorted = secrets.flatten.groupBy(value => value)
    sorted.map((v1, v2) => (v1, 1 - v2.length.toDouble / secrets.flatten.length))
  }

  // (7)
  def rank(frqs: Map[Char, Double], s: String): Double = s.map(frqs).sum

  def ranked_evil(secrets: List[String], word: String): List[String] =
    val _evil            = evil(secrets, word)
    val _frequencies     = frequencies(secrets)
    val eval_by_rank     = _evil.map(x => (x, rank(_frequencies, x)))
    val max_eval_by_rank = eval_by_rank.maxBy(_._2)._2

    eval_by_rank.filter((_, v) => v == max_eval_by_rank).map((x, _) => x)

}
