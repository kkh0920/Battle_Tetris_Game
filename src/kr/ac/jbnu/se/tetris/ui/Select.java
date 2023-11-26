package kr.ac.jbnu.se.tetris.ui;

import javax.swing.*;

import kr.ac.jbnu.se.tetris.audio.Music;
import kr.ac.jbnu.se.tetris.game.TetrisGameManager;

public class Select extends JFrame {
    
    private Wallpapers background;
    
    private Music music;

    private SelectLevel selectLevel;

    private Setting setting;

    private Tutorial tutorial;

    private CustomButton ai;
    private CustomButton versus;
    private CustomButton settingBtn;
    private CustomButton tutorialBtn;

    public Select() {
        setFrame();

        selectLevel = new SelectLevel(this);
        setting = new Setting(this);
        tutorial = new Tutorial();
        
        music = new Music();
        music.startMusic();
    }

    public Music getMusic(){
        return music;
    }

    public void setFrame() {
        int frameWidth = 800;
        int frameHeight = 453;

        setSize(frameWidth, frameHeight);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        background = new Wallpapers("image/Background.jpg", frameWidth, frameHeight);
        setButton();

        add(ai); add(versus); add(settingBtn); add(tutorialBtn);
        add(background.getPane());
    }

    public void setButton() {
        int btnWidth = 172;
        int btnHeight = 50;

        int btnY = getHeight() - 150;

        ai = new CustomButton(new ImageIcon("image/buttons/AImodes.png"));
        ai.setBounds(71, btnY, btnWidth, btnHeight);
        
        versus = new CustomButton(new ImageIcon("image/buttons/2P_modes.png"));
        versus.setBounds(142 + btnWidth, btnY, btnWidth, btnHeight);

        settingBtn = new CustomButton(new ImageIcon("image/buttons/settings.png"));
        settingBtn.setBounds(213 + btnWidth * 2, btnY, btnWidth, btnHeight);
        
        tutorialBtn = new CustomButton(new ImageIcon("image/buttons/tutorial.png"));
        tutorialBtn.setBounds(getWidth() - 150, 30, 120, 35);

        buttonAction();
    }

    public void buttonAction() {
        ai.addActionListener(e->{
            dispose();
            selectLevel.setVisible(true);
        });

        versus.addActionListener(e->{
            dispose();

            TetrisGameManager.level = 0; // 플레이어 대전인 경우 0 레벨 설정
            TetrisGameManager game = new TetrisGameManager();
            game.start(false);
        });

        settingBtn.addActionListener(e->{
            setVisible(false);
            setting.setVisible(true);
        });

        tutorialBtn.addActionListener(e->
            tutorial.setVisible(true)
        );
    }
}