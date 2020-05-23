import java.awt.*;

public class Fire extends Enemy {
    // Fields
    private int attackDelay = 50;
    // Sprites
    private static Image[] motionSprites = new Image[7];
    public static void init(){
        motionSprites = Utilities.spriteArrayLoad(motionSprites, "Enemies/Fire/fire");

    }
    public Fire(String data) {
        super(data);
        health = 100;
        maxHealth=health;
        damage = 15 * difficulty;
    }

    @Override
    public void updateSprite() {
        spriteCount+=0.10;
        if(spriteCount > motionSprites.length){
            spriteCount = 0;
        }
    }
    public void updateMotion(Player player){
        // Applying velocity values to position
        x += velocityX;
        y += velocityY;
        // Resetting boolean values so they can be rechecked for the new position
        platformAhead = false;
        platformBehind = false;
        onMovingPlat = false;
    }
    @Override
    public void updateAttack(Player player){
        isAttacking = getHitbox().intersects(player.getHitbox()); // Setting it to true if there is hitbox collision
        if(isAttacking){
            attackDelay--;
            if(attackDelay == 0){
                player.enemyHit(this);
                attackDelay = 50;
            }
        }
    }
    @Override
    public Image getSprite() {
        Image sprite;
        int spriteIndex = (int)Math.floor(spriteCount);
        sprite = motionSprites[spriteIndex];
        return sprite;
    }

    @Override
    public Rectangle getHitbox() {
        return new Rectangle((int)x + 70, (int)y + 40, 90, 200);
    }


}