import javax.servlet.ServletContext

import _root_.akka.actor.ActorSystem
import org.scalatra._
import org.slf4j.LoggerFactory
import org.zorel.olccs._
import org.zorel.olccs.models._

class ScalatraBootstrap extends LifeCycle {
  val system = ActorSystem("OlccsSystem")
  ConfiguredBoard("euromussels", "http://euromussels.eu/?q=tribune.xml", "last_id", Slip.Encoded, "http://euromussels.eu/?q=tribune/post", "message", "http://euromussels.eu/node?destination=node", "", "name", "pass", "#d0d0ff", "euro,euroxers")
  ConfiguredBoard("sveetch", "http://www.sveetch.net/tribune/remote/xml/", "last_id", Slip.Raw, "http://www.sveetch.net/tribune/post/xml/", "content", "http://www.sveetch.net/accounts/login/", "", "username", "password", "#ededdb", "sveetch")
  ConfiguredBoard("dlfp", "https://linuxfr.org/board/index.xml", "", Slip.Encoded, "https://linuxfr.org/board", "board[message]", "https://linuxfr.org/compte/connexion", "linuxfr.org_session=", "account[login]", "account[password]", "#dac0de", "linuxfr,beyrouth,passite,dapassite")
  ConfiguredBoard("hadoken", "http://hadoken.free.fr/board/remote.php", "", Slip.Encoded, "http://hadoken.free.fr/board/post.php", "message", "", "", "", "", "#77aadd", "axel,waf")
//  ConfiguredBoard("see", "http://tout.essaye.sauf.ca/tribune.xml","last_id",Slip.Raw,"http://tout.essaye.sauf.ca/tribune/post","message","http://tout.essaye.sauf.ca/user/login","","name","pass")
//  ConfiguredBoard("olo", "http://board.olivierl.org/remote.xml","",Slip.Raw,"http://board.olivierl.org/add.php","message","","","","")
  ConfiguredBoard("moules", "http://moules.org/board/backend", "last_id", Slip.Raw, "http://moules.org/board/add", "message", "http://moules.org/board", "", "name", "pass", "#ffe3c9", "")
  ConfiguredBoard("batavie", "http://batavie.leguyader.eu/remote.xml", "last", Slip.Raw, "http://batavie.leguyader.eu/index.php/add", "message", "http://batavie.leguyader.eu/user.php/login", "", "login", "passwd", "#ffccaa", "")
//  ConfiguredBoard("finss", "http://www.finss.fr/drupal/tribune/xml","",Slip.Raw,"http://www.finss.fr/drupal/tribune/post","message","http://www.finss.fr/drupal/user/login","","name","pass")
  ConfiguredBoard("ratatouille", "http://ratatouille.leguyader.eu/data/backend.xml", "", Slip.Raw, "http://ratatouille.leguyader.eu/add.php", "message", "http://ratatouille.leguyader.eu/loginA.php", "", "login", "password", "#cacaca", "")
  ConfiguredBoard("gabuzomeu", "http://gabuzomeu.fr/tribune.xml", "", Slip.Raw, "http://gabuzomeu.fr/tribune/post", "message", "", "", "", "", "aaffbb", "#dac0de")
  ConfiguredBoard("devnewton", "http://b3.bci.im/legacy/xml", "last", Slip.Encoded, "http://b3.bci.im/legacy/post", "message", "", "", "", "", "#f5d6cc", "b3,jb3,grosidlol")
//  ConfiguredBoard("bluenight", "http://cdesoutter.free.fr/remote.php","",Slip.Raw,"http://devnewton.bci.im/fr/chat/post","message","","","","")

// Dead tribune
//  ConfiguredBoard("bouchot", "http://bouchot.org/tribune/remote","last",Slip.Encoded,"http://bouchot.org/tribune/post_coincoin","missive","http://bouchot.org/account/login","","login","password")
//  ConfiguredBoard("jplop", "http://catwitch.eu/jplop/backend","",Slip.Encoded,"http://catwitch.eu/jplop/post","message","http://catwitch.eu/jplop/logon","","username","password")

//Namespace pourri
//  Board("comptoir", "http://lordoric.free.fr/daBoard/remote.xml","",Slip.Raw,"http://lordoric.free.fr/daBoard/remote.xml","message","","","","")
//Board("kadreg","http://kadreg.org/board/backend.php","",Slip.Encoded,"http://kadreg.org/board/add.php","message","","","","")
//Board("darkside", "http://quadaemon.free.fr/remote.xml","",Slip.Encoded,"http://quadaemon.free.fr/add.xml","message","","","","")
//

  override def init(context: ServletContext) {
    val l = LoggerFactory.getLogger(getClass)
    l.info("Initializing Scalatra Bootstrap")
    Scheduler.start
    context.initParameters("org.scalatra.environment") = OlccsConfig.config("env")

    context mount (new OlccsServlet, "/*")
    context mount (new TribuneServlet, "/t/*")

    l.info("Done Scalatra Bootstrap")
  }

  override def destroy(context: ServletContext) {
    system.shutdown()
  }
}
