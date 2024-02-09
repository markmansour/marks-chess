package com.stateofflux.chess.model;


import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class TranspositionTableTest {
    @Test
    public void basicTT() {
        TranspositionTable tt = new TranspositionTable();
        long key = Integer.MAX_VALUE - 5;
        int score = 10000000;
        int depth = 3;
        int ply = 1;
        Move m = new Move(Piece.EMPTY, 0, 0, false);
        tt.put(key, score, m, TranspositionTable.NodeType.EXACT, depth, ply);

        TranspositionTable.Entry entry = tt.get(key, ply);
        assertThat(entry.key()).isEqualTo(key);
        assertThat(entry.score()).isEqualTo(score);
        assertThat(entry.depth()).isEqualTo(depth);
        assertThat(entry.nt()).isEqualTo(TranspositionTable.NodeType.EXACT);
        // ply test!>
    }

    @Test public void testWithLargeDataSet() {
        long[] testData = { -702565069, -651716404, -90772002, 139827217, -818005229, 1941803680, -1748144377, 596276289,
            1921378528, 1368035031, -1425636917, -575780564, 1409290471, 845303300, 1136883922, -1430851484,
            924912986, 92095816, -1065598504, -748528041, 1089599880, -723160118, 598513820, -1833342420,
            -906682537, -1353481056, 1170562732, 879692537, -1717334298, -2107126715, -1800285465, -2019153999,
            -312376001, -1972445292, -1934743398, -1607487485, 923393733, 1080390386, 36105524, 93848421,
            356787436, -660305748, 1469707869, 640989862, 2057833544, -1142459402, 853007415, 1784621122,
            781779923, -1144725482, 273392100, -682150231, -256820337, 1136592960, -942170213, -1018707522,
            1847610686, -1601831344, -1276646715, -346590898, 488806964, 1313067790, -1682129807, -341730302,
            612847047, 1095071911, -445450136, 1920139068, 145389739, -643893363, 1241636357, -1100133313,
            -184365430, 1319278410, 337755483, 1168851636, -2018124883, 1987138910, -130098383, -465363916,
            1261447690, 525217523, -636830992, 1346028279, -956871218, 538206404, 649095846, 1324071018,
            1081035944, -613597020, 1165899758, -1992686127, 775621122, 1788875486, -1115851423, 1026827060,
            -1049617314, -1819399049, 44482663, 1565463311, 967598703, -1176084252, 532537143, -1736176902,
            1968791497, 1942159074, 1553299834, 1540848062, -1077798375, -1957540514, -748422607, -1835034361,
            -781018150, -1237809038, 621622304, -1852382785, -44645642, 1394279158, 1641190693, 1031255177,
            1970948249, -874529702, 363532650, 149151584, -2019253001, 1233445122, -1942132566, -1605421159,
            -1510841015, -1047551057, 1756701190, -1429453207, -391100556, 1575039684, -597592416, -427242900,
            1458115053, -1658987226, -1870818275, 1265802426, 784056402, 398561731, -137806895, -325193710 };

        Move m = new Move(Piece.EMPTY, 0, 0, false);
        TranspositionTable tt = new TranspositionTable();

        for(var key : testData) {
            tt.put(key, ((int) Math.abs(key)) % 10000, m, TranspositionTable.NodeType.EXACT, ((int) Math.abs(key)) % 5, 1);
        }

        for(var key: testData) {
            TranspositionTable.Entry entry = tt.get(key, 1);
            assertThat(entry.key()).isEqualTo(key);
            assertThat(entry.score()).isEqualTo(((int) Math.abs(key)) % 10000);
            assertThat(entry.depth()).isEqualTo(((int) Math.abs(key)) % 5);
            assertThat(entry.nt()).isEqualTo(TranspositionTable.NodeType.EXACT);
        }

        tt.clear();
        for(var key: testData) {
            assertThat(tt.get(key, 1)).isNull();
        }
    }
}
