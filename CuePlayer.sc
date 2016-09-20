
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
    MIDIIn.connectAll;
  }

  gui {arg monitorInChannels = 2, monitorOutChannels = 8, largeDisplay = false;
    guiInstance = CuePlayerGUI(this, monitorInChannels, monitorOutChannels, largeDisplay);
    cues.addDependant(guiInstance);
    this.addDependant(guiInstance);
  }
  tempo { arg bpm = 120;
    ("CuePlayer tempo set to " ++ bpm ++ " bpm").postln;
    clock.tempo = bpm/60;
    this.changed(\tempo);
    ^bpm;
  }

  midiTrigger { arg note = 60, channel = 15;
    MIDIFunc.noteOn({ arg vel, noteNum, chan;
      if (noteNum == note && chan == channel, {
        this.trigger(vel+1);
      }.defer);
    }).permanent = true;
  }

  sendOSC { arg ip = "127.0.0.1", port = 57120, msg = ["/play", 1]; var address;
    address = NetAddr(ip, port);
    this.sched(clock.timeToNextBeat,{address.sendMsg(msg)});
  }

  quit {
  }

  /* interacting with the Cues */

  addCue { arg function, cueNumber;
    ^cues.addCue(function, cueNumber);
  }

  next {
    ^cues.next;
  }

  current {
    ^cues.current;
  }

  setCurrent { arg cue;
    ^cues.setCurrent(cue);
  }

  trigger { arg cue = 1;
    ^cues.trigger(cue);
  }

  sched { arg time, function;
    Routine {
      time.wait;
      Server.default.makeBundle(nil, function);
    }.play(clock);
  }

}
