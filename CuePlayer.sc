
CuePlayer { 

  var gui, <cues, name;

  *new { arg name;
    ^super.newCopyArgs(name).init;
  }

  init {
    cues = Cues.new;
  }

  gui {
    gui = CuePlayerGUI(cues);
  }

  next {
    ^cues.next;
  }

  setCurrent { arg cue;
    cues.setCurrent(cue)
  }

}
