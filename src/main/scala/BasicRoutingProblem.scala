package sg.beeline
import com.thesamet.spatial.{KDTreeMap, RegionBuilder}
import Util.Point

class BasicRoutingProblem(val busStops: Seq[BusStop],
                          val suggestions: Seq[Suggestion],
                          val startWalkingDistance : Double = 300.0,
                          val endWalkingDistance : Double = 300.0) extends RoutingProblem {
  println(s"Problem with ${suggestions.size} suggestions")

  type BusStopsTree = KDTreeMap[(Double, Double), BusStop]

  val busStopsTree : BusStopsTree = KDTreeMap.fromSeq(
    busStops map {x => x.xy -> x}
  )

  val requests = suggestions.map(sugg =>
    new Request(this, sugg.start, sugg.end, sugg.time, weight=sugg.weight))
    .filter(_.startStops.nonEmpty)
    .filter(_.endStops.nonEmpty)

  println(s"Only ${requests.size} suggestions used")
  println(s"Average # start stops ${requests.map(_.startStops.size).sum / requests.size.toDouble}")
  println(s"Average # end stops ${requests.map(_.endStops.size).sum / requests.size.toDouble}")

  val distanceMatrix = {
    val ois = new java.io.ObjectInputStream(
                new java.util.zip.GZIPInputStream(
                  new java.io.FileInputStream("./distances_cache.dat.gz")))

    ois.readObject().asInstanceOf[Array[Array[Double]]]
  }

  // The current set of routes for the current iteration
  def distance(a : BusStop, b: BusStop) : Double = {
    distanceMatrix(a.index)(b.index)
  }

  //
  def nearBusStopsStart(origin : Point) =
    kdtreeQuery.queryBall(busStopsTree, origin, this.startWalkingDistance)
    .sortBy(_._1)
    .map(_._2)

  def nearBusStopsEnd(origin : Point) =
    kdtreeQuery.queryBall(busStopsTree, origin, this.endWalkingDistance)
      .sortBy(_._1)
      .map(_._2)

  // Start with a solution where everyone is ferried directly from the nearest
  def initialize = {
    val (routes, badRequests) = DirectFerryRecreate.recreate(this, List(), requests)

    (routes, requests, badRequests)

//    val (routes, badRequests) = LowestRegretRecreate.recreate(this, List(), requests)
//
//    (routes, requests, badRequests)
  }

  def solution = Array[String]()
}
