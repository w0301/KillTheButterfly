package madscience.views;

/**
 *
 * @author Richard Kakaš
 */
public interface GameViewListener {

    public void gameEnded(GameView game, boolean won);
    public void gamePaused(GameView game);
    public void gameUnpaused(GameView game);
}
