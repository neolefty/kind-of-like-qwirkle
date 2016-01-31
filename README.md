An implementation of the boardgame Qwirkle, by MindWare:

    http://mindware.com/qwirkle-a2-32016.fltr

Originally written as a teaching aid for an AP computer science class.

Students can

  * Make new tile shapes
  * Write AIs (but first, hide MaxPlayer from them)
  * Draw doodles and animations
  * Change the game rules
  * ...

Currently, there is a Java Swing UI, but it's fairly well separated
from the game logic, so it should be possible to write other UIs.

Major To Dos:

  * Make the human player interface more graceful.
    It's too AI-centric.

  * Add network play.

  * Port to other platforms (Android, web, robo-vm)

  * ... talk to MindWare?