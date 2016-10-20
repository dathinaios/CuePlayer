
CuePlayer : Cues {

  classvar <globalClock;
  var <>clock, <guiInstance;
  var <timelineRegister, oscTriggerFunc;
  var midiFuncRegister;

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
    MIDIIn.connectAll;
    this.midiTrigger;
    this.midiTriggerVelocity;
    this.oscTrigger;
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

  midiTrigger { arg note = 60, channel = 14; var func;
    func = { arg src, chan, noteNum, vel;
      chan = chan + 1;
      /* [chan, noteNum, vel].postln; */
      if (note == noteNum && chan == channel, {
        {this.next}.defer;
      });
    };
    midiFuncRegister.add(func);
    MIDIIn.addFuncTo(\noteOn, func);
  }

  midiTriggerVelocity { arg note = 60, channel = 15, offset = 0; var func;
    func = { arg src, chan, noteNum, vel;
      chan = chan + 1;
      /* [chan, noteNum, vel].postln; */
      if (note == noteNum && chan == channel, {
        {this.trigger((vel-1)+offset)}.defer;
      });
    };
    midiFuncRegister.add(func);
    MIDIIn.addFuncTo(\noteOn,func);
  }

  clearMIDI {
    midiFuncRegister.do{ arg func; MIDIIn.removeFuncFrom(\noteOn, func)}
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

  sendOSC { arg ip = "127.0.0.1", port = 8000, msg = ["/play", 1]; var address;
    address = NetAddr(ip, port);
		this.sched(clock.timeToNextBeat,{address.sendMsg(msg[0], msg[1])});
  }

  useGlobalClock {
    clock = globalClock;
    "This instance of CuePlayer is now using the global clock.".postln;
  }

  /* Private */

  sched { arg time, function;
    Routine {
      time.wait;
      Server.default.makeBundle(Server.default.latency, function);
    }.play(clock);
  }

  oscTriggerInform { arg path, message;
    "====================================================".postln;
    ("CuePlayer OSC trigger:").postln;
    (" with path " ++ path).postln;
    (" message: " ++ message).postln;
    "====================================================".postln;
  }

  addTimelineToRegister { arg cueNumber, timeline; var registerNumber;
    registerNumber = if(cueNumber.isNil && timeline.notNil, {cueList.size.asSymbol}, {cueNumber.asSymbol});
    timelineRegister.put(registerNumber, timeline);
  }

}
