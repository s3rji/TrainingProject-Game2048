import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;
    int score;
    int maxTile;


    public Model() {
        resetGameTiles();
    }

    public void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        score = 0;
        maxTile = 0;
        addTile();
        addTile();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove() {
        if (!getEmptyTiles().isEmpty()) return true;

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if ((j < FIELD_WIDTH - 1 && gameTiles[i][j].value == gameTiles[i][j + 1].value) ||
                        (i < FIELD_WIDTH - 1 && gameTiles[i][j].value == gameTiles[i + 1][j].value)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public void left() {
        if (isSaveNeeded) saveState(gameTiles);

        boolean isChange = false;
        for (Tile[] tiles : gameTiles) {
            isChange = (compressTiles(tiles) | mergeTiles(tiles));
        }
        if (isChange) addTile();

        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        rotateRightBy90Degrees();
        rotateRightBy90Degrees();
        left();
        rotateRightBy90Degrees();
        rotateRightBy90Degrees();
    }

    public void up() {
        saveState(gameTiles);
        rotateRightBy90Degrees();
        rotateRightBy90Degrees();
        rotateRightBy90Degrees();
        left();
        rotateRightBy90Degrees();
    }

    public void down() {
        saveState(gameTiles);
        rotateRightBy90Degrees();
        left();
        rotateRightBy90Degrees();
        rotateRightBy90Degrees();
        rotateRightBy90Degrees();
    }

    private void rotateRightBy90Degrees() {
        Tile[][] output = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                output[i][j] = gameTiles[FIELD_WIDTH - 1 - j][i];
            }
        }
        gameTiles = output;
    }

    private boolean compressTiles(Tile[] tiles) {
        int insertPosition = 0;
        boolean isChange = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    isChange = true;
                }
                insertPosition++;
            }
        }
        return isChange;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isChange = false;
        for (int j = 0; j < tiles.length; j++) {

            if (tiles[j].value != 0 && j < tiles.length - 1) {

                if (tiles[j].value == tiles[j + 1].value) {
                    int sum = tiles[j].value * 2;
                    score += sum;
                    if (sum > maxTile) maxTile = sum;

                    tiles[j] = new Tile(sum);
                    if (j < tiles.length - 2) {
                        System.arraycopy(tiles, j + 2, tiles, j + 1, tiles.length - j - 2);
                    }
                    tiles[tiles.length - 1] = new Tile();
                    isChange = true;
                }
            }
        }
        return isChange;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            int index = (int) (Math.random() * emptyTiles.size());
            emptyTiles.get(index).value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTiles = new ArrayList<>();
        for (Tile[] tileLine : gameTiles) {
            for (Tile tile : tileLine) {
                if (tile.isEmpty()) emptyTiles.add(tile);
            }
        }
        return emptyTiles;
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] states = new Tile[FIELD_WIDTH][FIELD_WIDTH];

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                states[i][j] = new Tile(tiles[i][j].value);
            }
        }

        previousStates.push(states);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.empty()) gameTiles = previousStates.pop();

        if (!previousScores.empty()) score = previousScores.pop();
    }

    public void randomMove() {
        int rand = ((int) (Math.random() * 100)) % 4;
        switch (rand) {
            case 0:
                left();
                break;
            case 1:
                down();
                break;
            case 2:
                right();
                break;
            case 3:
                up();
                break;
        }
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();

        MoveEfficiency moveEfficiency;
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(),
                                score, move);
        } else
            moveEfficiency = new MoveEfficiency(-1, 0, move);

        rollback();

        return moveEfficiency;
    }

    private boolean hasBoardChanged() {
        Tile[][] previouslyState = previousStates.peek();
        return !Arrays.deepEquals(gameTiles, previouslyState);
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::up));
        queue.offer(getMoveEfficiency(this::down));

        queue.poll().getMove().move();
    }
}
