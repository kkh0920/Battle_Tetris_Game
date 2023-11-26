package kr.ac.jbnu.se.tetris.board;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.util.concurrent.ThreadLocalRandom;

import javax.swing.*;

import kr.ac.jbnu.se.tetris.game.Tetris;
import kr.ac.jbnu.se.tetris.game.TetrisGameManager;
import kr.ac.jbnu.se.tetris.score.MaxScorePanel;
import kr.ac.jbnu.se.tetris.shape.BlockImage;
import kr.ac.jbnu.se.tetris.shape.Shape;
import kr.ac.jbnu.se.tetris.shape.Tetrominoes;

public class Board extends JPanel {

    protected Tetris parentTetris; 

    protected boolean isFallingFinished;
    protected boolean isStarted = false;

    protected boolean isBlockOvered = false;

    protected final int BoardWidth = 10;
    protected final int BoardHeight = 22;

    protected final int PanelWidth = 220;
    protected final int PanelHeight = 440;

    // 공격 데미지 및 hp 회복량
    protected final int HealthRecover = 9;
    protected final int AttackDamage = 15;

    // 점수
    protected int numLinesRemoved;
    
    // 타이머
    protected Timer timer;

    // 보드판
    protected Tetrominoes[][] gridBoard;

    // 현재 떨어지는 도형 / 다음에 떨어질 도형
    protected Shape curPiece;
    protected Shape nextPiece;

    // 상대편 보드
    protected Board opponent;

    public Board(Tetris parent) {
        setPreferredSize(new Dimension(PanelWidth, PanelHeight));

        this.parentTetris = parent;

        gridBoard = new Tetrominoes[BoardHeight][BoardWidth];

        curPiece = new Shape();
        nextPiece = new Shape();

        isFallingFinished = true;
        numLinesRemoved = 0;
        clearBoard();

        nextPiece.setRandomShape();
    }

    /**
     * TetrisGameManager의 start 메소드에서 사용됨
     * 
     * @param opponent 대결할 상대편 보드; 해당 객체를 참조하여 
     *                  decreaseOtherHp, stackLinesToOpponent 메소드를 통해 상대방 공격
     */
    public void setOpponent(Board opponent) {
        this.opponent = opponent;
    }

    /**
     * 타이머를 가동
     */
    public void start() {
        isStarted = true;
        timer.start();
    }

    /**
     * 게임 종료 -> 게임에 패배한 보드에서 실행되는 메소드
     * AI 대전인 경우, numLinesRemoved를 최대 점수로 갱신
     */
    public void gameOver() {
        Board player = this instanceof BoardPlayer ? this : opponent;

        TetrisGameManager manager = player.parentTetris.gameManager();

        if(manager.isComputer()) {
            MaxScorePanel maxScorePanel = manager.getMaxScorePanel();

            int prevMaxScore = maxScorePanel.getMaxScore();
            
            if(player.numLinesRemoved > prevMaxScore)
                maxScorePanel.fileWriter(player.numLinesRemoved);
        }

        opponent.isStarted = false;
        opponent.timer.stop();

        isStarted = false;
        timer.stop();

        curPiece.setShape(Tetrominoes.NoShape);

        gameOverBoardPaint();

        manager.gameOverDialog().setVisible(true);
    }

    /**
     * 게임에서 패배한 보드의 모든 블록을 LockBlock으로 변경
     */
    private void gameOverBoardPaint() {
        for(int i = 0; i < BoardHeight; i++){
            for(int j = 0; j < BoardWidth; j++){
                if(gridBoard[i][j] != Tetrominoes.NoShape)
                    gridBoard[i][j] = Tetrominoes.LockBlock;
            }
        }
        repaint();
    }

    /**
     * 보드의 모든 블록을 지운다.
     */
    public void clearBoard() {
        for (int i = 0; i < BoardHeight; ++i) {
            for(int j = 0; j < BoardWidth; ++j) {
                gridBoard[i][j] = Tetrominoes.NoShape;
            }
        }
    }

    /**
     * 보드의 가장 위에 새로운 블록을 생성한다.
     * 
     * @return 더이상 블록을 생성할 수 없다면,
     *         (블록이 생성되는 위치에 다른 블록이 존재한다면) false를 반환한다.
     *         그 외에는 true를 반환한다.
     */
    public boolean newPiece() {
        curPiece = nextPiece.copy();

        int initPosX = BoardWidth / 2;
        int initPosY = BoardHeight - 2 + curPiece.minY();

        if (!tryMove(curPiece, initPosX, initPosY)) {
            gameOver();
            return false;
        }

        move(curPiece, initPosX, initPosY);

        nextPiece.setRandomShape();
        parentTetris.getBlockPreview().setNextPiece(nextPiece);

        isFallingFinished = false;

        return true;
    }

    /**
     * 폭탄이 터질 때 수행.
     * 범위 내의 블록을 제거한다.
     */
    public void bombBlock() {
        int x = curPiece.curX();
        int y = curPiece.curY();

        for(int i = y + 1; i >= y - 1; i--){
            for(int j = x - 1; j <= x + 1; j++){
                if(i < 0 || i >= BoardHeight || j < 0 || j >= BoardWidth)
                    continue;
                if(gridBoard[i][j] != Tetrominoes.NoShape)
                    gridBoard[i][j] = Tetrominoes.NoShape;
            }
        }
    }

    public int panelWidth(){
        return PanelWidth;
    }
    public int panelHeight(){
        return PanelHeight;
    }

    public int width(){
        return BoardWidth;
    }
    public int height(){
        return BoardHeight;
    }

    public Timer getTimer(){
        return timer;
    }

    public boolean isStarted(){
        return isStarted;
    }

    public Shape getNextPiece() { // 다음에 떨어질 도형
        return nextPiece;
    }
    public Shape getCurPiece() { // 현재 떨어지고 있는 도형
        return curPiece;
    }
    
    private int squareWidth() { // 블록 한칸(1 x 1) 가로 길이
        return getPreferredSize().width / BoardWidth;
    }
    private int squareHeight() { // 블록 한칸(1 x 1) 세로 길이
        return getPreferredSize().height / BoardHeight;
    }
    
    public Tetrominoes shapeAt(int x, int y) { // (x, y)에 있는 블럭의 Tetrominoes 타입
        return gridBoard[y][x];
    }

    /**
     * 떨어지고 있는 블록을 직접적으로 이동시키는 메소드
     * 
     * @param piece 변화된 블록의 모양 (Up, Down 키를 통한 블록의 회전을 고려)
     * @param newX 이동할 X 좌표
     * @param newY 이동할 Y 좌표
     */
    public void move(Shape piece, int newX, int newY) {
        if(!tryMove(piece, newX, newY))
            return;
        curPiece = piece;
        curPiece.moveTo(newX, newY);
        repaint();
    }

    /**
     * 블록이 다음 위치로 이동 가능한지의 여부를 체크하는 메소드
     * 
     * @param piece 변화된 블록의 모양 (Up, Down 키를 통한 블록의 회전을 고려)
     * @param newX 이동할 X 좌표
     * @param newY 이동할 Y 좌표
     * @return 이동 가능하면 true
     *         이동 불가능하면 false를 반환
     */
    public boolean tryMove(Shape piece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + piece.x(i);
            int y = newY - piece.y(i);
            if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight)
                return false;
            if (shapeAt(x, y) != Tetrominoes.NoShape)
                return false;
        }
        return true;
    }

    /**
     * curPiece를 한줄 아래로 이동시킨다.
     */
    public void oneLineDown() {
        if (!tryMove(curPiece, curPiece.curX(), curPiece.curY() - 1))
            pieceDropped();
        else
            move(curPiece, curPiece.curX(), curPiece.curY() - 1);
    }
    
    /**
     * curPiece의 위치를 확정시킨다.(맨 아래로 이동시킨다)
     * 
     * 이후 pieceDropped, removeFullLines, afterRemoveLines, stackLinesToOpponent, 
     *     removeOneBlock, recoverHp, decreaseOtherHp 메소드가 연쇄적으로 수행된다.
     */
    public void dropDown() {
        int newY = curPiece.curY();
        while (newY > 0) {
            if (!tryMove(curPiece, curPiece.curX(), --newY))
                break;
            move(curPiece, curPiece.curX(), newY);
        }
        pieceDropped();
    }
    private void pieceDropped() { // 블록이 완전히 떨어지면, 해당 블록을 board에 그리는 식
        for (int i = 0; i < 4; ++i) {
            int x = curPiece.curX() + curPiece.x(i);
            int y = curPiece.curY() - curPiece.y(i);
            gridBoard[y][x] = curPiece.getShape();
        }

        if(curPiece.getShape() == Tetrominoes.BombBlock) {
            bombBlock();
        } 
        removeFullLines();
    }

    private void removeFullLines() { // 한 줄 제거 가능 여부 탐색(점수 획득)
        curPiece.setShape(Tetrominoes.NoShape);
        int numFullLines = 0;
        for (int i = BoardHeight - 1; i >= 0; --i) {
            boolean lineIsFull = true;
            for (int j = 0; j < BoardWidth; ++j) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < BoardHeight - 1; ++k) {
                    for (int j = 0; j < BoardWidth; ++j){
                        gridBoard[k][j] = shapeAt(j, k + 1);
                        if(k == BoardHeight - 2 && shapeAt(j, k + 1) != Tetrominoes.NoShape)
                            gridBoard[k + 1][j] = Tetrominoes.NoShape;
                    }
                }
            }
        }
        isFallingFinished = true;
        afterRemoveLines(numFullLines);
    }

    private void afterRemoveLines(int numFullLines) { 
        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            parentTetris.getStatusBar().setText(String.valueOf(numLinesRemoved)); // 0. 점수 갱신
            recoverHp(numFullLines); // 1. 내 hp 회복
            int remainCount = decreaseOtherHp(numFullLines); // 2. 상대 hp 감소
            stackLinesToOpponent(remainCount); // 3. 상대 보드에 장애물 생성
        }
        repaint();

        if(isBlockOvered)
            opponent.gameOver();
    }

    private void stackLinesToOpponent(int attackCount) { // 상대 보드에 장애물 블록 생성
        if(attackCount == 0)
            return;

        Shape opponentPiece = opponent.curPiece;
        for(int i = 0; i < attackCount; i++){
            opponent.move(opponentPiece, opponentPiece.curX(), opponentPiece.curY() + 1);
        }
    
        for (int i = BoardHeight - 1; i >= 0; i--) {
            for (int j = 0; j < BoardWidth; j++) {
                if(opponent.gridBoard[i][j] == Tetrominoes.NoShape)
                    continue;

                if (i + attackCount >= BoardHeight) {
                    isBlockOvered = true;
                } else {
                    opponent.gridBoard[i + attackCount][j] = opponent.gridBoard[i][j];
                }
                opponent.gridBoard[i][j] = Tetrominoes.NoShape;
            }
        }

        removeOneBlock(attackCount);
    }

    private void removeOneBlock(int linesHeight) { // 상대 보드에 생성된 장애물 라인에서 각각 한칸씩만 지운다. 
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int x = r.nextInt(BoardWidth);

        for (int i = 0; i < linesHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {
                opponent.gridBoard[i][j] = Tetrominoes.LockBlock;
            }
            opponent.gridBoard[i][x] = Tetrominoes.NoShape;
        }
    }

    private void recoverHp(int count){ // 내 hp 회복 
        JProgressBar curHp = parentTetris.getHealthBar();
        int increasedHp = curHp.getValue() + count * HealthRecover;
        if(increasedHp > 100)
            increasedHp = 100;
        curHp.setValue(increasedHp);
    }

    private int decreaseOtherHp(int count) { // 상대 hp 감소
        int attackCount = 0;

        JProgressBar otherHp = opponent.parentTetris.getHealthBar();
        int decreasedHp = otherHp.getValue() - count * AttackDamage;
        if(decreasedHp < 0){
            attackCount += (-1 * decreasedHp) / AttackDamage;
            decreasedHp = 0;
        }
        otherHp.setValue(decreasedHp);

        return attackCount;
    }

    /**
     * gridBoard(보드에 위치한 블록), curPiece(떨어지는 블록), 
     * 블록 고스트를 페인트 하는 메소드
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BoardHeight * squareHeight();

        paintBoardPiece(g, boardTop);
        paintDroppingPiece(g, boardTop);
        paintGhost(g, boardTop);
    }

    private void paintBoardPiece(Graphics g, int boardTop) {
        for (int i = 0; i < BoardHeight; ++i) {
            for (int j = 0; j < BoardWidth; ++j) {
                Tetrominoes shape = shapeAt(j, BoardHeight - i - 1);
                if(shape != Tetrominoes.NoShape)
                    drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
            }
        }
    }
    private void paintDroppingPiece(Graphics g, int boardTop) {
        if (curPiece.getShape() == Tetrominoes.NoShape) 
            return;

        for (int i = 0; i < 4; ++i) {
            int x = curPiece.curX() + curPiece.x(i);
            int y = curPiece.curY() - curPiece.y(i);
            drawSquare(g, x * squareWidth(), boardTop + (BoardHeight - y - 1) * squareHeight(), curPiece.getShape());
        }
    }

    private void paintGhost(Graphics g, int boardTop) {
        if(curPiece.getShape() == Tetrominoes.NoShape)
            return;

        int nX = curPiece.curX();
        int nY = curPiece.curY();
        while(nY >= 0){
            if(!tryMove(curPiece, nX, nY))
                break;
            nY--;
        }
        nY++;

        if(curPiece.getShape() == Tetrominoes.BombBlock) {
            paintBombGhost(g, boardTop, nX, nY);
        } else {
            paintPieceGhost(g, boardTop, nX, nY);
        }
    }

    private void paintBombGhost(Graphics g, int boardTop, int nX, int nY){
        for(int i = nY - 1; i <= nY + 1; i++){
            for(int j = nX - 1; j <= nX + 1; j++){
                if(i < 0 || i >= BoardHeight || j < 0 || j >= BoardWidth)
                    continue;
                drawSquare(g, j * squareWidth(), boardTop + (BoardHeight - i - 1) * squareHeight(), Tetrominoes.NoShape);
            }
        }
    }

    private void paintPieceGhost(Graphics g, int boardTop, int nX, int nY){
        for(int i = 0; i < 4; i++){
            int x = nX + curPiece.x(i);
            int y = nY - curPiece.y(i);
            drawSquare(g, x * squareWidth(), boardTop + (BoardHeight - y - 1) * squareHeight(), Tetrominoes.NoShape);
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
        BlockImage block = new BlockImage(shape);

        int imageSize = squareWidth(); // 이미지 크기를 블록 크기에 맞게 조정합니다.
        
        if(shape == Tetrominoes.BombBlock) {
            imageSize = 30;
            g.drawImage(block.getImage(), x - 2, y - 8, imageSize, imageSize, null);
        }
        else if(shape == Tetrominoes.NoShape){
            Graphics2D g2d = (Graphics2D) g;
            AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
            g2d.setComposite(alphaComposite);
            g2d.drawImage(block.getImage(), x, y - 2, imageSize, imageSize, null);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
        else
            g.drawImage(block.getImage(), x, y - 2, imageSize, imageSize, null);
    }
}