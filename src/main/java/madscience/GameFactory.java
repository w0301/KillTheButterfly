package madscience;

import java.util.Random;
import madscience.sprites.EnemySprite;
import madscience.sprites.ShooterSprite;

/**
 *
 * @author Richard Kakaš
 */
public class GameFactory {

    private static final Random rand = new Random();

    public static Game createGame(int width, int height, int diff) {
        Game newGame = new Game(width, height);

        /// enemies
        newGame.setEnemiesToGen(20 + 10 * diff);
        newGame.setEnemiesGenInterval(2000);

        EnemySprite enemy1 = new EnemySprite(newGame, EnemySprite.DEFAULT_IMG_1, 1);
        enemy1.setSpeedXY(0, 75);
        enemy1.addGun(new ShooterSprite.Gun(0, enemy1.getHeight() / 2, -100, 0));
        enemy1.setShootingInterval(5000);
        enemy1.setShooting(true);

        EnemySprite enemy2 = new EnemySprite(newGame, EnemySprite.DEFAULT_IMG_2, 2);
        enemy2.setSpeedXY(0, 75);
        enemy2.addGun(new ShooterSprite.Gun(0, enemy2.getHeight() / 2, -130, 0));
        enemy2.setShootingInterval(4000);
        enemy2.setShooting(true);

        EnemySprite enemy3 = new EnemySprite(newGame, EnemySprite.DEFAULT_IMG_3, 3);
        enemy3.setSpeedXY(0, 75);
        enemy3.addGun(new ShooterSprite.Gun(0, enemy3.getHeight() / 4, -130, 0));
        enemy3.addGun(new ShooterSprite.Gun(0, enemy3.getHeight() / 4 + enemy3.getHeight() / 2, -130, 0));
        enemy3.setShootingInterval(3500);
        enemy3.setShooting(true);

        newGame.addPossibleEnemy(0.70, enemy1);
        newGame.addPossibleEnemy(0.25, enemy2);
        newGame.addPossibleEnemy(0.05, enemy3);

        return newGame;
    }

}
