package xyz.cofe.term.ui
package prop.color

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color

implicit def colorProp2Color( prop:Prop[Color] ):Color = prop.get
