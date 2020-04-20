
CuePlayer : Cues {

  classvar <globalClock;
  var <>clock, <guiInstance;
  var <timelineRegister, oscTriggerFunc;
  var midiFuncRegister;
  var <fastForwardTimeline;

  *new {
    ^super.new.init;
  }

  *initClass {
    globalClock = TempoClock(120/60, queueSize: 2048 * 2).permanent_(true);
  }

  init {
    super.init;
    clock = TempoClock(120/60, queueSize: 2048 * 2).permanent_(true);
    timelineRegister = IdentityDictionary.new;
    midiFuncRegister = Array.new;
  }

  add { arg function, timeline, timelineOptions = ();
  timeline = timeline.asTimeline(clock, timelineOptions);
  this.addTimelineToRegister(cueList.size+1, timeline);
  ^super.add(function, {timeline.play;});
}

put { arg cueNumber, function, timeline, timelineOptions = ();
timeline = timeline.asTimeline(clock, timelineOptions);
this.addTimelineToRegister(cueNumber, timeline);
^super.put(cueNumber, function, {timeline.play;});
  }

  stop {
    clock.clear;
  }

  gui {arg monitorInChannels = 2, monitorOutChannels = 8, options;
    if (guiInstance.isNil or: {guiInstance.active.not},
    {
      guiInstance = CuePlayerGUI(this, monitorInChannels, monitorOutChannels, options);
      this.addDependant(guiInstance);
    }, {"The GUI for this CuePlayer is already active".warn;})
  }

  tempo_ { arg bpm = 120;
    ("CuePlayer tempo set to " ++ bpm ++ " bpm").postln;
    clock.tempo = bpm/60;
    this.changed(\tempo);
    ^bpm;
  }

  tempo {
    ^clock.tempo*60
  }

  plot { arg cueNumber = current; var timeline;
    timeline = timelineRegister[cueNumber.asSymbol];
    if(timeline.notNil) {timeline.plot};
  }

  disableLiveReload {
    timelineRegister.do{arg i; i.options.liveReload = false};
    this.liveReload = false;
    "Live reload disabled".postln;
  }

  enableLiveReload {
    timelineRegister.do{arg i; i.options.liveReload = true};
    this.liveReload = true;
    "Live reload enabled".postln;
  }

  /* External Control */

  midiTrigger { arg note = 60, channel = 15; var msg;
    "\n==============".postln;
    msg = "\nThe method midiTrigger has been deprecated";
    msg = msg ++ "\nand will be removed. Please use\nmidiTriggerNoteOn instead.";
    msg.warn;
    "==============\n".postln;
    this.midiTriggerNoteOn(note, channel);
  }

  midiTriggerNoteOn { arg note = 60, channel = 15; var func;
    func = { arg vel, noteNum, chan;
      chan = chan + 1;
      if (note == noteNum && chan == channel, {
        /* [chan, noteNum, vel].debug("midiTriggerNoteOn"); */
        {this.next}.defer;
      });
    };
    this.addMIDIFunc(MIDIFunc.noteOn(func).fix);
  }

  midiTriggerVelocity { arg note = 60, channel = 16, offset = 0; var func;
    func = { arg vel, noteNum, chan;
      chan = chan + 1;
      if (note == noteNum && chan == channel, {
        /* [chan, noteNum, vel].debug("midiTriggerVelocity"); */
        {this.trigger((vel)+offset)}.defer;
      });
    };
    this.addMIDIFunc(MIDIFunc.noteOn(func).fix);
  }

  midiTriggerControl { arg value = 0, ccNum = 64, channel = 1 ; var func;
    func = { arg val, controller, chan;
      chan = chan + 1;
      if (value == val && ccNum == controller && channel == chan, {
        {this.next}.defer;
      });
    };
    this.addMIDIFunc(MIDIFunc.cc(func).fix);
  }

  addMIDIFunc { arg midiFunc;
    midiFuncRegister = midiFuncRegister.add(midiFunc);
  }

  clearMIDI {
    midiFuncRegister.do{ arg midiFunc; midiFunc.free};
  }

  oscTrigger { arg path = '/cueTrigger';
    oscTriggerFunc = OSCFunc(
      { arg msg;
        if (msg[1] == -1,
        { {this.next}.defer },
        { {this.trigger(msg[1])}.defer }
      );
    }, path
  );
  oscTriggerFunc.permanent = true;
  this.oscTriggerInform(path);
  ^oscTriggerFunc;
}

freeOscTrigger {
  oscTriggerFunc.free;
  "The OSC trigger has been removed".postln;
}

sendOSC { arg ip = "127.0.0.1", port = 8000, msg = ["/play", 1]; var address;
address = NetAddr(ip, port);
this.sched(clock.timeToNextBeat,{address.sendMsg(msg[0], msg[1])});
  }

  useGlobalClock {
    clock = globalClock;
    "This instance of CuePlayer is now using the global clock.".postln;
  }

  fastForward {arg interval = 10, timelineOptions;
    var time = 0;
    fastForwardTimeline = Timeline.new(clock, timelineOptions);
    (cueList.size).do{ arg idx; 
      var cueNumber = idx + 1;
      var timeline = timelineRegister[cueNumber.asSymbol];
      if(timeline.notNil){
        timeline.functionList.pairsDo{ arg i, function;
          fastForwardTimeline.add(time, function);
          time = time + interval;
        };
      }{
        //if cue only contains a function
        fastForwardTimeline.add(time, cueList[idx]);
        time = time + interval;
      };
    };
    "Starting fastForward".postln;
    fastForwardTimeline.play; 
  }

  stopFastForward {
    fastForwardTimeline.stop;
    "Stopped fastForward".postln;
  }

  /* Private */

  sched { arg time, function;
    Routine {
      time.wait;
      Server.default.makeBundle(Server.default.latency, function);
    }.play(clock);
  }

  oscTriggerInform { arg path, message;
    ("CuePlayer OSC trigger:").postln;
    (" with path " ++ path).postln;
    (" message: -1 for next cue or any number for specific cues").postln;
  }

  addTimelineToRegister { arg cueNumber, timeline; var registerNumber;
    registerNumber = if(cueNumber.isNil && timeline.notNil, {cueList.size.asSymbol}, {cueNumber.asSymbol});
    timelineRegister.put(registerNumber, timeline);
  }

}
