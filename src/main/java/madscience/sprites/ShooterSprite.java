package madscience.sprites;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import madscience.Game;

/**
 *
 * @author richard
 */
public abstract class ShooterSprite extends MovableSprite {

    public static class Gun {
        private double x, y;
        private double bulletSpeedX, bulletSpeedY;

        public Gun(double x, double y, double bulletSpeedX, double bulletSpeedY) {
            this.x = x;
            this.y = y;
            this.bulletSpeedX = bulletSpeedX;
            this.bulletSpeedY = bulletSpeedY;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public BulletSprite getBullet(Game game) {
            BulletSprite bullet = new BulletSprite(game, x, y);
            bullet.setSpeedXY(bulletSpeedX, bulletSpeedY);
            return bullet;
        }

    }

    List<Gun> guns;
    boolean shooting = false;

    // in miliseconds
    long shootingInterval = 0;
    long lastShootTime = 0;

    public ShooterSprite(Game game, double x, double y) {
        super(game, x, y);
        this.guns = new ArrayList<Gun>();
    }

    public ShooterSprite(Game game) {
        this(game, 0, 0);
    }

    public void addGun(Gun gun) {
        guns.add(gun);
    }

    public boolean isShooting() {
        return shooting;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public void setShooting(long interval) {
        setShooting(true);
        shootingInterval = interval;
    }

    @Override
    public void update(double sec) {
        super.update(sec);

        // adding bullets
        if (shooting && (System.currentTimeMillis() - lastShootTime) >= shootingInterval) {
            for (Gun gun : guns) {
                BulletSprite bullet = gun.getBullet(game);
                bullet.setXY(x + bullet.getX() - bullet.getWidth() / 2,
                             y + bullet.getY() - bullet.getHeight());
                game.addSprite(bullet);
            }
            lastShootTime = System.currentTimeMillis();
        }
    }

}
