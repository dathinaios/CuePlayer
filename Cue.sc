
Cue {
  var <>title, <>func, <>performerInfo;

  *new { arg title = "|> aCue <|", func, performerInfo = "N\A";
    ^super.newCopyArgs(title, func, performerInfo).init;
  }

  init {
  }

  value { arg cuePlayer;
	title.postln;
	func.value;
	performerInfo.postln;
  }

}