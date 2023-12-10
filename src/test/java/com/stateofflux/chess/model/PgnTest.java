package com.stateofflux.chess.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PgnTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PgnTest.class);


    @Test public void readWellFormedPgn() {
        String pgnString = """
            [Event "FICS rated standard game"]
            [Site "FICS freechess.org"]
            [FICSGamesDBGameNo "530203162"]
            [White "Demicka"]
            [Black "konozrout"]
            [WhiteElo "1863"]
            [BlackElo "2262"]
            [WhiteRD "109.6"]
            [BlackRD "39.7"]
            [BlackIsComp "Yes"]
            [TimeControl "900+0"]
            [Date "2023.01.31"]
            [Time "22:18:00"]
            [WhiteClock "0:15:00.000"]
            [BlackClock "0:15:00.000"]
            [ECO "A85"]
            [PlyCount "33"]
            [Result "0-1"]
                        
            1. d4 f5 2. c4 Nf6 3. Nc3 g6 4. f3 Bg7 5. e4 d6 6. Bd3 O-O 7. Nge2 fxe4 8. fxe4 Ng4 9. Qc2 Nc6 10. d5 Nb4 11. Qb1 Nxd3+ 12. Qxd3 Nf2 13. Qc2 Nxh1 14. Be3 e5 15. O-O-O Nf2 16. Rf1 Nd3+ 17. Qxd3 {White resigns} 0-1
            """;

        // PgnReader reader = new PgnReader(new BufferedReader(new StringReader(pgnString)));
        Pgn pgn = new Pgn(pgnString);

        assertThat(pgn.getMoves()).hasSize(17);
        assertThat(pgn.getTagPairs()).hasSize(18);
        assertThat(pgn.getMoves().get(pgn.getMoves().size() -1).blackMove()).isEmpty();
    }

    @Test public void readWellFormedPgnWithDraw() {
        String pgnString = """
            [Event "FICS rated standard game"]
            [Site "FICS freechess.org"]
            [FICSGamesDBGameNo "530197044"]
            [White "ArasanX"]
            [Black "exeComp"]
            [WhiteElo "2759"]
            [BlackElo "2755"]
            [WhiteRD "33.2"]
            [BlackRD "36.3"]
            [WhiteIsComp "Yes"]
            [BlackIsComp "Yes"]
            [TimeControl "960+3"]
            [Date "2023.01.30"]
            [Time "22:15:00"]
            [WhiteClock "0:16:00.000"]
            [BlackClock "0:16:00.000"]
            [ECO "D23"]
            [PlyCount "250"]
            [Result "1/2-1/2"]
            
            1. c4 c6 2. d4 d5 3. Nf3 Nf6 4. Qc2 dxc4 5. Qxc4 Bg4 6. Nbd2 Nbd7 7. g3 e6 8. Bg2 Be7 9. O-O O-O 10. Qb3 Nb6 11. e3 Nfd7 12. Nc4 Nxc4 13. Qxc4 Rc8 14. Bd2 Bxf3 15. Bxf3 Ne5 16. dxe5 Qxd2 17. Qb3 Rc7 18. Rfd1 Qa5 19. Qc3 Bb4 20. Qc4 Be7 21. Qe4 Rd8 22. Rxd8+ Bxd8 23. Rd1 Be7 24. a4 Qb6 25. b3 g6 26. Qd4 Qxd4 27. Rxd4 Kf8 28. Kf1 a6 29. Rc4 a5 30. Be4 Ke8 31. g4 h6 32. f4 Bb4 33. Rd4 Bc5 34. Rd3 Rd7 35. Ke2 Rxd3 36. Bxd3 g5 37. Kf3 Ba3 38. Ke4 Bb2 39. Bf1 Ke7 40. Kd3 Bc1 41. Ke4 Ke8 42. Bc4 Ke7 43. f5 Kd7 44. Kd4 Bb2+ 45. Ke4 Bc3 46. Be2 Ke7 47. h3 Bd2 48. Kd3 Bc1 49. Ke4 Bb2 50. Bf1 Bc1 51. Bc4 Bd2 52. Kd3 Bc1 53. Ke4 Bb2 54. Bf1 Kd7 55. Bd3 Ba3 56. Bb1 Kc7 57. Bd3 Kd7 58. Bc4 Ke7 59. Kd4 Bb2+ 60. Ke4 Bc3 61. Bf1 Bb2 62. Bc4 Ba3 63. Kd4 Kd7 64. Kc3 Bc5 65. Kd3 Ba3 66. Kc2 Bb4 67. Kd3 Ke7 68. Kd4 Kd7 69. Kd3 Ke7 70. Ke4 Bc5 71. Bf1 Ba3 72. Kd3 Kd8 73. Ke4 Kd7 74. Bg2 Bb2 75. Bf1 b6 76. Bc4 Ba3 77. Ba6 Bc1 78. Bf1 Ke7 79. Ba6 Kd7 80. Be2 Ke7 81. Bf1 Bb2 82. Be2 Kd7 83. Bd3 Ba3 84. Bc4 Bc5 85. Bf1 Be7 86. Bc4 Ba3 87. Bd3 Bc5 88. Ba6 Bb4 89. Be2 Bc5 90. Bf1 Ba3 91. Ba6 Bb2 92. Bf1 Ke7 93. Be2 Bc1 94. Bf3 Kd7 95. Be2 Ke8 96. Bc4 Ke7 97. Bd3 Ba3 98. Ba6 Kd7 99. Bf1 Bb4 100. Ba6 Ba3 101. Bd3 Ke7 102. Bc4 Bb4 103. Ba6 Kd7 104. Kd4 Bd2 105. Ke4 Be1 106. Bf1 Bc3 107. Bc4 Bb2 108. Bd3 Bc3 109. Bf1 Bb4 110. Bc4 Be1 111. Kd3 Bb4 112. Kc2 Bc5 113. Kd3 Ba3 114. Kc3 Bc1 115. Kd4 Ke7 116. Kd3 Ba3 117. Ke4 Bc5 118. Bd3 Bb4 119. Bc4 Kd7 120. Bd3 Be1 121. Bc4 Bc3 122. Bd3 Ke8 123. Ba6 Ke7 124. Bb7 Kd7 125. Ba6 Bb2 {Game drawn by the 50 move rule} 1/2-1/2
            """;

        // PgnReader reader = new PgnReader(new BufferedReader(new StringReader(pgnString)));
        Pgn pgn = new Pgn(pgnString);

        assertThat(pgn.getMoves()).hasSize(125);
        assertThat(pgn.getTagPairs()).hasSize(19);
        assertThat(pgn.getMoves().get(pgn.getMoves().size() -1).blackMove()).isNotEmpty();
    }

    @Test public void readWellFormedPgnWithTrailingLine() {
        String pgnString = """
            [Event "FICS rated standard game"]
            [Site "FICS freechess.org"]
            [FICSGamesDBGameNo "530203162"]
            [White "Demicka"]
            [Black "konozrout"]
            [WhiteElo "1863"]
            [BlackElo "2262"]
            [WhiteRD "109.6"]
            [BlackRD "39.7"]
            [BlackIsComp "Yes"]
            [TimeControl "900+0"]
            [Date "2023.01.31"]
            [Time "22:18:00"]
            [WhiteClock "0:15:00.000"]
            [BlackClock "0:15:00.000"]
            [ECO "A85"]
            [PlyCount "33"]
            [Result "0-1"]
                        
            1. d4 f5 2. c4 Nf6 3. Nc3 g6 4. f3 Bg7 5. e4 d6 6. Bd3 O-O 7. Nge2 fxe4 8. fxe4 Ng4 9. Qc2 Nc6 10. d5 Nb4 11. Qb1 Nxd3+ 12. Qxd3 Nf2 13. Qc2 Nxh1 14. Be3 e5 15. O-O-O Nf2 16. Rf1 Nd3+ 17. Qxd3 {White resigns} 0-1
            
            """;

        // PgnReader reader = new PgnReader(new BufferedReader(new StringReader(pgnString)));
        Pgn pgn = new Pgn(pgnString);

        assertThat(pgn.getMoves()).hasSize(17);
        assertThat(pgn.getTagPairs()).hasSize(18);
        assertThat(pgn.getMoves().get(pgn.getMoves().size() -1).blackMove()).isEmpty();
    }

    @Test public void readWellFormedPgnWithLeadingLine() {
        String pgnString = """
            
            [Event "FICS rated standard game"]
            [Site "FICS freechess.org"]
            [FICSGamesDBGameNo "530203162"]
            [White "Demicka"]
            [Black "konozrout"]
            [WhiteElo "1863"]
            [BlackElo "2262"]
            [WhiteRD "109.6"]
            [BlackRD "39.7"]
            [BlackIsComp "Yes"]
            [TimeControl "900+0"]
            [Date "2023.01.31"]
            [Time "22:18:00"]
            [WhiteClock "0:15:00.000"]
            [BlackClock "0:15:00.000"]
            [ECO "A85"]
            [PlyCount "33"]
            [Result "0-1"]
                        
            1. d4 f5 2. c4 Nf6 3. Nc3 g6 4. f3 Bg7 5. e4 d6 6. Bd3 O-O 7. Nge2 fxe4 8. fxe4 Ng4 9. Qc2 Nc6 10. d5 Nb4 11. Qb1 Nxd3+ 12. Qxd3 Nf2 13. Qc2 Nxh1 14. Be3 e5 15. O-O-O Nf2 16. Rf1 Nd3+ 17. Qxd3 {White resigns} 0-1
            """;

        // PgnReader reader = new PgnReader(new BufferedReader(new StringReader(pgnString)));
        Pgn pgn = new Pgn(pgnString);

        assertThat(pgn.getMoves()).hasSize(17);
        assertThat(pgn.getTagPairs()).hasSize(18);
        assertThat(pgn.getMoves().get(pgn.getMoves().size() -1).blackMove()).isEmpty();
    }

}
