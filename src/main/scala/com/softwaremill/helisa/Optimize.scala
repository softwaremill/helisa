package com.softwaremill.helisa

import io.{jenetics => j}

object Optimize {
  type Optimize = j.Optimize

  val Maximum = j.Optimize.MAXIMUM
  val Minimum = j.Optimize.MINIMUM
}
