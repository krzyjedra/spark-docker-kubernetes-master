ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "krzysiek"
ThisBuild / scalaVersion := "2.12.8"

lazy val root = (project in file("."))
  .settings(
    name := "spark-docker-kubernetes-master"
  )
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.1" % "provided"
enablePlugins(DockerPlugin)
docker / dockerfile := {
  val jarFile: File = (Compile / packageBin / sbt.Keys.`package`).value
  val classpath = (Compile / managedClasspath).value
  val mainclass = (Compile / packageBin / mainClass).value.getOrElse(sys.error("Expected exactly one main class"))
  val jarTarget = s"/opt/spark/jars/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/opt/spark/jars/" + _.getName)
    .mkString(":") + ":" + jarTarget
  new Dockerfile {
    // Base image
    from("spark:v3.2.1")
    // Add all files on the classpath
    //    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    user("root")
    // On launch run Java with the classpath and the main class
    entryPoint("/opt/spark/bin/spark-submit", "--conf", "spark.jars.ivy=/tmp/.ivy", jarTarget)
  }
}

docker / imageNames := Seq(
  // Sets the latest tag
  ImageName(s"${organization.value}/${name.value}:${version.value}")
)