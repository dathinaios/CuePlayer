
CuePlayer { 

  var gui, <cues;

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

}
