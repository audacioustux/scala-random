import akka.stream.{FlowShape, Graph, IOResult, OverflowStrategy}
import akka.stream.scaladsl.{Balance, Broadcast, FileIO, Flow, GraphDSL, Merge, RunnableGraph, Source}
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import java.nio.file.Path
import java.nio.file.Paths
import akka.util.ByteString
import scala.concurrent.Future
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import akka.NotUsed
import akka.stream.scaladsl.Sink
import concurrent.duration.DurationInt
import java.nio.file.Files
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@main def hello: Unit =
  given actorSystem: ActorSystem = ActorSystem("Assignment1")
  given ec: ExecutionContext = actorSystem.dispatcher

  // create case classes
  case class Library(groupID: String, artifactID: String, version: String)
  case class Dependency(groupID: String, artifactID: String, version: String)
  sealed trait DependencyType
  case object Compile extends DependencyType
  case object Runtime extends DependencyType
  case class DependencyRecord(library: Library, dependency: Dependency, dependencyType: DependencyType)

  // read maven_dependencies.csv from resources folder
  val resourcesFolder: String = "src/main/resources"
  val pathCSVFile: Path = Paths.get(s"$resourcesFolder/maven_dependencies.csv")

  // check if the file exists
  if (!Files.exists(pathCSVFile)) {
    println(s"File $pathCSVFile does not exist")
    sys.exit(1)
  }

  // create a Source from the CSV file
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(pathCSVFile)

  // parse the CSV file
  val csvParsing: Flow[ByteString, List[ByteString], NotUsed] = CsvParsing.lineScanner()

  // map the CSV file to a Map
  val mappingHeader: Flow[List[ByteString], Map[String, ByteString], NotUsed] = CsvToMap.toMap()

  // map the Map to a DependencyRecord
  val flowLibraryDependency: Flow[Map[String, ByteString], DependencyRecord, NotUsed] = Flow[Map[String, ByteString]]
    .map(tempMap => {
      tempMap.map(element => {
        (element._1, element._2.utf8String)
      })
    }).map(record => {
      DependencyRecord(
        library = Library(
          groupID = record("library").split(":")(0),
          artifactID = record("library").split(":")(1),
          version = record("library").split(":")(2)
        ),
        dependency = Dependency(
          groupID = record("dependency").split(":")(0),
          artifactID = record("dependency").split(":")(1),
          version = record("dependency").split(":")(2)
        ),
        dependencyType = record("type") match {
          case "Compile" => Compile
          case "Runtime" => Runtime
        }
      )
    })

  // group the dependency records by library
  val flowGroupByLibrary: Flow[DependencyRecord, (Library, Set[DependencyRecord]), NotUsed] = Flow[DependencyRecord]
    .groupBy(185, libraryDependency => libraryDependency.library)
    .fold((Library("", "", ""), Set[DependencyRecord]()))((acc, libraryDependency) => {
      val (library, dependencies) = acc
      (libraryDependency.library, dependencies + libraryDependency)
    })
    .mergeSubstreams

  // case class to map a library to its dependency count
  case class LibraryDependencyCount(library: Library, dependencyCount: Map[DependencyType, Int])

  val flowDependencyCount: Graph[FlowShape[(Library, Set[DependencyRecord]), LibraryDependencyCount], NotUsed] =
    Flow.fromGraph(
      GraphDSL.create() {
        implicit builder =>
          import GraphDSL.Implicits._

          // create a balance element
          val balance = builder.add(Balance[(Library, Set[DependencyRecord])](2))

          // create a merge element
          val merge = builder.add(Merge[LibraryDependencyCount](2))

          // count dependency types
          val pipeline: Flow[(Library, Set[DependencyRecord]), LibraryDependencyCount, NotUsed] = Flow[(Library, Set[DependencyRecord])]
            .map(libraryDependency => {
              val dependencyCount: Map[DependencyType, Int] = libraryDependency._2.groupBy(_.dependencyType).map((dependencyType, dependencies) => {
                (dependencyType, dependencies.size)
              })
              LibraryDependencyCount(library = libraryDependency._1, dependencyCount = dependencyCount)
            })

          // connect the elements
          balance.out(0) ~> pipeline ~> merge.in(0)
          balance.out(1) ~> pipeline ~> merge.in(1)

          // expose ports
          FlowShape(balance.in, merge.out)
      }
    )

  // create a flow to throttle the stream
  val flowThrottle = Flow[LibraryDependencyCount]
    .throttle(elements = 10, per = 1.second)

  // create a flow to buffer the stream
  val flowBuffer = Flow[LibraryDependencyCount]
    .buffer(size = 5, OverflowStrategy.backpressure)
  
  // create a sink to print the stream
  val sinkPrint = Sink.foreach[LibraryDependencyCount](libraryDependencyCount => {
    val LibraryDependencyCount(library, dependencyCount) = libraryDependencyCount
    val Library(groupID, artifactID, version) = library
    val compileCount = dependencyCount.getOrElse(Compile, 0)
    val runtimeCount = dependencyCount.getOrElse(Runtime, 0)
    println(s"Name: ${library.groupID} ${library.artifactID} ${library.version} --> Compile: $compileCount, Runtime: $runtimeCount")
  })

  source
    .via(csvParsing)
    .via(mappingHeader)
    .via(flowLibraryDependency)
    .via(flowGroupByLibrary)
    .throttle(elements = 10, per = 1.second)
    .buffer(size = 5, OverflowStrategy.backpressure)
    .via(flowDependencyCount)
    .runWith(sinkPrint)
    .onComplete(_ => actorSystem.terminate())

  // wait for the actor system to terminate
  Await.result(actorSystem.whenTerminated, Duration.Inf)