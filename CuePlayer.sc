
CuePlayer : Cues {

  var <name;
  var <>clock, <guiInstance;
  var <timelineRegister, oscTriggerFunc;

  *new { arg name;
    ^super.newCopyArgs(name).init;
  }

  init {
    super.init;
    clock = TempoClock(120/60, queueSize: 2048 * 2).permanent_(true);
    MIDIIn.connectAll;
    timelineRegister = IdentityDictionary.new;
  }

  add { arg function, cueNumber, timeline, timelineOptions = ();
    timeline = timeline.asTimeline(clock, timelineOptions);
    this.addTimelineToRegister(cueNumber, timeline);
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

  plot { arg cue = current; var timeline;
    timeline = timelineRegister[cue.asSymbol];
    if(timeline.notNil) {timeline.plot};
  }

  /* External Control */

  midiTrigger { arg note = 60, channel = 15;
    MIDIFunc.noteOn({ arg vel, noteNum, chan;
      if (note == noteNum && chan == channel, {
        /*[vel, noteNum, chan].postln;*/
        {this.trigger(vel-1)}.defer;
      });
    }).permanent = true;
  }

  oscTrigger { arg message = 1, path = '/cueTrigger';
    oscTriggerFunc = OSCFunc(
      { arg msg; if (msg[1] == message, {{this.next}.defer}); },
			path
    );
    oscTriggerFunc.permanent = true;
    this.oscTriggerInform(path, message);
    ^oscTriggerFunc;
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

  oscTriggerInform { arg path, message;
    "====================================================".postln;
    ("CuePlayer OSC trigger:").postln;
    (" with path " ++ path).postln;
    ("message: " ++ message).postln;
    "====================================================".postln;
  }

  addTimelineToRegister { arg cueNumber, timeline; var registerNumber;
    registerNumber = if(cueNumber.isNil && timeline.notNil, {cueList.size.asSymbol}, {cueNumber.asSymbol});
    timelineRegister.put(registerNumber, timeline);
  }

}
