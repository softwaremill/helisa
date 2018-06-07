name := "helisa"
version := "0.1"

scalaVersion := "2.12.6"
lazy val akkaVersion = "2.5.12"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")

scalacOptions += "-Ypartial-unification"

val jeneticsDeps = Seq(
  "jenetics",
  "jenetics.prog",
  "jenetics.ext"
).map("io.jenetics" % _ % "4.1.0")

val coreDeps = Seq("com.chuusai" %% "shapeless" % "2.3.2",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion) ++
  Seq("cats-core", "alleycats-core").map("org.typelevel" %% _ % "1.1.0")

val testDeps = Seq("org.scalatest" %% "scalatest" % "3.0.5",
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.6").map(_ % "test")

libraryDependencies ++= jeneticsDeps ++ coreDeps ++ testDeps
