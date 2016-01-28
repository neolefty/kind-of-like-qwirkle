/**
 * <h1>Architecture</h1>
 *
 * <p>Each level knows nothing about higher levels.</p>
 *
 * <ol>
 *     <li>
 *         Game model (lowest level) & players (<tt>qwirkle.game.base</tt>, <tt>qwirkle.game.control.players</tt>)
 *         <ul>
 *             <li>base classes & interfaces: QwirkleGrid, QwirkleColor, QwirkleSettings, QwirklePlayer, etc.</li>
 *             <li>implementations: QwirkleGridImpl, MaxPlayer, etc.</li>
 *         </ul>
 *     </li>
 *
 *     <li>
 *        Game control: depends on game model (<tt>qwirkle.game.control</tt>, <tt>qwirkle.game.event</tt>)
 *         <ul>
 *             <li>events: GameStarted, TurnTaken, etc.</li>
 *             <li>gameplay: GameController, autoplay threads</li>
 *         </ul>
 *     </li>
 *
 *     <li>
 *         UI control: depends on game control and game model (<tt>qwirkle.ui.control</tt>, <tt>qwirkle.ui.event</tt>)
 *         <ul>
 *             <li>Dragging & dropping: DragPiece, PieceDropWatcher, etc</li>
 *             <li>Building up a play piece by piece: HypotheticalPlay</li>
 *             <li>Threading: QwirkleThreads, ThreadStatus</li>
 *             <li>UI sugar: HighlightTurn</li>
 *         </ul>
 *     </li>
 *
 *     <li>
 *         UI implementation (view): depends on game model & control and UI control (<tt>qwirkle.ui</tt>)
 *         <ul>
 *             <li>general implementations & abstract interfaces in main package</li>
 *             <li>swing: Swing implementation</li>
 *         </ul>
 *     </li>
 *
 *     <li>
 *         Tests: get to violate all abstraction rules. (<tt>qwirkle.test</tt>)
 *     </li>
 * </ol>
 *
 * <ul>
 *     <li>
 *         Attic: stuff I don't want to throw away but which isn't used in the main code anymore (<tt>qwirkle.attic</tt>)</p>
 *         <ul>
 *             <li>old attempts at a grid panel</li>
 *             <li>old ideas about automatic change events</li>
 *         </ul>
 *     </li>
 * </ul>
 */
package qwirkle;