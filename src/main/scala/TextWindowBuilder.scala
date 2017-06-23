import java.awt._
import javax.swing._

class TextWindowBuilder (
                          textToDisplay: String,
                          location: Point = new Point(20, 150)
                        ) extends ConfLoader {

  val textArea = new JTextArea(conf.getInt("window-rows"), conf.getInt("window-columns"))
  textArea.setFont(new Font(conf.getString("font"), Font.PLAIN, conf.getInt("font-size")))
  textArea.setEditable(false)
  textArea.setForeground(Color.decode(conf.getString("foreground-color")))
  textArea.setBackground(Color.decode(conf.getString("background-color")))
  textArea.setMargin(new Insets(12, 12, 12, 12))
  textArea.setText(textToDisplay)

  val f = new JFrame
  f.setAlwaysOnTop(true)
  f.setUndecorated(true)
  f.setOpacity(conf.getDouble("window-opacity").toFloat)
  f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  f.getContentPane.add(textArea, BorderLayout.CENTER)
  f.setLocation(new Point(conf.getInt("window-top-x"), conf.getInt("window-top-y")))

  def setText(text: String) {
    val code = textArea.setText(text)
    invokeLater(code)
  }

  def setVisible(makeVisible: Boolean) {
    if (makeVisible) {
      val block = {
        f.pack
        f.setVisible(true)
      }
      invokeLater(block)
    } else {
      val block = f.setVisible(false)
      invokeLater(block)
    }
  }

  private def invokeLater[A](blockOfCode: => A) = {
    SwingUtilities.invokeLater(new Runnable {
      def run {
        blockOfCode
      }
    })
  }

}
