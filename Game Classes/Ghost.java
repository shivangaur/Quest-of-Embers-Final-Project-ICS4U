import java.awt.*;

public class Ghost extends Enemy {
    //Fields
    private static Image[] movingSprites = new Image[5];
    private static Image[] idleSprites= new Image[10];
    private static Image[] attackSprites= new Image[8];
    private static Image[] deathSprites= new Image[7];
    private static Image[] hurtSprites= new Image[1];
    // Fields
    private float alpha;//draw transparent
    private float ghostAlpha = (float)0.005;
    private int speed;
    //Constructor
    public Ghost(String data){
        super(data);
        health = 100 * difficulty;
        maxHealth = health;
        damage = 15;
        speed = difficulty;
        hasAlphaSprites = true;
        alpha = (float) 0.1;
    }
    public static void init(){
        movingSprites = Utilities.spriteArrayLoad(movingSprites, "Enemies/Ghost/move");
        deathSprites = Utilities.spriteArrayLoad(deathSprites, "Enemies/Ghost/death");
        idleSprites = Utilities.spriteArrayLoad(movingSprites, "Enemies/Ghost/idle");
        attackSprites = Utilities.spriteArrayLoad(attackSprites, "Enemies/Ghost/attack");
        hurtSprites = Utilities.spriteArrayLoad(hurtSprites, "Enemies/Ghost/hurt");
    }
    // General methods
    @Override
    public void updateMotion(Player player){
        // Checking the position of the Player and setting velocity towards them
        Rectangle playerHitbox = player.getHitbox();
        double angle = Math.atan(((playerHitbox.y - 10.0) - getHitbox().y)/((playerHitbox.x - 40.0)- getHitbox().x));
        if(Double.isNaN(angle)){
            return;
        }
        if(!isDying()){ // Only move the ghost while it's not dying
            if(playerHitbox.x - 40 >= getHitbox().x){
                x += Math.cos(angle) * speed;
                y += Math.sin(angle) * speed;
                direction = RIGHT;
            }
            else if(playerHitbox.x - 40 < getHitbox().x){
                x -= Math.cos(angle) * speed;
                y -= Math.sin(angle) * speed;
                direction = LEFT;
            }
        }
    }
    @Override
    public void updateAttack(Player player) {
        super.updateAttack(player);
        // Checking if the player should be dealt damage
        if(isAttacking && Utilities.roundOff(spriteCount,2) == attackSprites.length/2.0){
            player.enemyHit(this);
        }
    }

    @Override
    public void updateSprite() {
        if(isHurt){
            if(health <= 0){
                spriteCount += 0.08;
                if(spriteCount > deathSprites.length){
                    isActive = false;
                }
            }
            else{
                spriteCount += 0.08;
                if(spriteCount > hurtSprites.length){
                    spriteCount = 0;
                    isHurt = false;
                }
            }
        }
        else if(isAttacking){
            spriteCount += 0.08;
            if(spriteCount > attackSprites.length){
                spriteCount = 0;
            }
        }
        else{
            spriteCount += 0.08;
            if(spriteCount > movingSprites.length){
                spriteCount = 3;
            }
        }
        // Updating sprite transparency
        alpha+=ghostAlpha;
        if(Utilities.roundOff(alpha,2)==1.0 || Utilities.roundOff(alpha,2) == 0.05){
            ghostAlpha=-ghostAlpha;
        }
    }
    @Override
    public void checkCollision(LevelProp prop){
        // Doing nothing since Ghosts ignore platforms
    }
    // Getter methods
    @Override
    public Image getSprite() {
        Image sprite;
        int spriteIndex = (int)Math.floor(spriteCount);
        if(isHurt){
            if(health <= 0){
                sprite = deathSprites[spriteIndex];
            }
            else{
                sprite = hurtSprites[spriteIndex];
            }
        }
        else if(isAttacking){
            sprite = attackSprites[spriteIndex];
        }
        else{
            sprite = movingSprites[spriteIndex];
        }
        // Flipping image since sprites are left facing
        if(direction == RIGHT){
            sprite = Utilities.flipSprite(sprite);
        }
        return sprite;
    }
    @Override
    public float getSpriteAlpha(){
        if(isHurt){
            return 1;
        }
        return alpha ;
    }

    @Override
    public Rectangle getHitbox() {
        return new Rectangle((int)x + 50, (int)y + 25, 75, 105);
    }
}
