
CuePlayer { 

  var gui, <cues, <name, <clock;

  *new { arg name;
    ^super.newCopyArgs(name).init;
  }

  init {
    cues = Cues.new;
    clock = TempoClock(50/60).permanent_(true);
  }

  gui {
    gui = CuePlayerGUI(this);
  }

  next {
    ^cues.next;
  }

  setCurrent { arg cue;
    ^cues.setCurrent(cue)
  }

  /* tempo { arg bpm = 120; */
  /*   var beatDur = 1/(50/60); // time multiplier, gives the duration of 1 beat */
  /* } */

}
