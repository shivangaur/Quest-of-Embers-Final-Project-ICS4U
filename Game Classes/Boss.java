import java.awt.*;
import java.awt.image.BufferedImage;

public class Boss extends Enemy{
    // Fields
    // Images
    private Image bossHealthBar;
    // Initialization of Class
    public static void init(){

    }
    // Constructor
    public Boss(String data) {
        super(data);
        health = 5000;
        damage = 250;
    }
    // General methods
    @Override
    public void drawHealth(Graphics g, int levelOffset){
        g.drawString("BOSS HEALTH: " + health, 500, 400);
    }
    @Override
    public void updateSprite() {

    }
    // Getter methods
    @Override
    public Image getSprite() {
        return new BufferedImage(1,1,1);
    }

    @Override
    public Rectangle getHitbox() {
        return new Rectangle((int)x, (int)y, 100, 100);
    }
}