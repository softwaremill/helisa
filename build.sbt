name := "helisa"
version := "0.1"

scalaVersion := "2.12.6"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")

scalacOptions += "-Ypartial-unification"

val jeneticsDeps = Seq(
  "jenetics",
  "jenetics.prog",
  "jenetics.ext"
).map("io.jenetics" % _ % "4.1.0")

val coreDeps = Seq("com.chuusai" %% "shapeless" % "2.3.2",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0") ++
  Seq("cats-core", "alleycats-core").map("org.typelevel" %% _ % "1.1.0")

libraryDependencies ++= jeneticsDeps ++ coreDeps
