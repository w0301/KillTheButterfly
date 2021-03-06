package madscience.sprites;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import madscience.views.GameView;

/**
 *
 * @author richard
 */
public abstract class ShooterSprite extends MovableSprite {

    public static class Gun {
        private double x, y;
        private double bulletSpeedX, bulletSpeedY;
        private BufferedImage bulletImg;

        public Gun(double x, double y, double bulletSpeedX, double bulletSpeedY) {
            this.x = x;
            this.y = y;
            this.bulletSpeedX = bulletSpeedX;
            this.bulletSpeedY = bulletSpeedY;
        }

        public Gun(double x, double y, double bulletSpeedX, double bulletSpeedY, BufferedImage img) {
            this(x, y, bulletSpeedX, bulletSpeedY);
            bulletImg = img;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public BulletSprite getBullet(GameView game, AbstractSprite owner) {
            BulletSprite bullet;
            if (bulletImg != null) bullet = new BulletSprite(game, owner, bulletImg);
            else bullet = new BulletSprite(game, owner);
            bullet.setXY(x, y);
            bullet.setSpeedXY(bulletSpeedX, bulletSpeedY);
            return bullet;
        }

    }

    private List<Gun> guns;
    private boolean shooting = false;

    // in miliseconds
    protected long shootingInterval = 0;
    protected double shootInTime = 0;

    public ShooterSprite(GameView game, SpriteView view) {
        super(game, view);
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
                double newX = x + bullet.getX();
                double newY = y + bullet.getY();
                if (bullet.getSpeedY() < 0) newY -= bullet.getHeight();
                if (bullet.getSpeedY() != 0) newX -= bullet.getWidth() / 2;

                if (bullet.getSpeedX() < 0) newX -= bullet.getWidth();
                if (bullet.getSpeedX() != 0) newY -= bullet.getHeight() / 2;

                double newSpeedX = bullet.getSpeedX();
                double newSpeedY = bullet.getSpeedY();
                if (newSpeedX != 0) newSpeedX += getSpeedX();
                if (newSpeedY != 0) newSpeedY += getSpeedX();
                bullet.setSpeedXY(newSpeedX, newSpeedY);

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

    @Override
    public void performAdded() {
    }

    @Override
    public void performRemoved() {
    }

}
