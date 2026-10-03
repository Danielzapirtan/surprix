# Surprix

Surprix is a native Android app for two people sharing one device. It provides
manual X-and-0 play for Tic-Tac-Toe, Gomoku, and Go; it has no computer player.

## Build and test

Requires JDK 17, Android SDK platform 35, and Gradle 8.9 or newer.

- Build a debug APK: `gradle :app:assembleDebug`
- Run the JVM game-rule tests: `gradle :app:testDebugUnitTest`
- Run one test: `gradle :app:testDebugUnitTest --tests 'com.surprix.GameStateTest.goRejectsSuicideWithoutChangingTurnOrBoard'`

## Rules in this app

- Tic-Tac-Toe uses a 3x3 board; Gomoku uses a 15x15 board and wins on five or
  more contiguous marks in any direction, with no restricted opening rules.
- Go uses a 9x9 board. Groups are connected orthogonally; a move that captures
  has its captures removed before its own liberties are checked. Suicide and
  immediate simple-ko repetition are rejected. Two consecutive passes end the
  game. The displayed area score counts stones plus empty regions bordered by
  only one color; neutral regions score for neither player. There is no komi or
  dead-stone adjudication.
