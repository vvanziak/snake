package com.globallogic.training.oleksii.morozov.sapperthehero.boardsaver;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class BoardSaverTest {
    Saver boardSaver;

    @Before
    public void setUp() {
        boardSaver = new BoardSaver();
    }

    @Test
    public void shouldBoardSaver_whenGameStart() {
        assertNotNull(boardSaver);
    }

    @Test
    public void shouldDefaultFilePath() {
        assertEquals("boards/", boardSaver.getFilePath());
    }

}