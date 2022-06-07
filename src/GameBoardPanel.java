import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

public class GameBoardPanel extends JPanel implements ActionListener {

    private static final int BoardWidth = 10;
    private static final int BoardHeight = 22;

    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font FONT = new Font(Font.SERIF, Font.PLAIN, 28);

    private final Timer timer;
    private boolean isFallingDone = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int currentScore = 0;

    private int curX = 0;
    private int curY = 0;

    private Tetromino curBlock;

    private final Tetromino.Tetrominoes[] gameBoard;
    private static final Color[] colorTable = new Color[Tetromino.Tetrominoes.values().length];

    private int currentTimerResolution;

    private final Map<Integer, Runnable> keyBindings;

    public GameBoardPanel(int timerResolution) {
        setFocusable(true);
        setBackground(new Color(0, 30, 30));
        curBlock = new Tetromino();
        timer = new Timer(timerResolution, this);
        timer.start();
        currentTimerResolution = timerResolution;

        gameBoard = new Tetromino.Tetrominoes[BoardWidth * BoardHeight];

        colorTable[Tetromino.Tetrominoes.I_SHAPE.ordinal()] = Color.CYAN;
        colorTable[Tetromino.Tetrominoes.J_SHAPE.ordinal()] = Color.BLUE;
        colorTable[Tetromino.Tetrominoes.L_SHAPE.ordinal()] = Color.ORANGE;
        colorTable[Tetromino.Tetrominoes.O_SHAPE.ordinal()] = Color.YELLOW;
        colorTable[Tetromino.Tetrominoes.S_SHAPE.ordinal()] = Color.GREEN;
        colorTable[Tetromino.Tetrominoes.T_SHAPE.ordinal()] = Color.MAGENTA;
        colorTable[Tetromino.Tetrominoes.Z_SHAPE.ordinal()] = Color.RED;

        keyBindings = new HashMap<>();
        keyBindings.put(KeyEvent.VK_A, () -> isMovable(curBlock, curX - 1, curY));
        keyBindings.put(KeyEvent.VK_D, () -> isMovable(curBlock, curX + 1, curY));
        keyBindings.put(KeyEvent.VK_W, () -> isMovable(curBlock.rotateRight(), curX, curY));
        keyBindings.put(KeyEvent.VK_S, this::advanceOneLine);
        keyBindings.put(KeyEvent.VK_SPACE, this::advanceToEnd);
        keyBindings.put(KeyEvent.VK_P, this::pause);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isStarted || curBlock.getShape() == Tetromino.Tetrominoes.NO_BLOCK) {
                    return;
                }
                int keycode = e.getKeyCode();
                if (keycode == 'p' || keycode == 'P') {
                    pause();
                    return;
                }
                if (isPaused) {
                    return;
                }
                Runnable action = keyBindings.get(keycode);
                if (action != null) {
                    action.run();
                }
            }
        });
    }

    private void setResolution() {
        currentTimerResolution = 370 - (currentScore / 10) * 30;
        timer.setDelay(currentTimerResolution);
    }

    private void initBoard() {
        Arrays.fill(gameBoard, Tetromino.Tetrominoes.NO_BLOCK);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingDone) {
            isFallingDone = false;
            newTetromino();
        } else {
            advanceOneLine();
        }
    }

    public void start() {
        if (isPaused) {
            return;
        }
        isStarted = true;
        isFallingDone = false;
        currentScore = 0;
        initBoard();

        newTetromino();
        timer.setInitialDelay(3000);
        timer.start();
    }

    public void pause() {
        if (!isStarted) {
            return;
        }

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }

        repaint();
    }

    private int blockWidth() {
        return (int) getSize().getWidth() / BoardWidth;
    }

    private int blockHeight() {
        return (int) getSize().getHeight() / BoardHeight;
    }

    Tetromino.Tetrominoes curTetrominoPos(int x, int y) {
        return gameBoard[(y * BoardWidth) + x];
    }

    @Override
    public void paint(Graphics g) {

        int blockW = blockWidth();
        int blockH = blockHeight();

        super.paint(g);

        StringBuilder sb = new StringBuilder();
        if (!isPaused) {
            sb.append("Score: ").append(currentScore).append("  Level: ").append(currentScore / 10 + 1);
        } else {
            sb.append("PAUSED");
        }

        g.setColor(TEXT_COLOR);
        g.setFont(FONT);
        g.drawString(sb.toString(), 15, 35);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BoardHeight * blockH;

        // rendering - shadow of tetromino
        int tempY = curY;
        while (tempY > 0) {
            if (!atomIsMovable(curBlock, curX, tempY - 1, false))
                break;
            tempY--;
        }
        for (int i = 0; i < 4; i++) {
            int x = curX + curBlock.getX(i);
            int y = tempY - curBlock.getY(i);
            drawTetromino(g, x * blockW, boardTop + (BoardHeight - y - 1) * blockH, curBlock.getShape(),
                    true);
        }

        // rendering - game board
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {
                Tetromino.Tetrominoes shape = curTetrominoPos(j, BoardHeight - i - 1);
                if (shape != Tetromino.Tetrominoes.NO_BLOCK)
                    drawTetromino(g, j * blockW, boardTop + i * blockH, shape, false);
            }
        }

        // rendering - current tetromino
        if (curBlock.getShape() != Tetromino.Tetrominoes.NO_BLOCK) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curBlock.getX(i);
                int y = curY - curBlock.getY(i);
                drawTetromino(g, x * blockW, boardTop + (BoardHeight - y - 1) * blockH,
                        curBlock.getShape(), false);
            }
        }

    }

    private void drawTetromino(Graphics g, int x, int y, Tetromino.Tetrominoes bs, boolean isShadow) {
        Color curColor = colorTable[bs.ordinal()];

        g.setColor(isShadow ? curColor.darker().darker() : curColor);
        g.fillRect(x + 1, y + 1, blockWidth() - 2, blockHeight() - 2);
    }

    private void removeFullLines() {
        int fullLines = 0;

        for (int i = BoardHeight - 1; i >= 0; i--) {
            boolean isFull = true;

            for (int j = 0; j < BoardWidth; j++) {
                if (curTetrominoPos(j, i) == Tetromino.Tetrominoes.NO_BLOCK) {
                    isFull = false;
                    break;
                }
            }

            if (isFull) {
                ++fullLines;
                for (int k = i; k < BoardHeight - 1; k++) {
                    for (int l = 0; l < BoardWidth; ++l)
                        gameBoard[(k * BoardWidth) + l] = curTetrominoPos(l, k + 1);
                }
            }
        }

        if (fullLines > 0) {
            currentScore += fullLines;
            isFallingDone = true;
            curBlock.setShape(Tetromino.Tetrominoes.NO_BLOCK);
            setResolution();
            repaint();
        }

    }

    // true - actual tetromino pos
    // flase - shadow pos
    private boolean atomIsMovable(Tetromino chkBlock, int chkX, int chkY, boolean flag) {
        for (int i = 0; i < 4; i++) {
            int x = chkX + chkBlock.getX(i);
            int y = chkY - chkBlock.getY(i);
            if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight)
                return false;
            if (curTetrominoPos(x, y) != Tetromino.Tetrominoes.NO_BLOCK) {
                return false;
            }
        }

        if (flag) {
            curBlock = chkBlock;
            curX = chkX;
            curY = chkY;
            repaint();
        }

        return true;
    }

    private boolean isMovable(Tetromino chkBlock, int chkX, int chkY) {
        return atomIsMovable(chkBlock, chkX, chkY, true);
    }

    private void newTetromino() {
        curBlock.setRandomShape();
        curX = BoardWidth / 2 + 1;
        curY = BoardHeight - 1 + curBlock.minY();

        if (!isMovable(curBlock, curX, curY)) {
            curBlock.setShape(Tetromino.Tetrominoes.NO_BLOCK);
            timer.stop();
            isStarted = false;
            GameOver(currentScore);
        }
    }

    private void tetrominoFixed() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curBlock.getX(i);
            int y = curY - curBlock.getY(i);
            gameBoard[(y * BoardWidth) + x] = curBlock.getShape();
        }

        removeFullLines();

        if (!isFallingDone) {
            newTetromino();
        }
    }

    private void advanceOneLine() {
        if (!isMovable(curBlock, curX, curY - 1)) {
            tetrominoFixed();
        }
    }

    private void advanceToEnd() {
        int tempY = curY;
        while (tempY > 0) {
            if (!isMovable(curBlock, curX, tempY - 1))
                break;
            --tempY;
        }
        tetrominoFixed();
    }

    private void GameOver(int dbScore) {
        int maxScore = readDB();
        String showD;
        if (dbScore > maxScore) {
            writeDB(dbScore);
            showD = String.format("%nCongratulations! %nNew max score: %d", dbScore);
        } else {
            showD = String.format("Score: %d %nMax score: %d", dbScore, maxScore);
        }
        UIManager.put("OptionPane.okButtonText", "new game");
        JOptionPane.showMessageDialog(null, showD, "Game Over!", JOptionPane.ERROR_MESSAGE);
        setResolution();
        start();
    }

    private int readDB() {
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader("Tetris.score"));
            String dbMaxScore = inputStream.readLine();
            inputStream.close();
            return Integer.parseInt(dbMaxScore);
        } catch (IOException | NumberFormatException e) {
            return -1;
        }
    }

    private void writeDB(int dbScore) {
        try {
            File UIFile = new File("Tetris.score");
            if (!UIFile.exists()) {
                UIFile.createNewFile();
            }
            FileWriter filewriter = new FileWriter(UIFile.getAbsoluteFile());
            BufferedWriter outputStream = new BufferedWriter(filewriter);
            outputStream.write(String.valueOf(dbScore));
            outputStream.newLine();
            outputStream.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
