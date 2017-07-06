import io.circe.Json
import io.shaka.http.Http.http
import io.shaka.http.Request.GET
import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._

import scalaz._
import Scalaz._
import java.util.concurrent._

import scala.collection.mutable.ListBuffer
import java.net.URLEncoder.encode

import scala.collection.immutable.ListMap

/**
  * Created by David on 16-Jun-17.
  */
object Client extends ConfLoader {

  case class Player(rank: Int, dead: Boolean, online: Boolean, character: Character, account: Account)
  case class Character(name: String, level: Int, `class`: String, experience: Long)
  case class Account(name: String)
  case class CurrentPos(rank: Int, claRank: Int, ascRank: Int)

  implicit val playerDecoder: Decoder[Player] = deriveDecoder
  implicit val characterDecoder: Decoder[Character] = deriveDecoder
  implicit val accountDecoder: Decoder[Account] = deriveDecoder

  private val url = "http://api.pathofexile.com/ladders/"

  val myName = conf.getString("account-name")
  val positionFeatureOn = conf.getBoolean("display-position-change")
  var counter = 0;
  var raceState: CurrentPos = null
  var posQueue = new scala.collection.mutable.Queue[CurrentPos]
  var myPlayer = new Player(-1, true, false, new Character("", 1, "Unknown", 0), new Account(myName))
  var cMap = Map[String, Player]()

  def stuff(window: TextWindowBuilder) = {
    myPlayer = new Player(-1, true, false, new Character("", 1, "Unknown", 0), new Account(myName))
    var i = 0;
    var result = callAPI(i)
    while (!result && i < 74) {
      i = i + 1
      result = callAPI(i)
    }

    val s = cMap.toSeq.sortWith { case ((_, m1), (_, m2)) =>
        if (m1.character.experience > m2.character.experience) true
        else if (m1.character.experience == m2.character.experience && m1.rank < m2.rank) true else false
    }
    val ascList = s.filter(x => x._2.character.`class` == myPlayer.character.`class`)
    val claList = s.filter(x => changeClass(x._2.character).`class` == changeClass(myPlayer.character).`class`)

    var toDisp = new ListBuffer[String]()
    if (myPlayer.rank == -1 && raceState == null) {
      toDisp += "You have no living character on the rankings"
    } else {
      toDisp += s"${myPlayer.character.name} (${myPlayer.character.level} ${myPlayer.character.`class`})"
      val myClass = changeClass(myPlayer.character).`class`
      raceState = CurrentPos(myPlayer.rank, claList.indexOf((myPlayer.character.name, myPlayer)) + 1, ascList.indexOf((myPlayer.character.name, myPlayer)) + 1)
      if (positionFeatureOn) {
        val raceChange = if (posQueue.isEmpty) CurrentPos(0, 0, 0) else {
          val front = posQueue.front
          CurrentPos(raceState.rank - front.rank, raceState.claRank - front.claRank, raceState.ascRank - front.ascRank)
        }
        toDisp += s"Overall rank: ${myPlayer.rank} (${dispPosChange(raceChange.rank)})"
        toDisp += s"Class rank (${myClass}): ${raceState.claRank} (${dispPosChange(raceChange.claRank)})"
        toDisp += s"Ascendancy rank (${myPlayer.character.`class`}): ${raceState.ascRank} " +
          s"(${dispPosChange(raceChange.claRank)})"
      } else {
        toDisp += s"Overall rank: ${myPlayer.rank}"
        toDisp += s"Class rank (${myClass}): ${raceState.claRank}"
        toDisp += s"Ascendancy rank (${myPlayer.character.`class`}): ${raceState.ascRank}"
      }
    }
    window.setText(toDisp mkString "\n")
  }

  def main(args: Array[String]): Unit = {
    val w = new TextWindowBuilder("Searching ...")
    w.setVisible(true)
    val ex = Executors.newSingleThreadScheduledExecutor(new WorkerThreadFactory("main"))
    val task = new Runnable {
      def run() = stuff(w)
    }
    ex.scheduleAtFixedRate(task, 0, 30, TimeUnit.SECONDS)

    if (positionFeatureOn) {
      val ex2 = Executors.newSingleThreadScheduledExecutor(new WorkerThreadFactory("sub"))
      val task2 = new Runnable {
        def run() = fillPositionQueue()
      }
      ex2.scheduleAtFixedRate(task2, 0, 1, TimeUnit.MINUTES)
    }
  }

  def callAPI(i: Int):  Boolean = {
    val response = http(GET(s"${url}${encode(conf.getString("league-name"), "utf-8")}?limit=200&offset=${i * 200}"))
    if (response.status.toString != "OK") throw new APIResponseException(s"API Request failed: ${response.status}")
    val doc = parse(response.entity.getOrElse("").toString).getOrElse(Json.Null)
    getData(doc)
  }

  def getData(json: Json): Boolean = {
    val cursor: HCursor = json.hcursor
    val players: Decoder.Result[List[Player]] = cursor.downField("entries").as[List[Player]]
    val ascendancyList = players.getOrElse(Nil)
    ascendancyList.map(x => cMap += (x.character.name -> x))
    val myPlayerList = ascendancyList.filter(p => p.account.name == myName && !p.dead)
    if (myPlayerList.length > 0) {
      myPlayer = myPlayerList.head
    }
    myPlayer.rank > 0
  }

  def changeClass(c: Character): Character = c.`class` match {
    case "Slayer" | "Gladiator" | "Champion" => c.copy(`class` = "Duelist")
    case "Assassin" | "Saboteur" | "Trickster" => c.copy(`class` = "Shadow")
    case "Juggernaut" | "Berserker" | "Chieftain" => c.copy(`class` = "Marauder")
    case "Deadeye" | "Raider" | "Pathfinder" => c.copy(`class` = "Ranger")
    case "Ascendant" => c.copy(`class` = "Scion")
    case "Inquisitor" | "Hierophant" | "Guardian" => c.copy(`class` = "Templar")
    case "Elementalist" | "Necromancer" | "Occultist" => c.copy(`class` = "Witch")
    case _ => c
  }

  def fillPositionQueue(): Unit = {
    if (raceState != null) {
      posQueue += raceState
      if (posQueue.size > conf.getInt("position-change-check-interval-in-minutes")) posQueue.dequeue()
    }
  }

  def dispPosChange(i: Int): String = {
    i match {
      case 0 => "-"
      case x if x > 0 => s"+${i.toString}"
      case _ => i.toString
    }
  }

}
