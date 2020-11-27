package org.specs2.matcher

import java.util.regex.{Matcher => _, MatchResult =>_, Pattern}
import org.specs2.text.Quote._
import org.specs2.control.Exceptions._
import org.specs2.text.Regexes._
import util.matching.Regex
import StringMatchers._

/**
 * The `StringMatchers` trait provides matchers which are applicable to String objects
 */
trait StringMatchers:

  /** adapt the BeEqualTo matcher to provide ignoreCase and ignoreSpace matcher */
  extension (m: AdaptableMatcher[Any]):

    def ignoreCase: AdaptableMatcher[Any] =
      m.^^^((s: Any) => s.toString.toLowerCase, ignoringCase, ignoringCase)

    def ignoreSpace: AdaptableMatcher[Any] =
      m.^^^((s: Any) => s.toString.replaceAll("\\s", ""), ignoringSpace, ignoringSpace)

    def trimmed: AdaptableMatcher[Any] =
      m.^^^((s: Any) => s.toString.trim, isTrimmed, isTrimmed)

  private[specs2] val ignoringCase = (_:Any).toString + ", ignoring case"
  private[specs2] val ignoringSpace = (_:Any).toString + ", ignoring space"
  private[specs2] val isTrimmed = (_:Any).toString + ", trimmed"

  /** matches if a.toLowerCase.trim = b.toLowerCase.trim */
  def ==/(s: String): Matcher[String] =
    be_==/(s)

  /** matches if a.toLowerCase.trim = b.toLowerCase.trim */
  def be_==/(a: String): Matcher[String] =
    new BeEqualTo(a).ignoreCase.ignoreSpace

  /** matches if a.toLowerCase.trim != b.toLowerCase.trim */
  def be_!=/(a: String): Matcher[String] =
    be_==/(a).not

  /** matches if a.toLowerCase.trim != b.toLowerCase.trim */
  def !=/(s: String): Matcher[String] =
    be_!=/(s)

  /** matches if (b contains a) */
  def contain(t: String): Matcher[String] =
    new Matcher[String]:
      def apply[S <: String](b: Expectable[S]) =
        val a = t
        result(a != null && b.value != null && b.value.contains(a),
               b.description + " contains " + q(a),
               b.description + " doesn't contain " + q(a), b)

  /** matches if (b contains a) */
  def contain(t: Char): Matcher[String] =
    new Matcher[String]:
      def apply[S <: String](b: Expectable[S]) =
        val a = t
        result(b.value != null && b.value.contains(a),
               b.description + " contains " + q(a),
               b.description + " doesn't contain " + q(a), b)

  /** matches if b matches the regular expression a */
  def beMatching(a: =>String): Matcher[String] =
    new BeMatching(a)

  /** alias to use with contain */
  def matching(a: =>String): Matcher[String] =
    new BeMatching(a)

  /** matches if b matches the pattern a */
  def beMatching(a: Pattern): Matcher[String] =
    new BeMatchingPattern(a)

  /** alias to use with contain */
  def matching(a: Pattern): Matcher[String] =
    new BeMatchingPattern(a)

  /** matches if b matches the regex a */
  def beMatching(a: Regex): Matcher[String] =
    new BeMatchingRegex(a)

  /** alias to use with contain */
  def matching(a: Regex): Matcher[String] =
    new BeMatchingRegex(a)

  /** alias for beMatching but matching just a fragment of the string */
  def =~(t: =>String): Matcher[String] =
    BeMatching.withPart(t)

  /** alias for beMatching but matching just a fragment of the string */
  def =~(p: Pattern): BeMatchingPattern =
    new BeMatchingPattern(Pattern.compile(p.toString.regexPart, p.flags()))

  /** alias for beMatching but matching just a fragment of the string */
  def =~(r: Regex): BeMatchingRegex =
    new BeMatchingRegex(r.toString.regexPart.r)

  /** matches if b.startsWith(a) */
  def startWith(a: String): Matcher[String] =
    new Matcher[String]:
      def apply[S <: String](b: Expectable[S]) =
        result(b.value != null && a != null && b.value.startsWith(a),
               s"${b.description} starts with ${q(a)}",
               s"${b.description} doesn't start with ${q(a)}", b)

  /** matches if b.endsWith(a) */
  def endWith(t: =>String): Matcher[String] =
    new Matcher[String]:
      def apply[S <: String](b: Expectable[S]) =
        val a = t
        result(b.value!= null && a!= null && b.value.endsWith(a),
               b.description  + " ends with " + q(a),
               b.description  + " doesn't end with " + q(a), b)
  /** matches if the regexp a is found inside b */
  def find(a: =>String): FindMatcher =
    new FindMatcher(a)

  /** matches if the pattern p is found inside b */
  def find(p: Pattern): FindMatcherPattern =
    new FindMatcherPattern(p)

  /** matches if the regexp r is found inside b */
  def find(r: Regex): FindMatcherRegex =
    new FindMatcherRegex(r)

  /**
   * Matcher to find if the regexp a is found inside b.
   * This matcher can be specialized to a FindMatcherWithGroups which will also check the found groups
   */
  class FindMatcher(t: =>String) extends Matcher[String]:
    lazy val pattern = Pattern.compile(t)

    def withGroup(group: String) = new FindMatcherWithGroups(t, group)
    def withGroups(groups: String*) = new FindMatcherWithGroups(t, groups:_*)
    def apply[S <: String](b: Expectable[S]) =
      val a = t
      result(a != null && b.value != null && pattern.matcher(b.value).find,
             q(a) + " is found in " + b.description,
             q(a) + " isn't found in " + b.description, b)

  /**
   * Matcher to find if the pattern p is found inside b.
   */
  class FindMatcherPattern(p: Pattern) extends FindMatcher(p.toString):
    override lazy val pattern = p
    override def withGroup(group: String) = new FindMatcherPatternWithGroups(p, group)
    override def withGroups(groups: String*) = new FindMatcherPatternWithGroups(p, groups:_*)
  /**
   * Matcher to find if the Regex r is found inside b.
   */
  class FindMatcherRegex(r: Regex) extends FindMatcherPattern(r.pattern)

  /**
   * Matcher to find if the regexp a is found inside b.
   * This matcher checks if the found groups are really the ones expected
   */
  class FindMatcherWithGroups(t: =>String, groups: String*) extends Matcher[String]:
    lazy val pattern = Pattern.compile(t)

    def found(b: String) =
      val matcher = pattern.matcher(b)
      val groupsFound = new scala.collection.mutable.ListBuffer[String]()
      (1 to matcher.groupCount).foreach { i =>
        matcher.reset()
        while matcher.find do { groupsFound += matcher.group(i) }
      }
      groupsFound.toList
    def apply[S <: String](b: Expectable[S]) =
      val a = t
      val groupsFound = found(b.value)
      val withGroups = if groups.size > 1 then " with groups " else " with group "
      def foundText =
        if groupsFound.isEmpty then
          ". Found nothing"
        else
           ". Found: " + q(groupsFound.mkString(", "))
      val groupsToFind = if groups == null then Nil else groups.toList
      result(a != null && b.value != null && groupsFound == groupsToFind,
             q(a) + " is found in " + b.description  + withGroups + q(groupsToFind.mkString(", ")),
             q(a) + " isn't found in " + b.description  + withGroups + q(groupsToFind.mkString(", ")) + foundText, b)
  /**
   * Matcher to find if the pattern p is found inside b.
   */
  class FindMatcherPatternWithGroups(p: Pattern, groups: String*) extends FindMatcherWithGroups(p.toString, groups:_*):
    override lazy val pattern = p

object StringMatchers extends StringMatchers

protected[specs2]
class BeMatching(t: =>String) extends Matcher[String]:
  lazy val pattern = Pattern.compile(t)

  def apply[S <: String](b: Expectable[S]) =
    val a = t
    result(tryOrElse(pattern.matcher(b.value).matches)(false),
           s"${b.description} matches ${q(a)}",
           s"'${b.description}' doesn't match ${q(a)}", b)

object BeMatching:
  def withPart(expression: String): BeMatching =
    new BeMatching(expression.regexPart)

protected[specs2]
class BeMatchingPattern(p: Pattern) extends BeMatching(p.toString):
  override lazy val pattern = p

class BeMatchingRegex(r: Regex) extends BeMatching(r.toString):
    override lazy val pattern = r.pattern
