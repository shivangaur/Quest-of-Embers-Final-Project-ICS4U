import java.awt.*;

public class Projectile {
    //Constants
    public static final int PLAYER = 0, ENEMY = 1;
    public static final int LEFT = 0, RIGHT = 1;
    //Fields
    private double x, y;
    private double damage, speed;
    private int direction, type;
    private double spriteCount = 0, flightTime;
    private boolean exploding, doneExploding;
    private Image[] projectileSprites;
    // Angled projectile fields
    private double angle;
    private Rectangle angledRect;
    private boolean isAngled;
    // Sprite Image Arrays
    private static Image[] iceSprites = new Image[60];
    private static Image[] darkSprites = new Image[60];
    private static Image[] explosionSprites = new Image[44];
    public static void init(){
        iceSprites = Utilities.spriteArrayLoad(iceSprites, "Projectiles/Iceball/iceball");
        darkSprites = Utilities.spriteArrayLoad(darkSprites, "Projectiles/DarkCast/darkCast");
        explosionSprites = Utilities.spriteArrayLoad(explosionSprites, "Projectiles/Explosion/explosion");

    }
    public Projectile(int type, double x, double y, double damage, double speed){
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.speed = speed;
        this.type = type;
        if(speed > 0){
            this.direction = RIGHT;
        }
        else{
            this.direction = LEFT;
        }
        assignArray();

    }
    public Projectile(int type, double startX, double startY,double targX,double targY ,double damage, double speed){
        this.type= type;
        this.damage = damage;
        this.speed = speed;
        isAngled = true;
        if(speed > 0){
            this.direction = RIGHT;
        }
        else{
            this.direction = LEFT;
        }
        this.angle = Math.atan((targY - startY)/(targX - startX));
        assignArray();
        angledRect = Utilities.rectFinder(getSprite());
        // Assigning x and y with proper offset
        x = startX - angledRect.x;
        y = startY - angledRect.y;
        System.out.println(targX - startX);
    }
    public void assignArray(){
        if(type == PLAYER){
            projectileSprites = iceSprites;
        }
        else{
            projectileSprites = darkSprites;
        }
    }
    public void update(){
        updateSprite();
        updatePos();
    }
    public void updatePos(){
        if(!exploding){
            // Updating X and Y coordinate
            if(isAngled){
                x += Math.cos(angle) * speed;
                y += Math.sin(angle) * speed;
            }
            else{
                x += speed;
            }
            // Updating the time counter and forcing the projectile to explode after a while
            flightTime++;
            if(flightTime > 400){
                explode();
            }
        }
    }
    public void updateSprite(){
        spriteCount += 0.5;
        if(exploding && spriteCount >= explosionSprites.length){
            doneExploding = true;
        }
        if(spriteCount >= projectileSprites.length){
            spriteCount = 0;
        }
    }
    public void explode(){
        exploding = true;
        spriteCount = 0;
    }
    // Getter methods
    public Image getSprite(){
        Image sprite;
        int spriteIndex = (int)Math.floor(spriteCount);
        if(exploding){
            sprite = explosionSprites[spriteIndex];
        }
        else{
            sprite = projectileSprites[spriteIndex];
            if(direction == RIGHT){
                sprite = Utilities.flipSprite(sprite);
            }
            if(isAngled){
                int noRotHeight = projectileSprites[0].getHeight(null);
                int noRotWidth = projectileSprites[0].getWidth(null);
                if(Math.sin(angle) * speed < 0){
                    if(Math.cos(angle) * speed < 0){
                        sprite = Utilities.rotateSprite(sprite,angle,0, noRotHeight);
                    }
                    else{
                        sprite = Utilities.rotateSprite(sprite,angle, noRotWidth,noRotHeight/2);
                    }
                }
                else{
                    sprite = Utilities.rotateSprite(sprite,angle,noRotWidth/2,noRotHeight/2);

                }
            }
        }
        return sprite;
    }
    public Rectangle getHitbox(){
        if(isAngled){
            return new Rectangle(angledRect.x + (int)x, angledRect.y + (int)y, angledRect.width, angledRect.height);
        }
        // Since the area where the projectile applies damage is on the end of the image, offsets must be applied depending on direction
        if(direction == RIGHT) {
            return new Rectangle((int) x+110, (int) y, 58, 18);
        }
        else{
            return new Rectangle((int) x, (int) y, 58, 18);
        }
    }
    public double getX(){
        // When exploding, the picture size changes, so the coordinate must change to accommodate
        if(exploding){
            if(isAngled){
                return angledRect.x + x - 20;
            }
            else if(direction == LEFT){
                return x - 30;
            }
            else{
                return x + 120;
            }
        }
        // Returning normal value otherwise
        return x;
    }
    public double getY(){
        // When exploding, the picture size changes, so the coordinate must change to accommodate
        if(exploding){
            if(isAngled){
                return angledRect.y + y - 10;
            }
            return y - 20;
        }
        // Returning normal value otherwise
        return y;
    }
    public double getDamage(){return damage;}
    public double getSpeed(){return speed;}
    public boolean isExploding() {return exploding;}
    public boolean isDoneExploding(){return doneExploding;}
    public int getType(){ return type;}
    public double getAngle(){ return angle;}
}
