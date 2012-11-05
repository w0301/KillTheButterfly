package madscience;

import madscience.views.GameView;
import java.util.Random;
import madscience.sprites.BossSprite;
import madscience.sprites.ElixirSprite;
import madscience.sprites.EnemySprite;
import madscience.sprites.ShooterSprite;

/**
 *
 * @author Richard Kakaš
 */
public class GameFactory {

    private static final Random rand = new Random();

    public static GameView createGame(int width, int height, int diff) {
        GameView newGame = new GameView(width, height);

        /// enemies
        newGame.setEnemyGeneration(10 + 10 * diff, 2000, 1, 1);

        EnemySprite enemy1 = new EnemySprite(newGame, EnemySprite.DEFAULT_IMG_1, 1);
        enemy1.addGun(new ShooterSprite.Gun(0, enemy1.getHeight() / 2, -100, 0));
        enemy1.setShootingInterval(5000);
        enemy1.setShooting(true);

        EnemySprite enemy2 = new EnemySprite(newGame, EnemySprite.DEFAULT_IMG_2, 2);
        enemy2.addGun(new ShooterSprite.Gun(0, enemy2.getHeight() / 2, -130, 0));
        enemy2.setShootingInterval(4000);
        enemy2.setShooting(true);

        EnemySprite enemy3 = new EnemySprite(newGame, EnemySprite.DEFAULT_IMG_3, 3);
        enemy3.addGun(new ShooterSprite.Gun(0, enemy3.getHeight() / 4, -130, 0));
        enemy3.addGun(new ShooterSprite.Gun(0, enemy3.getHeight() / 4 + enemy3.getHeight() / 2, -130, 0));
        enemy3.setShootingInterval(3500);
        enemy3.setShooting(true);

        newGame.addPossibleEnemy(0.70, enemy1);
        newGame.addPossibleEnemy(0.25, enemy2);
        newGame.addPossibleEnemy(0.05, enemy3);

        /// adding boss
        BossSprite newBoss = new BossSprite(newGame, 50 + (diff - 1)*20);
        newBoss.addGun(new ShooterSprite.Gun(0, newBoss.getHeight() / 4, -130, 0));
        newBoss.addGun(new ShooterSprite.Gun(0, newBoss.getHeight() / 2, -130, 0));
        newBoss.addGun(new ShooterSprite.Gun(0, newBoss.getHeight() / 4 + newBoss.getHeight() / 2, -130, 0));
        newBoss.setShootingInterval(5000);
        newBoss.setShooting(true);
        newGame.setBossSprite(newBoss);

        /// adding elixirs
        newGame.setElixirGeneration(5000);
        newGame.addPossibleElixir(0.25, new ElixirSprite(newGame, ElixirSprite.LIFE_IMG) {
            @Override
            public void performElixir() {
                game.getPlayerSprite().addLife();
            }
        });
        newGame.addPossibleElixir(0.25, new ElixirSprite(newGame, ElixirSprite.SLOWDOWN_IMG) {
            @Override
            public void performElixir() {
                game.setSpeedRatio(0.5, 5000);
            }
        });
        newGame.addPossibleElixir(0.25, new ElixirSprite(newGame, ElixirSprite.FASTFORWARD_IMG) {
            @Override
            public void performElixir() {
                game.setSpeedRatio(2, 5000);
            }
        });
        newGame.addPossibleElixir(0.25, new ElixirSprite(newGame, ElixirSprite.SHIELD_IMG) {
            @Override
            public void performElixir() {
                game.getPlayerSprite().setShield(true);
            }

            @Override
            public void performElixirShooted() {
                game.removeSprite(this);
            }
        });

        return newGame;
    }

}
