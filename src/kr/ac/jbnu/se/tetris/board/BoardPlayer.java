package kr.ac.jbnu.se.tetris.board;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import kr.ac.jbnu.se.tetris.game.Tetris;

public class BoardPlayer extends Board implements ActionListener {

    public static int moveDelay = 500;

    private final int BombCutlineScore = 10;
    private int cutlineCheck;

    public BoardPlayer(Tetris parent) {
        super(parent);
        cutlineCheck = BombCutlineScore;
        timer = new Timer(moveDelay, this);        
        start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            if(numLinesRemoved >= cutlineCheck) { // 점수 기준 넘어가면 폭탄 획득
                parentTetris.acquireBomb();
                cutlineCheck += BombCutlineScore;
            }
            newPiece();
        } else {
            oneLineDown();
        }
    }
}