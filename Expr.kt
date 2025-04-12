package datayja.klox

sealed class Expr {
  data class Binary   (
    val Expr: ,
    val operator: Token,
    val right: Expr,
  ) : Expr() {
  }
  data class Grouping (
    val Expr: ,
  ) : Expr() {
  }
  data class Literal  (
    val Any: ,
  ) : Expr() {
  }
  data class Unary    (
    val Token: ,
    val right: Expr,
  ) : Expr() {
  }
}

