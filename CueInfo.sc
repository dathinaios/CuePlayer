
CueInfo {
  var <>cueTitle, <>largeDisplayInfo, <>function;

  *new { arg cueTitle = "", largeDisplayInfo = "", function;
    ^super.newCopyArgs(cueTitle, largeDisplayInfo, function);
  }

  value { arg cuePlayer;
	function.value(cuePlayer);
  }

}