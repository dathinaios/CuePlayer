
CuePlayer : Cues {

  var <name;
  var <>clock, <guiInstance;
  var oscTriggerFunc;

  *new { arg name;
    ^super.newCopyArgs(name).init;
  }

  init {
    super.init;
    clock = TempoClock(120/60, queueSize: 2048 * 2).permanent_(true);
    MIDIIn.connectAll;
  }

  add { arg function, cueNumber, timeline, timelineOptions = ();
    timeline = timeline.asTimeline(clock, timelineOptions);
    ^super.add({function.value; timeline.play;}, cueNumber);
  }

  gui {arg monitorInChannels = 2, monitorOutChannels = 8, options;
    if (guiInstance.isNil or: {guiInstance.active.not},
    {
      guiInstance = CuePlayerGUI(this, monitorInChannels, monitorOutChannels, options);
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

  midiTrigger { arg note = 60, channel = 0;
    MIDIFunc.noteOn({ arg vel, noteNum, chan;
      if (note == noteNum && chan == channel, {
        [vel, noteNum, chan].postln;
        {this.trigger(vel+1)}.defer;
      });
    }).permanent = true;
  }

  oscTrigger { arg message = 1, path = '/cueTrigger'; var address;
    address = NetAddr("127.0.0.1", NetAddr.langPort);
    oscTriggerFunc = OSCFunc(
      { arg msg; if (msg[1] == message, {{this.next}.defer}); },
      '/cueTrigger',
      address
    );
    oscTriggerFunc.permanent = true;
    this.oscTriggerInform(address, path, message)
  }

  freeOscTrigger {
    oscTriggerFunc.free;
    "The OSC trigger has been removed".postln;
  }

  sendOSC { arg ip = "127.0.0.1", port = 57120, msg = ["/play", 1]; var address;
    address = NetAddr(ip, port);
    this.sched(clock.timeToNextBeat,{address.sendMsg(msg)});
  }

  /* Private */

  sched { arg time, function;
    Routine {
      time.wait;
      Server.default.makeBundle(nil, function);
    }.play(clock);
  }

  oscTriggerInform { arg address, path, message;
    "====================================================".postln;
    ("CuePlayer OSC trigger:").postln;
    ("" ++ address ++ " with path " ++ path).postln;
    ("message: " ++ message).postln;
    "====================================================".postln;
  }

}
