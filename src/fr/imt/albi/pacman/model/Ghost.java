package fr.imt.albi.pacman.model;

import fr.imt.albi.pacman.main.PacManLauncher;
import fr.imt.albi.pacman.utils.Figure;
import fr.imt.albi.pacman.utils.Food;
import fr.imt.albi.pacman.utils.GhostSkin;
import fr.imt.albi.pacman.utils.Wall;

import java.util.ArrayList;

public class Ghost extends Creature {

    public int SPEED_GHOST= 10;
    public static int SPEED_GHOST_AVG =10;
 
    public static int SPEED_GHOST_MIN = 9;
    public static int SPEED_GHOST_MAX = 11;
    public static final int GHOST_SCORE = 100;
    /* Distance (en pixels) à partir de laquelle le fantôme repère Pacman */
    private static final int CHASE_DISTANCE = 100;
    private final GhostSkin ghostSkin;
    private final String ghostColor;
    private String previousMove;
    private int counterUTurn;
    private int counterFear;
    
    

    public Ghost(int size, int x, int y, String color) {
        this.previousMove = PacManLauncher.UP;
        this.initUTurnCounter();

        this.counterFear = 0;
        this.ghostColor = color;

        this.ghostSkin = new GhostSkin(size, x, y, color);
    }

    public void move(Pacman pacman) {
        if (this.counterFear == 0) {
            this.setNormalState();
        }
        
       
        if (this.counterFear % 2 == 1 || this.counterFear == 0) {
            if (this.counterFear > 0) {
                this.counterFear--;
                
                
            }
            this.counterUTurn--;

            if (this.isNear(pacman)) {
                // Proche de Pacman : on le poursuit, ou on le fuit si on a peur
                this.chaseOrFlee(pacman, this.counterFear > 0);
            } else if (this.counterUTurn <= 0) {
            	//SPEED_GHOST=Ghost.SPEED_GHOST_AVG;
                switch (this.previousMove) {
                    case PacManLauncher.UP:
                        this.move(PacManLauncher.DOWN);
                        break;
                    case PacManLauncher.DOWN:
                        this.move(PacManLauncher.UP);
                        break;
                    case PacManLauncher.LEFT:
                        this.move(PacManLauncher.RIGHT);
                        break;
                    case PacManLauncher.RIGHT:
                        this.move(PacManLauncher.LEFT);
                        break;
                }
                this.initUTurnCounter();
            } else {
                checkCrossing(this.previousMove);
            }
        } else {
            this.counterFear--;
        }
    }

    private boolean isNear(Pacman pacman) {
        return Math.abs(this.getX() - pacman.getX()) < CHASE_DISTANCE
                && Math.abs(this.getY() - pacman.getY()) < CHASE_DISTANCE;
    }

    /**
     * Choisit une seule direction par tour, vers Pacman (ou à l'opposé s'il faut
     * fuir), en évitant les murs quand on est sur une intersection.
     */
    private void chaseOrFlee(Pacman pacman, boolean flee) {
        int dx = pacman.getX() - this.getX();
        int dy = pacman.getY() - this.getY();
        if (flee) {
            dx = -dx;
            dy = -dy;
        }
        String horizontal = dx < 0 ? PacManLauncher.LEFT : PacManLauncher.RIGHT;
        String vertical = dy < 0 ? PacManLauncher.UP : PacManLauncher.DOWN;

        String[] wanted;
        if (Math.abs(dx) >= Math.abs(dy)) {
            wanted = new String[] { horizontal, vertical };
        } else {
            wanted = new String[] { vertical, horizontal };
        }

        for (String direction : wanted) {
            if (this.isFree(direction)) {
                this.move(direction);
                return;
            }
        }
        this.checkCrossing(this.previousMove);
    }

    private boolean isFree(String direction) {
        int sizeCase = this.gameMap.getSizeCase();
        if (this.getX() % sizeCase != 0 || this.getY() % sizeCase != 0) {
            // Entre deux cases, on ne peut que continuer ou faire demi-tour
            return direction.equals(this.previousMove);
        }
        Figure[][] map = this.gameMap.getMap();
        int[] position = this.getColumnAndRow();
        int x = position[0];
        int y = position[1];
        switch (direction) {
            case PacManLauncher.UP:
                return !(map[y - 1][x] instanceof Wall);
            case PacManLauncher.DOWN:
                return !(map[y + 1][x] instanceof Wall);
            case PacManLauncher.LEFT:
                return !(map[y][x - 1] instanceof Wall);
            case PacManLauncher.RIGHT:
                return !(map[y][x + 1] instanceof Wall);
            default:
                return false;
        }
    }

    public void initUTurnCounter() {
        this.counterUTurn = (int) (Math.random() * 30) + 20;
    }

    public void move(String direction) {
        this.previousMove = direction;
        int xMove = 0;
        int yMove = 0;

        int[] crossMap = this.navigateInMap(direction);
        xMove = crossMap[0];
        yMove = crossMap[1];

        crossMap = this.checkCollision(direction, xMove, yMove);
        xMove = crossMap[0];
        yMove = crossMap[1];

        this.move(xMove, yMove);
    }

    public void move(int dx, int dy) {
        for (Figure figure : this.getSkin()) {
            figure.move(dx, dy);
        }
    }

    private Figure[] getSkin() {
        return this.ghostSkin.getFigures();
    }

    public void setFearState() {
        this.counterFear = 60;
        Figure[] figures = this.getSkin();
        for (int i = 0; i < 5; i++) {
            figures[i].setColor("blue");
        }
    }

    public void setNormalState() {
        this.counterFear = 0;
        Figure[] figures = this.getSkin();
        for (int i = 0; i < 5; i++) {
            figures[i].setColor(this.ghostColor);
        }
    }

    public int getX() {
        return this.ghostSkin.getX();
    }

    public int getY() {
        return this.ghostSkin.getY();
    }

    public int getWidth() {
        return this.ghostSkin.getWidth();
    }

    public int getSpeed() {
        return SPEED_GHOST;
    }

    public int getFearCounter() {
        return this.counterFear;
    }

    public void checkCrossing(String toward) {
        boolean haveMoved = false;
        Figure[][] map = this.gameMap.getMap();

        if (this.getX() % this.gameMap.getSizeCase() == 0 && this.getY() % this.gameMap.getSizeCase() == 0) {
            int[] position = this.getColumnAndRow();
            int xPos = position[0];
            int yPos = position[1];

            Figure fUp = map[yPos - 1][xPos];
            Figure fDown = map[yPos + 1][xPos];
            Figure fLeft = map[yPos][xPos - 1];
            Figure fRight = map[yPos][xPos + 1];

            ArrayList<Figure> caseAround = new ArrayList<Figure>();
            caseAround.add(fUp);
            caseAround.add(fDown);
            caseAround.add(fLeft);
            caseAround.add(fRight);

            switch (toward) {
                case PacManLauncher.UP:
                    if (!(fLeft instanceof Wall) || !(fRight instanceof Wall)) {
                        caseAround.remove(fDown);
                        this.chooseMove(toward, caseAround, fUp, fDown, fLeft, fRight);
                        haveMoved = true;
                    } else if (fUp instanceof Wall) {
                        this.chooseMove(toward, caseAround, fUp, fDown, fLeft, fRight);
                        haveMoved = true;
                    }
                    break;
                case PacManLauncher.DOWN:
                    if (!(fLeft instanceof Wall) || !(fRight instanceof Wall)) {
                        caseAround.remove(fUp);
                        this.chooseMove(toward, caseAround, fUp, fDown, fLeft, fRight);
                        haveMoved = true;
                    } else if (fDown instanceof Wall) {
                        this.chooseMove(toward, caseAround, fUp, fDown, fLeft, fRight);
                        haveMoved = true;
                    }
                    break;
                case PacManLauncher.LEFT:
                    if (!(fUp instanceof Wall) || !(fDown instanceof Wall)) {
                        caseAround.remove(fRight);
                        this.chooseMove(toward, caseAround, fUp, fDown, fLeft, fRight);
                        haveMoved = true;
                    } else if (fLeft instanceof Wall) {
                        this.chooseMove(toward, caseAround, fUp, fDown, fLeft, fRight);
                        haveMoved = true;
                    }
                    break;
                case PacManLauncher.RIGHT:
                    if (!(fUp instanceof Wall) || !(fDown instanceof Wall)) {
                        caseAround.remove(fLeft);
                        this.chooseMove(toward, caseAround, fUp, fDown, fLeft, fRight);
                        haveMoved = true;
                    } else if (fRight instanceof Wall) {
                        this.chooseMove(toward, caseAround, fUp, fDown, fLeft, fRight);
                        haveMoved = true;
                    }
                    break;
            }
        }

        if (!haveMoved) {
            this.move(this.previousMove);
        }
    }

    public void chooseMove(String toward, ArrayList<Figure> listF, Figure fUp, Figure fDown, Figure fLeft, Figure fRight) {
        boolean result = false;
        ArrayList<Figure> toGo = new ArrayList<Figure>();

        for (Figure f : listF) {
            if (!(f instanceof Wall)) {
                toGo.add(f);
            }
        }

        Figure nextMove = null;
        double ran = Math.random() * toGo.size();
        for (int i = 0; i < toGo.size(); i++) {
            if (ran >= i && ran < i + 1) {
                nextMove = toGo.get(i);
            }
        }

        if (nextMove == null) {
            this.move(toward);
        } else if (nextMove == fUp) {
            this.move(PacManLauncher.UP);
        } else if (nextMove == fDown) {
            this.move(PacManLauncher.DOWN);
        } else if (nextMove == fLeft) {
            this.move(PacManLauncher.LEFT);
        } else if (nextMove == fRight) {
            this.move(PacManLauncher.RIGHT);
        }
    }

    public boolean checkCaseType(Figure f) {
        return (f instanceof Wall);
    }

    protected void interactWithFood(Figure[][] map, int i, int j) {
    }

    public void draw() {
        this.ghostSkin.draw();
    }
}
