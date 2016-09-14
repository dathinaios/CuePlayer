
CuePlayer { 

  var <name;
  var <cues, <clock;
  var <guiInstance;

  *new { arg name;
    ^super.newCopyArgs(name).init;
  }

  init {
    cues = Cues.new;
    clock = TempoClock(120/60).permanent_(true);
  }

  gui {arg monitorInChannels, monitorOutChannels;
    guiInstance = CuePlayerGUI(this, monitorInChannels, monitorOutChannels);
    cues.addDependant(guiInstance);
  }

  addCue { arg function, cueNumber;
    ^cues.addCue(function, cueNumber);
  }

  next {
    ^cues.next;
  }

  setCurrent { arg cue;
    ^cues.setCurrent(cue)
  }

  trigger { arg cue = 1;
    ^cues.trigger(cue);
  }

  tempo { arg bpm = 120;
    ("CuePlayer tempo set at " ++ bpm ++ " bpm").postln;
    clock.tempo = bpm/60;
    ^bpm;
  }

  midiTrigger { arg note = 60, channel = 15;
    MIDIFunc.noteOn({ arg vel, noteNum, chan;
      if (noteNum == note && chan == channel, {
        this.trigger(vel);
      });
    }).permanent = true;
  }

  quit {
  }

}
