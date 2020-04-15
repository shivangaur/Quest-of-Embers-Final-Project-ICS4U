import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public abstract class Enemy {
    // Constants
    public static final int RIGHT = 0, LEFT = 1;
    protected static final double GRAVITY = 0.25;
    //Fields
    protected double x, y, velocityX, velocityY;
    protected double spriteCount;
    protected int direction;
    protected int health, maxHealth, damage, difficulty;
    protected boolean isActive;
    // Declaring methods that subclasses need to implement
    public abstract void update(Player player);
    public abstract void checkCollision(Rectangle rect);
    public abstract Image getSprite();
    public abstract Rectangle getHitbox();
    // Getter methods
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public int getHealth(){
        return health;
    }
    public int getMaxHealth(){
        return maxHealth;
    }
    public int getHealthPercent(){
        return (health/maxHealth)*100;
    }
    public void swordHit(Player player){
        System.out.println("hit");
        System.out.println(player.getSwordDamage());
        health-=(randint(80,100)/100.0)*player.getSwordDamage();

    }
    // Helper methods for subclasses
    protected Image flipImage(Image image){
        // Using AffineTransform with Nearest-Neighbour to apply flip while keeping 8-bit style
        AffineTransform flip = AffineTransform.getScaleInstance(-1, 1);
        flip.translate(-image.getWidth(null), 0);
        AffineTransformOp flipOp = new AffineTransformOp(flip, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = flipOp.filter((BufferedImage)image, null);
        return image;
    }
    //Other helper methods
    public static int randint(int low, int high){
        return (int)(Math.random()*(high-low+1)+low);
    }

}
