package xyz.cofe.jtfm

case class Lens[A,B]( get:A=>B, set:(A,B)=>A ):
  def apply(a:A):B = get(a)
  def apply(a:A,b:B) = set(a,b)
  def compose[C](lens:Lens[B,C]):Lens[A,C]=
    Lens(
      get= a => lens.get(get(a)),
      set= (a,c) => set(a, lens.set(get(a), c))
    )
  def +[C](lens:Lens[B,C]):Lens[A,C] = compose(lens)

