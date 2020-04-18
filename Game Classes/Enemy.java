import java.awt.*;

public abstract class Enemy {
    // Constants
    public static final int RIGHT = 0, LEFT = 1;
    protected static final double GRAVITY = 0.25;
    //Fields
    protected double x, y, velocityX, velocityY;
    protected double spriteCount;
    protected int direction;
    protected int health, maxHealth, damage, difficulty;
    protected boolean isActive, knockedBack;
    // General methods
    public void castHit(Player player){
        System.out.println("hit");
        System.out.println(player.getSwordDamage());
        health -= (Utilities.randint(80,100)/100.0)*player.getSpellDamage();
        velocityY = -3;
        if(player.getDirection() == Player.RIGHT){
            velocityX = 3;
        }
        else{
            velocityX = -3;
        }
        knockedBack = true;

    }
    public void swordHit(Player player){
        System.out.println("hit");
        System.out.println(player.getSwordDamage());
        health -= (Utilities.randint(80,100)/100.0)*player.getSwordDamage();
        velocityY = -4;
        if(player.getDirection() == Player.RIGHT){
            velocityX = 4;
        }
        else{
            velocityX = -4;
        }
        knockedBack = true;

    }
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

}
