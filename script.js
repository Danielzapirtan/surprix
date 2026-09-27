const emptyGame = [];
const games = [ 'TicTacToe', 'Gomoku' ];
const gameIndex = 0;

function EvalBoard(board) {
	if (gameIndex !== 0) {
		return undefined;
	}
	const x = Mate(board);
	if (x < 1) {
		if (x & 1 == 0) {
			return 200 - x;
		}
		else {
			return x - 200;
		}
	} else {
		return EvalPos(board);
	}
}

function RenderBoard(board) {
	// TODO
}

function Search(board, level = 1) {
	if (level !== 1) {
		return undefined;
	}
	let best = -20000;
	let bestmove = undefined;
	const allFree = GetAllFree(board);
	const insideFree = GetInside(board, allFree);
	inside.forEach(move => {
		const aux = Makemove(board, move);
		const value = EvalBoard(board);
		if (value > best) {
			best = value;
			bestmove = move;
		}
	});
	return { best, bestmove };
}

