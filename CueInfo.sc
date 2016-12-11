
CueInfo {
  var <>info, <>func, <>performerInfo;

  *new { arg info = "", func, performerInfo = "";
    ^super.newCopyArgs(info, func, performerInfo).init;
  }

  init {
  }

  value { arg cuePlayer;
	func.value(cuePlayer);
  }

}