import java.util.Arrays;
import java.util.Random;

public class Tetromino {
    enum Tetrominoes {
        NO_BLOCK, Z_SHAPE, S_SHAPE, I_SHAPE, T_SHAPE, O_SHAPE, L_SHAPE, J_SHAPE
    }

    private Tetrominoes tetrominoes;
    private final int[][] coords;
    private final int[][][] tetrominoTable;

    public Tetromino() {
        coords = new int[4][2];
        tetrominoTable = new int[][][] {
                { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } },
                { { 0, -1 }, { 0, 0 }, { -1, 0 }, { -1, 1 } },
                { { 0, -1 }, { 0, 0 }, { 1, 0 }, { 1, 1 } },
                { { 0, -1 }, { 0, 0 }, { 0, 1 }, { 0, 2 } },
                { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, 1 } },
                { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } },
                { { -1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } },
                { { 1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } }
        };
        setShape(Tetrominoes.NO_BLOCK);
    }

    public void setShape(Tetrominoes tetromino) {
        for (int i = 0; i < coords.length; i++) {
            System.arraycopy(tetrominoTable[tetromino.ordinal()][i], 0, coords[i], 0, coords[i].length);
        }
        tetrominoes = tetromino;
    }

    public void setRandomShape() {
        Random r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;
        setShape(Tetrominoes.values()[x]);
    }

    public Tetrominoes getShape() {
        return tetrominoes;
    }

    public void setX(int idx, int x) {
        coords[idx][0] = x;
    }

    public void setY(int idx, int y) {
        coords[idx][1] = y;
    }

    public int getX(int idx) {
        return coords[idx][0];
    }

    public int getY(int idx) {
        return coords[idx][1];
    }

    public int minX() {
        return Arrays.stream(coords).mapToInt(coord -> coord[0]).min().orElse(0);
    }

    public int minY() {
        return Arrays.stream(coords).mapToInt(coord -> coord[1]).min().orElse(0);
    }

    public Tetromino rotateLeft() {
        if (tetrominoes == Tetrominoes.O_SHAPE) {
            return this;
        }

        Tetromino ret = new Tetromino();
        ret.tetrominoes = tetrominoes;

        for (int i = 0; i < coords.length; i++) {
            ret.setX(i, getY(i));
            ret.setY(i, -getX(i));
        }

        return ret;
    }

    public Tetromino rotateRight() {
        if (tetrominoes == Tetrominoes.O_SHAPE) {
            return this;
        }

        Tetromino ret = new Tetromino();
        ret.tetrominoes = tetrominoes;

        for (int i = 0; i < coords.length; i++) {
            ret.setX(i, -getY(i));
            ret.setY(i, getX(i));
        }

        return ret;
    }
}
