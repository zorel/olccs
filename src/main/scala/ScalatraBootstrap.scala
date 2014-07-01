import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.zorel.olccs._
import org.scalatra._
import javax.servlet.ServletContext
import org.zorel.olccs.elasticsearch.ElasticSearch
import _root_.akka.actor.ActorSystem
import org.zorel.olccs.models._
import scala.slick.driver.H2Driver.simple._


import scala.slick.session.Session

class ScalatraBootstrap extends LifeCycle {
  val system = ActorSystem("OlccsSystem")
  ConfiguredBoard("euromussels", "http://euromussels.eu/?q=tribune.xml","last_id",Slip.Encoded,"http://euromussels.eu/?q=tribune/post","message","http://euromussels.eu/node?destination=node","","name","pass")
  ConfiguredBoard("sveetch", "http://www.sveetch.net/tribune/remote/xml/","last_id",Slip.Raw,"http://www.sveetch.net/tribune/post/xml/","content","http://www.sveetch.net/accounts/login/","","username","password")
  ConfiguredBoard("dlfp", "https://linuxfr.org/board/index.xml","",Slip.Encoded,"https://linuxfr.org/board","board[message]","https://linuxfr.org/compte/connexion","","account[login]","account[password]")
  ConfiguredBoard("hadoken", "http://hadoken.free.fr/board/remote.php","",Slip.Encoded,"http://hadoken.free.fr/board/post.php","message","","","","")
  ConfiguredBoard("see", "http://tout.essaye.sauf.ca/tribune.xml","last_id",Slip.Raw,"http://tout.essaye.sauf.ca/tribune/post","message","http://tout.essaye.sauf.ca/user/login","","name","pass")
//  ConfiguredBoard("olo", "http://board.olivierl.org/remote.xml","",Slip.Raw,"http://board.olivierl.org/add.php","message","","","","")
  ConfiguredBoard("moules", "http://moules.org/board/backend","last_id",Slip.Encoded,"http://moules.org/board/add","message","http://moules.org/board","","name","pass")
  ConfiguredBoard("batavie", "http://batavie.leguyader.eu/remote.xml","last",Slip.Raw,"http://batavie.leguyader.eu/index.php/add","message","http://batavie.leguyader.eu/user.php/login","","login","passwd")
  ConfiguredBoard("bouchot", "http://bouchot.org/tribune/remote","last",Slip.Encoded,"http://bouchot.org/tribune/post_coincoin","missive","http://bouchot.org/account/login","","login","password")
//  ConfiguredBoard("jplop", "http://catwitch.eu/jplop/backend","",Slip.Encoded,"http://catwitch.eu/jplop/post","message","http://catwitch.eu/jplop/logon","","username","password")
  ConfiguredBoard("finss", "http://www.finss.fr/drupal/tribune/xml","",Slip.Raw,"http://www.finss.fr/drupal/tribune/post","message","http://www.finss.fr/drupal/user/login","","name","pass")
  ConfiguredBoard("ratatouille", "http://ratatouille.leguyader.eu/data/backend.xml","",Slip.Raw,"http://ratatouille.leguyader.eu/add.php","message","http://ratatouille.leguyader.eu/loginA.php","","login","password")
  ConfiguredBoard("gabuzomeu", "http://gabuzomeu.fr/tribune.xml","",Slip.Raw,"http://gabuzomeu.fr/tribune/post","message","","","","")
  ConfiguredBoard("devnewton", "http://devnewton.bci.im/fr/chat/xml","last_id",Slip.Raw,"http://devnewton.bci.im/fr/chat/post","message","","","","")

//Namespace pourri
//  Board("comptoir", "http://lordoric.free.fr/daBoard/remote.xml","",Slip.Raw,"http://lordoric.free.fr/daBoard/remote.xml","message","","","","")
//XML non valide
//  Board("djangotribune-demo", "http://sveetchies.sveetch.net/tribune/remote/xml/","",Slip.Encoded,"http://ygllo.com/tribune/post","message","","","","")
//XML non valide:  Board("kadreg","http://kadreg.org/board/backend.php","",Slip.Encoded,"http://kadreg.org/board/add.php","message","","","","")
//  Board("darkside", "http://quadaemon.free.fr/remote.xml","",Slip.Encoded,"http://quadaemon.free.fr/add.xml","message","","","","")
//

  override def init(context: ServletContext) {
    val l = LoggerFactory.getLogger(getClass)
    l.info("Initializing Scalatra Bootstrap")
    Scheduler.start
    context.initParameters("org.scalatra.environment") = OlccsConfig.config("env")

    OlccsConfig.graphiteReporter.start(1, TimeUnit.MINUTES)
    try {
      l.info("Creating database")
      OlccsConfig.db withSession { implicit session: Session =>
        (User.ddl ++ Storage.ddl).create
      }
      l.info("Database created")
    } catch {
      case e: Throwable => l.info("Database already exists")
    }
    context mount (new OlccsServlet, "/*")
    context mount (new TribuneServlet, "/t/*")
    context mount (new UserServlet, "/u/*")
    context mount (new StatsServlet, "/s/*")
    l.info("Done Scalatra Bootstrap")
  }

  override def destroy(context: ServletContext) {
    ElasticSearch.close()
    system.shutdown()
  }
}
