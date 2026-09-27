const games = [ 'TicTacToe', 'Gomoku', 'Go' ];

class Board {
	constructor() {
		_gameIndex = 0;
		_cursor = [0, 0];
		_board = BoardInit();
		_to_move = 0;
	}
	BoardInit() {}
	Render() { }
	Clear() { }
	Makemove() { }
	GetFinalScore() { }
	isFinal() { }
	listenMove() { }
}


