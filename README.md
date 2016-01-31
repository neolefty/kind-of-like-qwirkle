An implementation of the boardgame [Qwirkle, by
MindWare](http://mindware.com/qwirkle-a2-32016.fltr).
Originally written as a teaching aid for an AP computer science class.

Students can:

  * Make new tile shapes
  * Write AIs (but first, hide MaxPlayer from them)
  * Draw doodles and animations
  * Change the game rules

Currently, there is a Java Swing UI, but it's fairly well separated
from the game logic, so it should be possible to write other UIs.

Major To Dos:

  * Make the human player interface more graceful.
    It's too AI-centric.
  * Add network play.
  * Port to other platforms (Android, web, robo-vm)
  * ... talk to MindWare?

To run it,

    $ git clone https://github.com/neolefty/kind-of-like-qwirkle
	$ cd kind-of-like-qwirkle
	$ gradle runSwing

Or in an IDE, include these two source dirs:

  * `src/main/java/`
  * `src/swing/java/`

And run the class `qwirkle.ui.swing.main.SwingMain`