import io.circe.Json
import io.shaka.http.Http.http
import io.shaka.http.Request.GET
import io.circe._, io.circe.parser._
import io.circe.generic.semiauto._
import scalaz._
import Scalaz._
import java.util.concurrent._
import scala.collection.mutable.ListBuffer
import java.net.URLEncoder.encode

/**
  * Created by David on 16-Jun-17.
  */
object Client extends ConfLoader {

  case class Player(rank: Int, dead: Boolean, online: Boolean, character: Character, account: Account)
  case class Character(name: String, level: Int, `class`: String, experience: Long)
  case class Account(name: String)

  implicit val playerDecoder: Decoder[Player] = deriveDecoder
  implicit val characterDecoder: Decoder[Character] = deriveDecoder
  implicit val accountDecoder: Decoder[Account] = deriveDecoder

  private val url = "http://api.pathofexile.com/ladders/"

  val myName = conf.getString("account-name")
  var counter = 0;
  var myPlayer = new Player(-1, true, false, new Character("", 1, "Unknown", 0), new Account(myName))

  def stuff(window: TextWindowBuilder) = {
    myPlayer = new Player(-1, true, false, new Character("", 1, "Unknown", 0), new Account(myName))
    var i = 0;
    var result = callAPI(i)
    var collectorAsc = result._1
    var collectorCla = result._2
    while (!result._3 && i < 74) {
      i = i + 1
      result = callAPI(i)
      collectorAsc = collectorAsc |+| result._1
      collectorCla = collectorCla |+| result._2
    }

    var toDisp = new ListBuffer[String]()
    if (myPlayer.rank == -1) {
      toDisp += "You have no living character on the rankings"
    } else {
      toDisp += s"${myPlayer.character.name} (${myPlayer.character.level} ${myPlayer.character.`class`})"
      toDisp += s"Overall rank: ${myPlayer.rank}"
      val myClass = changeClass(myPlayer.character).`class`
      toDisp += s"Class rank (${myClass}): ${collectorCla.get(myClass).getOrElse(0) + 1}"
      toDisp += s"Ascendancy rank (${myPlayer.character.`class`}): " +
        s"${collectorCla.get(myPlayer.character.`class`).getOrElse(0) + 1}"
    }
    window.setText(toDisp mkString "\n")
  }

  def main(args: Array[String]): Unit = {
    val w = new TextWindowBuilder("Searching ...")
    w.setVisible(true)
    val ex = new ScheduledThreadPoolExecutor(1)
    val task = new Runnable {
      def run() = stuff(w)
    }
    val f = ex.scheduleAtFixedRate(task, 0, 30, TimeUnit.SECONDS)
  }

  def callAPI(i: Int): (Map[String, Int], Map[String, Int], Boolean) = {
    val response = http(GET(s"${url}${encode(conf.getString("league-name"), "utf-8")}?limit=200&offset=${i * 200}"))
    if (response.status.toString != "OK") throw new APIResponseException(s"API Request failed: ${response.status}")
    val doc = parse(response.entity.getOrElse("").toString).getOrElse(Json.Null)
    getData(doc)
  }

  def getData(json: Json): (Map[String, Int], Map[String, Int], Boolean) = {
    val cursor: HCursor = json.hcursor
    val players: Decoder.Result[List[Player]] = cursor.downField("entries").as[List[Player]]
    val ascendancyList = players.getOrElse(Nil)
    val classList = ascendancyList.map(p => p.copy(character = changeClass(p.character)))
    val myPlayerList = ascendancyList.filter(p => p.account.name == myName && !p.dead)
    if (myPlayerList.length > 0) {
      myPlayer = myPlayerList.head
    }
    val ascBetter = if (myPlayer.rank > 0) ascendancyList.filter(p => p.rank < myPlayer.rank) else ascendancyList
    val claBetter = if (myPlayer.rank > 0) classList.filter(p => p.rank < myPlayer.rank) else classList
    val a = for {
      e <- ascBetter
    } yield e.character.`class`
    val c = for {
      e <- claBetter
    } yield e.character.`class`
    (a.groupBy(item => item).map(i => (i._1, i._2.length)), c.groupBy(item => item).map(i => (i._1, i._2.length)),
      myPlayer.rank > 0)
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
}
