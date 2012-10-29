package madscience.sprites;

import java.awt.image.BufferedImage;
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

        public BulletSprite getBullet(Game game, AbstractSprite owner) {
            BulletSprite bullet = new BulletSprite(game, owner);
            bullet.setXY(x, y);
            bullet.setSpeedXY(bulletSpeedX, bulletSpeedY);
            return bullet;
        }

    }

    List<Gun> guns;
    boolean shooting = false;

    // in miliseconds
    long shootingInterval = 0;
    double shootInTime = 0;

    public ShooterSprite(Game game, BufferedImage image) {
        super(game, image);
        this.guns = new ArrayList<Gun>();
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

    public void setShootingInterval(long interval) {
        shootingInterval = interval;
        shootInTime = 0;
    }

    public synchronized boolean shoot() {
        if (shootInTime <= 0) {
            for (Gun gun : guns) {
                BulletSprite bullet = gun.getBullet(game, this);
                double newX = x + bullet.getX() - bullet.getWidth() / 2;
                double newY = y + bullet.getY();
                if (bullet.getSpeedY() < 0) newY -= bullet.getHeight();

                bullet.setXY(newX, newY);
                game.addSprite(bullet);
            }
            shootInTime = shootingInterval;
            return true;
        }
        return false;
    }

    @Override
    public void update(double sec) {
        super.update(sec);

        // adding bullets
        if (shooting) shoot();
        if (shootInTime > 0) shootInTime -= sec * 1000;
    }

}
