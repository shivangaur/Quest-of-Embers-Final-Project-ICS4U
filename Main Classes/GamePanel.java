import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;

class GamePanel extends JPanel implements KeyListener {
    // Window related Objects
    private boolean[] keysPressed; // Array that keeps track of keys that are pressed down
    private MainGame gameFrame;
    // Game related fields
    private Player player = new Player();
    private int levelNum, timeLeft;
    private int levelEndX, levelEndResetX;
    private int levelOffset = 0;
    private boolean paused = false;
    // Game state related fields
    private boolean fade = false;
    private int barFade = 0, barFadeAddition = 5;
    private int fadeInt = 0, fadeChange = 5;
    private boolean levelEnding, pointsGiven;
    private int endScreenFrames, bonusPoints;
    // Game Images
    private Image enemyHealthBar;
    private Image staminaBar;
    private Image healthBar;
    private Background background;
    // Game Sounds
    private Sound levelMusic = new Sound("Assets/Sounds/Music/level1.wav", 80);
    private Sound castSound = new Sound("Assets/Sounds/Effects/cast.wav", 80);
    private Sound castHitSound = new Sound("Assets/Sounds/Effects/castHit.wav", 80);
    private Sound[] swordSounds = {new Sound("Assets/Sounds/Effects/sword1.wav", 80),
                                   new Sound("Assets/Sounds/Effects/sword2.wav", 80),
                                   new Sound("Assets/Sounds/Effects/sword3.wav", 80)};
    private Sound[] hitSounds = {new Sound("Assets/Sounds/Effects/hit1.wav", 80),
                                 new Sound("Assets/Sounds/Effects/hit2.wav", 80),
                                 new Sound("Assets/Sounds/Effects/hit3.wav", 80)};
    private Sound pauseSound = new Sound("Assets/Sounds/Effects/pause.wav", 80);
    // ArrayLists that hold game objects
    private ArrayList<LevelProp> platforms = new ArrayList<>();
    private ArrayList<LevelProp> noCollideProps = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private ArrayList<Chest> chests = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<IndicatorText> indicatorText = new ArrayList<>();
    // Fonts
    private Font gameFont;
    private Font gameFontBig;
    private Font gameFontSmall;
    //Composite
    private Composite comp;
    // Constructor for GamePanel
    public GamePanel(MainGame game){
        // Setting up the GamePanel
        gameFrame = game;
        setSize(960,590);
        keysPressed = new boolean[KeyEvent.KEY_LAST+1];
        addKeyListener(this);
        try{
            // Loading Images
            enemyHealthBar = ImageIO.read(new File("Assets/Images/Enemies/healthBar.png"));
            staminaBar = ImageIO.read(new File("Assets/Images/Player/staminaBar.png"));
            healthBar = ImageIO.read(new File("Assets/Images/Player/healthBar.png"));
            // Loading fonts
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/Fonts/8BitFont.ttf"));
            gameFont = gameFont.deriveFont(30f);
            gameFontBig = gameFont.deriveFont(50f);
            gameFontSmall = gameFont.deriveFont(25f);
        }
        catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        // Initalizing the game Classes
        Slime.init();
        Skeleton.init();
        Ghost.init();
        Projectile.init();
        Chest.init();
        Item.init();
    }

    // Method to load up all level Objects from the corresponding text files
    public void loadLevel(int levelNum){
        this.levelNum = levelNum;
        // Emptying previous values
        platforms.clear();
        noCollideProps.clear();
        enemies.clear();
        chests.clear();
        projectiles.clear();
        items.clear();
        levelEnding = false; pointsGiven = false; endScreenFrames = 0; paused = false;
        try{
            // Setting up level fields
            ArrayList<String> levelData = Utilities.loadFile("LevelData.txt", levelNum);
            timeLeft = Integer.parseInt(levelData.get(0));
            levelEndX =  Integer.parseInt(levelData.get(1));
            levelEndResetX =  Integer.parseInt(levelData.get(2));
            levelMusic.closeSound();
            levelMusic = new Sound("Assets/Sounds/Music/" + levelData.get(3), 80);
            background = new Background(levelData.get(4), levelData.get(5));
            // Loading Game-Object Arrays
            for(String data: Utilities.loadFile("disappearingPlatforms.txt", levelNum)){
                platforms.add(new LevelProp(data));
            }
            for(String data: Utilities.loadFile("Platforms.txt", levelNum)){
                platforms.add(new LevelProp(data));
            }
            for(String data: Utilities.loadFile("NoCollideProps.txt", levelNum)){
                noCollideProps.add(new LevelProp(data));
            }
            for(String data: Utilities.loadFile("Slimes.txt", levelNum)){
                enemies.add(new Slime(data));
            }
            for(String data: Utilities.loadFile("Skeletons.txt", levelNum)){
                enemies.add(new Skeleton(data));
            }
            for(String data: Utilities.loadFile("Ghosts.txt", levelNum)){
                enemies.add(new Ghost(data));
            }
            for(String data: Utilities.loadFile("Chests.txt", levelNum)){
                chests.add(new Chest(data));
            }
        }
        catch (IOException e) {
            System.out.println("Level " + levelNum + " data incomplete!");
            e.printStackTrace();
        }
        // Resetting the Player
        if(player.isDead()){
            player.restoreHealth();
        }
        player.resetPos(0 ,366);
        // Resetting music
        levelMusic.play();
        if(Sound.isMuted()){
            levelMusic.forceMute();
        }
        fade = true; fadeChange = -3; fadeInt = 255; // Starting the fade in
    }

    // All window related methods
    public void addNotify() {
        super.addNotify();
        requestFocus();
        System.out.println("add notfiy");
    }
    public void removeNotify(){
        super.removeNotify();
        paused = true;
        System.out.println("remove notify");
    }
    public void paintComponent(Graphics g){
        // Setting up Graphics2D
        Graphics2D g2d = (Graphics2D) g;
        if(comp == null){
            comp = g2d.getComposite();
        }
        // Drawing the background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 960, 590);
        background.draw(g);
        // Drawing the level
        //g.setColor(Color.WHITE);
        for(LevelProp platform: platforms){
            if(platform.getRect().x + platform.getRect().width - levelOffset > 0 && platform.getRect().x - levelOffset < 960){
                Rectangle platformRect = platform.getRect();
                if(platform.isTemporary()){
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, platform.getSpriteAlpha()));
                    g2d.drawImage(platform.getPropImage(), platformRect.x - levelOffset, platformRect.y, this);
                    g2d.setComposite(comp);
                }
                else{
                    g.drawImage(platform.getPropImage(), platformRect.x - levelOffset, platformRect.y, this);

                }
                //g.drawRect(platformRect.x -levelOffset,platformRect.y,(int)platformRect.getWidth(),(int)platformRect.getHeight());
            }
        }
        for(LevelProp prop: noCollideProps){
            Rectangle propRect = prop.getRect();
            g.drawImage(prop.getPropImage(), propRect.x - levelOffset, propRect.y, this);
        }
        // Drawing chests
        for(Chest chest: chests){
            g.drawImage(chest.getSprite(), chest.getHitbox().x-levelOffset,chest.getHitbox().y, this);
            //g.drawRect(chest.getHitbox().x-levelOffset,chest.getHitbox().y, chest.getHitbox().width, chest.getHitbox().height);
        }
        // Drawing enemies
        for(Enemy enemy: enemies){
            if(enemy.getHitbox().x + enemy.getHitbox().width - levelOffset > 0 && enemy.getHitbox().x - levelOffset < 960){
                if(enemy.hasAlphaSprites() && !enemy.isDying()){
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, enemy.getSpriteAlpha()));
                    g2d.drawImage(enemy.getSprite(), (int)enemy.getX() - levelOffset, (int)enemy.getY(), this);
                    g2d.setComposite(comp);
                }
                else{
                    g.drawImage(enemy.getSprite(), (int)enemy.getX() - levelOffset, (int)enemy.getY(), this);
                }
                drawHealth(g, enemy);
                //g.drawRect(enemy.getHitbox().x - levelOffset, enemy.getHitbox().y, enemy.getHitbox().width, enemy.getHitbox().height);
            }
        }
        // Drawing Projectiles
        for(Projectile projectile: projectiles){
            g.drawImage(projectile.getSprite(),(int)projectile.getX()-levelOffset, (int)projectile.getY(),this);
            //g.drawRect(projectile.getHitbox().x-levelOffset,projectile.getHitbox().y,projectile.getHitbox().width,projectile.getHitbox().height);
        }
        // Drawing items
        g.setColor(Color.RED);
        for(Item item: items){
            g.drawImage(item.getSprite(), item.getHitbox().x - levelOffset, item.getHitbox().y, this);
        }
        // Drawing indicator text
        g.setFont(gameFont);
        for(IndicatorText text: indicatorText){
            g.setColor(text.getColor());
            g.drawString(text.getString(), text.getX() - levelOffset, text.getY());

        }
        // Drawing the Player
        g.drawImage(player.getSprite(), (int)player.getX() - levelOffset, (int)player.getY(), this);
        //g.drawRect(player.getHitbox().x - levelOffset, player.getHitbox().y, player.getHitbox().width, player.getHitbox().height);
        //g.drawRect(player.getAttackBox().x - levelOffset, player.getAttackBox().y, player.getAttackBox().width, player.getAttackBox().height);
        // Drawing game stats
        /*Fills in both of the stat bars from darker shades to lighter shades by increasing the respective rgb value by 1 while shifting the
        rectangle over each time. */
        for(int i = 0; i < 100; i++) {
            //Health
            g.setColor(new Color(155 + i, 0, 0));//Changing colour
            g.fillRect(59+i, 30, (int) (((double) player.getHealth() / player.getMaxHealth()) * 198)-i, 14);
            if(player.getHealthTimer()>0){
                g.setColor(new Color(255,20,100+i, barFade));
                g.fillRect(59+i, 30, (int) (((double) player.getHealth() / player.getMaxHealth()) * 198)-i, 14);
            }
            //Stamina
            g.setColor(new Color(0, 155 + i, 0));
            g.fillRect(59 + i, 83, (int) ((player.getStamina() / player.getMaxStamina()) * 198) - i, 14);
            if(player.getEnergyTime()>0) {
                g.setColor(new Color(155 + i, 155 + i, 0, barFade));
                g.fillRect(59 + i, 83, (int) ((player.getStamina() / player.getMaxStamina()) * 198) - i, 14);
            }
        }
        if(player.getHealthTimer() > 0){
            g.setColor(new Color(255,20,200));
            g.drawString(""+player.getHealthTimer(),267,41);
        }
        if(player.getEnergyTime() > 0){
            g.setColor(new Color(255 , 255 , 0));
            g.drawString("" + player.getEnergyTime(), 267, 96);
        }
        // Drawing the stats
        g.setColor(Color.BLACK);
        g.drawImage(healthBar, 10,10,this);
        g.drawImage(staminaBar, 10,65,this);
        g.drawString("Time: "+timeLeft,800,20);
        g.setFont(gameFontSmall);
        g.drawString("Points: "+ player.getPoints(),70,68);
        if(Sound.isMuted()){
            g.drawString("Sound muted", 600, 20);
        }
        // Drawing various special screens
        if(levelEnding){
            drawEnding(g);
        }
        if(paused){
            drawPause(g);
        }
        if(fade){
            drawFade(g);
        }
    }
    public void drawPause(Graphics g){
        g.setColor(new Color(0,0,0, 100));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(gameFont);
        g.drawString("Press ESC to unpause", 335, 330);
        g.setFont(gameFontBig);
        g.drawString("Paused", 400, 300);
    }
    public void drawFade(Graphics g){
        g.setColor(new Color(0, 0, 0, fadeInt));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    public void drawHealth(Graphics g, Enemy enemy){
        double health = enemy.getHealth();
        double maxHealth = enemy.getMaxHealth();
        // Using Graphics inputted to draw the bar
        if(health != maxHealth){ // Only drawing if they have lost health
            Rectangle hitBox = enemy.getHitbox();
            int healthBarOffset = ((100-hitBox.width)/8);
            g.setColor(Color.RED);
            g.fillRect(hitBox.x-levelOffset-healthBarOffset,hitBox.y-10,(int)((health/maxHealth)*88),13);
            g.drawImage(enemyHealthBar,hitBox.x-levelOffset-13-healthBarOffset,hitBox.y-15,this);
        }
    }
    public void drawEnding(Graphics g){
        g.setFont(gameFontBig);
        g.setColor(Color.BLACK);
        g.drawString("Level Complete!", 300, 180);
        if(endScreenFrames > 200){
            g.setFont(gameFont);
            g.drawString("Time bonus: " + bonusPoints, 375, 200);

        }
    }
    // Keyboard related methods
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(keyCode > keysPressed.length){
            return; // If the key entered is unrecognized, ignore this call of keyPressed
        }
        // Running code for initially clicked keys
        if(!keysPressed[keyCode]){
            if(keyCode == KeyEvent.VK_M){
                Sound.toggleVolume();
            }
            else if(keyCode == KeyEvent.VK_ESCAPE){
                paused = !paused;
                if(paused){
                    Sound.pauseAll();
                    pauseSound.stop(); pauseSound.play();
                }
                else{
                    Sound.resumeAll();
                }
                repaint();
            }
            else if(!paused && !levelEnding){
                if(keyCode == KeyEvent.VK_SPACE){
                    player.jump(Player.INITIAL);
                }
                else if(keyCode == KeyEvent.VK_O){
                    player.attack();
                }
                else if(keyCode == KeyEvent.VK_P){
                    player.castMagic();
                }
            }
        }
        // SOUND TEST
        if(keyCode == KeyEvent.VK_0){
            if(!levelMusic.hasStarted()){
                levelMusic.play();
            }
            else if(levelMusic.isPlaying()) {
                levelMusic.stop();
            }
            else{
                levelMusic.resume();
            }
        }
        // Keeping track of whether or not the key is pressed down
        keysPressed[keyCode] = true;
        // DEBUG KEYS (REMOVE THESE AFTER)
        if(keyCode == KeyEvent.VK_BACK_SLASH){
            if(getMousePosition() != null){
                System.out.println(getMousePosition() + " True x = " + (getMousePosition().x + levelOffset));
            }
        }
        else if(keyCode == KeyEvent.VK_CLOSE_BRACKET){
            player.resetPos(0,366);
        }
        else if(keyCode == KeyEvent.VK_OPEN_BRACKET){
            player.resetPos(getMousePosition().x + levelOffset - 50, getMousePosition().y);
        }
        else if(keyCode == KeyEvent.VK_SEMICOLON){
            loadLevel(levelNum);
            System.out.println("Level reloaded");
        }
        else if(keyCode == KeyEvent.VK_5){
            player.setPos(9000,100);
            fade=true;
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() > keysPressed.length){
            return; // If the key entered is unrecognized, ignore this call of keyReleased
        }
        keysPressed[e.getKeyCode()] = false;
    }
    @Override
    public void keyTyped(KeyEvent e) {}

    // Game related methods
    public void update(){
        // Updating all Objects
        player.update();
        indicatorText.addAll(player.flushTextQueue());
        for(Enemy enemy: enemies){
            if(enemy.isActive()){
                enemy.update(player);
            }
        }
        for(Projectile projectile: projectiles){
            projectile.update();
        }
        for(Item item: items){
            item.update();
        }
        for(IndicatorText text: indicatorText){
            text.update();
        }
        for(LevelProp platform: platforms){
            checkDisappearing(platform);
        }
        // Updating object
        checkPlayerAction();
        collectGarbage();
    }
    public void updateGraphics(){
        changeFade();
        calculateOffset();
        background.update(levelOffset);
        if(levelEnding){
            updateLevelEnd();
        }
    }
    public void changeFade(){
        if(fade){
            fadeInt+= fadeChange;
            if(fadeInt==255){
                levelMusic.stop();
                gameFrame.switchPanel(MainGame.SHOPPANEL);
            }
            else if(fadeInt==0){
                fade=false;
            }
        }
        // Allowing the powerup fade to continue
        barFade += barFadeAddition;
        if(barFade == 0 || barFade == 255){
            barFadeAddition *= -1;
        }
    }
    public void calculateOffset(){
        Rectangle hitbox = player.getHitbox();
        if(hitbox.x + hitbox.width > 480){
            levelOffset = (hitbox.x + hitbox.width) - 480;
        }
        else{
            levelOffset = 0;
        }
    }
    public void updateLevelEnd(){
        endScreenFrames++;
        if(endScreenFrames > 200){
            if(timeLeft > 0){
                timeLeft--;
                bonusPoints += 10;
            }
            else if(!pointsGiven){
                pointsGiven = true;
                player.addPoints(bonusPoints);
                indicatorText.add(new IndicatorText((int)player.getX(),(int)player.getY(), "+"+bonusPoints, Color.YELLOW));
            }
        }
        if(endScreenFrames == 600){
            fade = true; fadeChange = 1; fadeInt = 0;
        }
    }
    public void checkDisappearing(LevelProp platform){
        if(platform.isTemporary() && player.getX() > platform.getDisappearX() && player.getY() > platform.getDisappearY()){
            platform.disappear();
        }
    }
    public void checkCollision(){
        // Checking collision with Level platforms
        for(LevelProp platform: platforms){
            Rectangle platformRect = platform.getRect();
            player.checkCollision(platformRect);
            for(Enemy enemy: enemies){
                enemy.checkCollision(platformRect);
            }
            for(Item item: items){
                item.checkCollision(platformRect);
            }
        }
        // Checking projectile collision
        for(Projectile projectile:projectiles){
            for(Enemy enemy:enemies) {
                if(!enemy.isDying() && !projectile.isExploding() && enemy.getHitbox().intersects(projectile.getHitbox())){
                    double damageDone = enemy.castHit(projectile);
                    damageDone = Utilities.roundOff(damageDone, 1);
                    player.addPoints((int)projectile.getDamage());
                    projectile.explode();
                    castHitSound.stop(); castHitSound.play();
                    indicatorText.add(new IndicatorText(enemy.getHitbox().x, enemy.getHitbox().y, "-" + damageDone, Color.ORANGE));

                }
            }
        }
        //Checking item collision
        Rectangle playerHitbox = player.getHitbox();
        for(Item item: items){
            if(item.getHitbox().intersects(playerHitbox) && item.isSettled()) {
                player.gainItem(item);
                item.use();
                item.playSound();
                if(item.getType() == Item.HEALTH){
                    indicatorText.add(new IndicatorText(playerHitbox.x, playerHitbox.y, "+10", Color.GREEN));
                }
                else if(item.getType() == Item.COIN){
                    indicatorText.add(new IndicatorText(playerHitbox.x, playerHitbox.y, "+100", Color.YELLOW));
                }
            }
        }
        // Checking chest collision
        for(Chest chest: chests){
            Rectangle chestHitbox = chest.getHitbox();
            if(chest.isClosed() && playerHitbox.intersects(chestHitbox) && (playerHitbox.y + playerHitbox.height) == (chestHitbox.y + chestHitbox.height)){
                chest.open();
                for(int i = 0; i < chest.getQuantity(); i++){
                    items.add(new Item(chest));
                }
            }
        }
        // Checking if the player has reached the end of the level
        if(playerHitbox.x > levelEndX){
            levelEnding = true;
            background.ignoreNegative();
        }
        if(levelEnding && playerHitbox.x > levelEndResetX){
            int overshoot = levelEndResetX - playerHitbox.x;
            player.setPos(levelEndX + overshoot, (int)player.getY());
            calculateOffset();
        }
    }
    public void checkPlayerAction(){
        // Checking if the Player has used their sword attack
        if(player.isAttackFrame()){ // Checking if this is the frame where attacks land
            int randomSwordIndex = Utilities.randint(0,2);
            swordSounds[randomSwordIndex].stop();
            swordSounds[randomSwordIndex].play(); // Playing the sword sound effect
            // Going through each enemy and checking for collisions
            for(Enemy enemy:enemies){
                if(player.getAttackBox().intersects(enemy.getHitbox()) && !enemy.isDying()){
                    double damageDone = enemy.swordHit(player);
                    damageDone = Utilities.roundOff(damageDone, 1);
                    player.addPoints(player.getSwordDamage());
                    indicatorText.add(new IndicatorText(enemy.getHitbox().x, enemy.getHitbox().y, "-" + damageDone, Color.ORANGE));
                    int randomHitIndex = Utilities.randint(0,2);
                    hitSounds[randomHitIndex].stop();
                    hitSounds[randomHitIndex].play(); // Playing the hit effect
                }
            }
        }
        // Checking if the Player has cast
        if(player.isCastFrame()){
            Rectangle hitBox = player.getHitbox();
            Rectangle attackBox = player.getAttackBox();
            int speed = -5;
            int xPos = attackBox.x;
            if(player.getDirection() == Player.RIGHT){
                speed = -speed;
                xPos -= 150;
            }
            projectiles.add(new Projectile(Projectile.PLAYER, xPos,hitBox.y+hitBox.height/2.0-5,player.getSpellDamage(),speed));
            castSound.play();
        }
        // Check if the player is dead
        if(player.isDead() && !fade){
            fade = true; fadeChange = 1; fadeInt = 0;
        }
        if(player.getHitbox().y > getHeight()){
            player.kill();
        }
    }
    public void collectGarbage(){
        // Using removeIf for Arrays that only need removal of items
        projectiles.removeIf(Projectile::isDoneExploding);
        items.removeIf(item -> (item.isUsed() || item.getHitbox().y > this.getHeight()));
        platforms.removeIf(LevelProp::isDoneDisappearing);
        indicatorText.removeIf(IndicatorText::isDone);

        // Using for loops for Arrays that need to keep track of removals
        for(int i = enemies.size() - 1; i >= 0; i--){
            Enemy enemy = enemies.get(i);
            if(enemy.isDead() || enemy.getY() > this.getHeight()){
                enemies.remove(i);
                player.addPoints(100);
                indicatorText.add(new IndicatorText(enemy.getHitbox().x, enemy.getHitbox().y, "+100", Color.YELLOW));
            }
        }
    }
    public void checkInputs(){
        if(levelEnding){
            player.move(Player.RIGHT);
            return;
        }
        // Side-to-side movement inputs
        if(keysPressed[KeyEvent.VK_D] && keysPressed[KeyEvent.VK_A]){
            // Do nothing
        }
        else if(keysPressed[KeyEvent.VK_D]){
            player.move(Player.RIGHT);
        }
        else if(keysPressed[KeyEvent.VK_A]){
            player.move(Player.LEFT);
        }
        // Jumping input
        if(keysPressed[KeyEvent.VK_SPACE]){
            player.jump(Player.NORMAL);
        }
    }
    public void iterateTime(){
        if(!levelEnding){
            timeLeft-=1;
        }
        player.iterateTime();
    }

    // Getter methods
    public Player getPlayer(){
        return player;
    }
    public boolean isPaused(){
        return paused;
    }
}