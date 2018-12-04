name := "helisa"
version := "0.1"

scalaVersion := "2.12.6"


addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")

scalacOptions ++= Seq("-feature", "-Ypartial-unification")

val jeneticsDeps = Seq(
  "jenetics",
  "jenetics.prog",
  "jenetics.ext"
).map("io.jenetics" % _ % "4.1.0")

val coreDeps = Seq("com.chuusai" %% "shapeless" % "2.3.2",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0") ++
  Seq("cats-core", "alleycats-core").map("org.typelevel" %% _ % "1.1.0")

lazy val akkaVersion = "2.5.17"
val apiDeps = Seq("com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "co.fs2" %% "fs2-core" % "1.0.0").map(_ % Optional)

val testDeps = Seq("org.scalatest" %% "scalatest" % "3.0.5",
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.6").map(_ % "test")

libraryDependencies ++= jeneticsDeps ++ coreDeps ++ apiDeps ++ testDeps


//ScalaDoc
enablePlugins(SiteScaladocPlugin, GhpagesPlugin)

siteSubdirName in SiteScaladoc := "latest/api"
scalacOptions in (Compile,doc) ++= Seq("-groups") ++ Opts.doc.title("Helisa")

scmInfo := Some(ScmInfo(url("https://github.com/softwaremill/helisa"), "https://github.com/softwaremill/helisa.git"))
git.remoteRepo := scmInfo.value.get.connection