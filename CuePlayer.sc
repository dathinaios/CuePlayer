
CuePlayer { 

  var <name;
  var <cues, <clock;

  *new { arg name;
    ^super.newCopyArgs(name).init;
  }

  init {
    cues = Cues.new;
    clock = TempoClock(120/60).permanent_(true);
  }

  gui {
    CuePlayerGUI(this);
  }

  next {
    ^cues.next;
  }

  setCurrent { arg cue;
    ^cues.setCurrent(cue)
  }

  tempo { arg bpm = 120;
    ("CuePlayer tempo set at " ++ bpm ++ " bpm").postln;
    clock.tempo = bpm/60;
    ^bpm;
  }

  /* TODO */
  midiTrigger { arg note = 60, channel = 15;
    /* from 1 to 127 velocity should actiavte the relevant cues */
    /* When it reaches 127 it should move up a semitone */
    midiFunc = {MIDIFunc.noteOn({ arg vel, noteNum, chan;
      if (vel == 127 && noteNum == 73 && chan == 15, { { trigButton.valueAction = 0 }.defer; });
    }).permanent = true;};
    midiFunc.value;
  }

  quit {
  }

}
