package io.isomarcte.errors4s.core.syntax

import io.isomarcte.errors4s.core._
import scala.language.future
import scala.quoted.*
import scala.compiletime.*

private[core] trait NonEmptyStringSyntax{
  import NonEmptyStringSyntax.*

  extension (inline sc: StringContext) {
    inline def nes(inline args: Any*): NonEmptyString =
      inline if(isAtLeastOneNonEmptyString(sc, args)) {
        NonEmptyString.from(sc.s(args*)).getOrElse(throw new AssertionError("Error during macro expansion of StringContext `nes`. This is an errors4s-core bug. Please report it."))
      } else {
        error("At least one member of a NonEmptyString must be a literal with a non-zero toString representation.")
      }
  }
}

private object NonEmptyStringSyntax {

  private inline def isAtLeastOneNonEmptyString(inline sc: StringContext, inline args: Seq[Any]): Boolean =
    ${ isNonEmptyLiteralString('sc, 'args) }

  private def isNonEmptyLiteralString(partsExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using q: Quotes): Expr[Boolean] = {
    val scHasNonEmptyPart: Boolean =
      partsExpr.value.fold(
        false
      )(
        _.parts.exists(_.size > 0)
      )
    if(scHasNonEmptyPart) {
      Expr(true)
    } else {
      argsExpr match {
        case Varargs(argExprs) =>
          Expr(argExprs.exists{
            case '{ $x: String } =>
              x.value.fold(
                false
              )(_.size > 0)
            case '{ $x: Long } =>
              x.value.nonEmpty
            case '{ $x: Int } =>
              x.value.nonEmpty
            case '{ $x: Short } =>
              x.value.nonEmpty
            case '{ $x: Byte } =>
              x.value.nonEmpty
            case '{ $x: Double } =>
              x.value.nonEmpty
            case '{ $x: Float } =>
              x.value.nonEmpty
            case '{ $x: Boolean } =>
              x.value.nonEmpty
            case '{ $x: Char } =>
              x.value.nonEmpty
            case _ => false
          })
        case _ =>
          Expr(false)
      }
    }
  }
}
