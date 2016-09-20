
CuePlayer {

  var <name;
  var <cues, <>clock;
  var <guiInstance;

  *new { arg name;
    ^super.newCopyArgs(name).init;
  }

  init {
    cues = Cues.new;
    clock = TempoClock(120/60, queueSize: 2048 * 2).permanent_(true);
    MIDIIn.connectAll;
  }

  gui {arg monitorInChannels = 2, monitorOutChannels = 8, monitorInOffset = 0, largeDisplay = false;
    if (guiInstance.isNil or: {guiInstance.active.not},
    {
      guiInstance = CuePlayerGUI(this, monitorInChannels, monitorOutChannels, monitorInOffset, largeDisplay);
      cues.addDependant(guiInstance);
      this.addDependant(guiInstance);
    }, {"The GUI for this CuePlayer is already active".warn;})
  }

  tempo { arg bpm = 120;
    ("CuePlayer tempo set to " ++ bpm ++ " bpm").postln;
    clock.tempo = bpm/60;
    this.changed(\tempo);
    ^bpm;
  }

  /* External Control */

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

  /* interacting with the Cues */

  addCue { arg function, cueNumber, timeline, timelineMode = \beats;
    timeline = timeline.asFunkySchedulerCP(clock);
    timeline.debug("timeline");
    ^cues.addCue({function.value; timeline.play(timelineMode)}, cueNumber);
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

  /* Helper Methods */

  sched { arg time, function;
    Routine {
      time.wait;
      Server.default.makeBundle(nil, function);
    }.play(clock);
  }

}
