package xyz.cofe.jtfm

/**
 * Фильтр навигации по дереву
 */
trait NavigateFilter[-N] {
  def test(n:N):Boolean = true
}

object NavigateFilter {
  /**
   * Фильтр всегда возвращающий true
   */
  implicit val any : NavigateFilter[Any] = new NavigateFilter[Any] {
    override def test(n: Any): Boolean = true
  }

  /** Создание фильтра */
  def create[N]( f:N=>Boolean ):NavigateFilter[N] = new {
    override def test(n:N) = f(n)
  }
}
