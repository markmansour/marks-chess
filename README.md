# Mark's Chess

[UML Diagram Source](https://www.planttext.com/?text=NPBFJiCm3CRlVWeVEw7n01M7mIHW1wH9GrmGXz2wQrYQeDYb8E3ToR_HWPJ3vv-TsFwK5OloCWu6W0_QedTC0AB54AwDOYR69QvxOWQ09XAySigt-0c8PqbMYrdIksRHThyJ9PJuuDvegwcXwYueZ67Yugx9xiI7ucfHOrRm3M2T41hg15sNUq7kg1bVHXPKs2fHoE8yuQIbHp1QuiJ6xNLpZlSzuOqQ2CbspZV487r90jaMOuVZCbkZFiY7RMBhZ6x7GTdC2xdSbB34wV5amDHheRgy4i6pKStX3uYprsMH42V3isLxalNeFLd9vT67-MB8FYYZssWRLw-9Xu8OFZx5WeXEJVrT8B4qtiOATjuDHoqkg7347PsFjz8wozPDTbdcF2LbecBKnLV1foxzpyr-hOBVuNVz0000)

## Stockfish usage

```bash
% brew install stockfish
% stockfish
```

```
go perft 1
a2a3: 1
b2b3: 1
c2c3: 1
d2d3: 1
e2e3: 1
f2f3: 1
g2g3: 1
h2h3: 1
a2a4: 1
b2b4: 1
c2c4: 1
d2d4: 1
e2e4: 1
f2f4: 1
g2g4: 1
h2h4: 1
b1a3: 1
b1c3: 1
g1f3: 1
g1h3: 1

Nodes searched: 20

d

+---+---+---+---+---+---+---+---+
| r | n | b | q | k | b | n | r | 8
+---+---+---+---+---+---+---+---+
| p | p | p | p | p | p | p | p | 7
+---+---+---+---+---+---+---+---+
|   |   |   |   |   |   |   |   | 6
+---+---+---+---+---+---+---+---+
|   |   |   |   |   |   |   |   | 5
+---+---+---+---+---+---+---+---+
|   |   |   |   |   |   |   |   | 4
+---+---+---+---+---+---+---+---+
|   |   |   |   |   |   |   |   | 3
+---+---+---+---+---+---+---+---+
| P | P | P | P | P | P | P | P | 2
+---+---+---+---+---+---+---+---+
| R | N | B | Q | K | B | N | R | 1
+---+---+---+---+---+---+---+---+
a   b   c   d   e   f   g   h

Fen: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
Key: 8F8F01D4562F59FB
Checkers:
quit
```
To set your position
```
position fen rnbqkbnr/pppppppp/8/8/6P1/8/PPPPPP1P/RNBQKBNR b KQkq - 0 1
d

 +---+---+---+---+---+---+---+---+
 | r | n | b | q | k | b | n | r | 8
 +---+---+---+---+---+---+---+---+
 | p | p | p | p | p | p | p | p | 7
 +---+---+---+---+---+---+---+---+
 |   |   |   |   |   |   |   |   | 6
 +---+---+---+---+---+---+---+---+
 |   |   |   |   |   |   |   |   | 5
 +---+---+---+---+---+---+---+---+
 |   |   |   |   |   |   | P |   | 4
 +---+---+---+---+---+---+---+---+
 |   |   |   |   |   |   |   |   | 3
 +---+---+---+---+---+---+---+---+
 | P | P | P | P | P | P |   | P | 2
 +---+---+---+---+---+---+---+---+
 | R | N | B | Q | K | B | N | R | 1
 +---+---+---+---+---+---+---+---+
   a   b   c   d   e   f   g   h

Fen: rnbqkbnr/pppppppp/8/8/6P1/8/PPPPPP1P/RNBQKBNR b KQkq - 0 1
Key: 8E79D756F2C9E586
Checkers:
```