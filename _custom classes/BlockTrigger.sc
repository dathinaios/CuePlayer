BlockTrigger{ var <>action, <>interval;
	var list, pattern;

	*new{ arg action, interval = 0.3;
		^super.newCopyArgs(action, interval).init;
	}

	init{
		list = List[0,0];
		pattern = Pseq([0, 1], inf ).asStream;
	}

	do{
		list[pattern.next] = Main.elapsedTime;
		if ( (list[0] - list[1]).abs < interval ,{ nil } ,{ action.value } )
		}
}

// a = BlockTrigger(action: {"all ok".postln});
// a.interval = 0.1
// a.do