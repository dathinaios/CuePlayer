
CuePlayerGUI {

  var cuePlayer, monitorInChannels, monitorOutChannels, largeDisplay;
  var cues, name, clock;
  var timer, pauseButton, cueNumberDisplay, bpm, lrgCueWin, largeCueNumberDisplay;
  var <window, pdefText, reaperAddr;
  var font, titleFontSize, marginTop;

  /* Server and Routing */
  var outputLevels, inputLevels, oscInputLevels, oscOutLevels;
  var <groupA, <groupB,  <groupZ;

  *new { arg cuePlayer, monitorInChannels = 2, monitorOutChannels = 8, largeDisplay = false;
    ^super.newCopyArgs(cuePlayer, monitorInChannels, monitorOutChannels, largeDisplay).init;
  }

  init {
    cues = cuePlayer.cues;
    clock = cuePlayer.clock;
    name = cuePlayer.name ?? "Cue Player";

    this.initStyleVariables;
    this.createMainWindow;
    this.createInputLevels;
    this.createCueTrigger;
    this.createTimer;
    this.createMetronome;
    this.createBpmField;
    this.createOutputLevels;
    this.initServerResources;
    this.setCmdPeriodActions;

    window.front;
  }

  setCmdPeriodActions {
    CmdPeriod.add({
      SystemClock.sched(0.1, {
        this.initServerResources;
      });
    });
  }

  initStyleVariables {
    font = "Lucida Grande";
    titleFontSize = 11;
    marginTop = 3;
  }

  createMainWindow {
    window = Window.new(name, Rect(1400, 650, 282, 250 + this.calculateLevelSumHeight + (marginTop*4)), resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      Pdef(\metronome).clear;
      timer.stop;
      cues.removeDependant(this);
      oscOutLevels.free;
      oscInputLevels.free;
      outputLevels.free;
      inputLevels.free;
      if (lrgCueWin.notNil and: {lrgCueWin.isClosed.not}) {lrgCueWin.close};
    };
  }

  calculateLevelSumHeight { 
    var outHeightSum, inHeightSum, labelHeight = 20, outMeterHeight = 50, inMeterHeight = 20;
    labelHeight = labelHeight + 5;
    outMeterHeight = outMeterHeight;
    inMeterHeight = inMeterHeight + 5;
    outHeightSum = ((monitorOutChannels/8).roundUp(1))*(outMeterHeight+labelHeight);
    inHeightSum = monitorInChannels*inMeterHeight;
    ^(outHeightSum + inHeightSum);
  }

  /* -------- */

  createInputLevels { var inLevelArray;
    this.createLabel("Input meters").align_(\left);
    inLevelArray = Array.newClear(monitorInChannels);
    monitorInChannels.do{ arg i;
      inLevelArray[i] = this.createInputLevel;
      this.createLabel("  " ++ (i+1), 20).align_(\center);
    };
    oscInputLevels = OSCFunc({arg msg; var curMsg = 3;
      {
        monitorInChannels.do{ arg chan;
          inLevelArray[chan].value = msg[curMsg].ampdb.linlin(-75, 0, 0, 1);
          inLevelArray[chan].peakLevel = msg[curMsg+1].ampdb.linlin(-75, 0, 0, 1);
          curMsg = curMsg + 2;
        }
      }.defer;
    }, '/in_levels', Server.default.addr);
    oscInputLevels.permanent = true;
  }

  createInputLevel { var level;
    level = LevelIndicator(window, Rect(width:  220, height: 20));
    level.warning = -2.dbamp;
    level.critical = -1.dbamp;
    level.drawsPeak = true;
    level.background = Color.fromHexString("#A0A0A0");
    /* level.numTicks = 11; */
    /* level.numMajorTicks = 3; */
    ^level;
  }

  /* Cue Trigger */

  createCueTrigger {
    this.createLabel("", 282, marginTop);
    this.createLabel("Trigger / Display & Reset Cue-number").align_(\left);
    this.createTriggerButton;
    this.createCueNumberDisplay;
    if (largeDisplay, { this.createLargeCueNumberDisplay })
  }

  createLabel { arg text = "placeholder text", width = 280, height = 20; var label;
    label = StaticText(window, Rect( width: width, height: height));
    label.font_(Font(font, titleFontSize));
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_(text);
    ^label;
  }

  createTriggerButton { var trigButton;
    trigButton = Button(window, Rect(10, 200, 220, 60));
    trigButton.font_(Font(font, 12));
    trigButton.canFocus = false;
    trigButton.states_([["Next Cue / FootSwitch", Color.white, Color.fromHexString("#1DA34D")]]);
    trigButton.action_(
      {
        var cueNum;
        cueNum = cues.next;
        if (timer.isPlaying.not) {timer.play; pauseButton.value_(1)};
      }
    );
  }

  createCueNumberDisplay {
    cueNumberDisplay = NumberBox(window, Rect(width: 50, height: 60)).align_(\center);
    cueNumberDisplay.value = cues.current;
    cueNumberDisplay.font_(Font(font, 26));
    cueNumberDisplay.action = {
      arg box;
      cues.current = box.value.abs;
      timer.stop;
      if(box.value == 0){timer.stop; timer.cursecs_(0)};
      if (largeDisplay, { largeCueNumberDisplay.string = box.value })
    };
  }

  createLargeCueNumberDisplay { arg widthHeight = 950;
    lrgCueWin = Window.new("Huge Cue Number", Rect(1259, 900, widthHeight, widthHeight)).front;
    lrgCueWin.background = Color.black;
    largeCueNumberDisplay =  StaticText(lrgCueWin, Rect(width: widthHeight, height: widthHeight)).align_(\center);
    largeCueNumberDisplay.font_(Font(font, widthHeight * 0.73)).stringColor_(Color.white);
    largeCueNumberDisplay.string = cues.current;
  }

  /* Timer */

  createTimer { var stopButton;
    this.createLabel("", 282, marginTop);
    this.createLabel("Timer / Pause & Stop stopwatch");
    this.createLabel("", 8);
    timer = ClockFaceCP.new(window);

    pauseButton = Button(window, Rect(width: 22, height: 20) );
    pauseButton.states = [[">", Color.white, Color.grey], ["||", Color.white, Color.grey]];
    pauseButton.font_(Font(font, titleFontSize));
    pauseButton.canFocus = false;
    pauseButton.action = { arg button; 
      if(button.value == 0, 
        { timer.stop  },
        { timer.play  }
      );
    };
    stopButton = Button(window, Rect(width: 22, height: 20) );
    stopButton.states = [ ["[ ]", Color.white, Color.grey]];
    stopButton.font_(Font(font, titleFontSize));
    stopButton.canFocus = false;
    stopButton.action = { arg butState; timer.stop; timer.cursecs_(0); };
  }

  /* Metronome */

  createMetronome { var but_Metro, spec_Metro, metroOutBox, metroOut, metro_Vol, slid_Metro;
    metroOut = 1; // default output bus for metronome
    metro_Vol = 0.1; // default volume
    this.createLabel("", 282, marginTop);
    this.createLabel("Metronome / Metro Vol. / Metro Output / Bpm").align_(\left);
    but_Metro = Button(window, Rect(width: 80, height: 20) ); // 2 arguments: ( which_Window, bounds )
    but_Metro.states = [ ["Metro", Color.white, Color.grey], ["Metro", Color.white, Color(0.9, 0.5, 0.3)]];
    but_Metro.canFocus = false;
    but_Metro.font_(Font(font, titleFontSize));
    but_Metro.action = { arg butState;
      if ( butState.value == 1, {
        Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut - 1 )).play(clock, quant:[1]);
      });
      if ( butState.value == 0, { Pdef(\metronome).clear});
    };
    // metronomes volume slider
    slid_Metro = Slider(window, Rect(width: 82, height: 20) ).background_(Color.fromHexString("#A0A0A0"));
    spec_Metro = ControlSpec(minval: 0, maxval: 0.5);
    slid_Metro.value = metro_Vol;
    slid_Metro.canFocus = false;
    slid_Metro.action = {
      metro_Vol = spec_Metro.map(slid_Metro.value);
      // while moving the slider , only evaluate the Pdef when it is already playing
      if (Pdef(\metronome).isPlaying == true, {
        Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut - 1 )).play(clock, quant:[1])
      });
    };

    // defines metronomes output bus
    metroOutBox = NumberBox(window, Rect(width:50, height:20)).align_(\center);
    metroOutBox.background_(Color(0.9, 0.9, 0.9));
    metroOutBox.normalColor_(Color.black);

    metroOutBox.value = metroOut;
    metroOutBox.action = {arg box; metroOut = box.value;
      Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut - 1 ))
    };
  }

  createBpmField {
    bpm = NumberBox(window, Rect(width:50, height:20)).align_(\center);
    bpm.background_(Color(0.9, 0.9, 0.9));
    bpm.normalColor_(Color.black);
    bpm.value = 120;
    bpm.action = {arg box;
      cuePlayer.tempo(box.value);
    };
  }

  /* Output Levels */

  createOutputLevels{ var outlev, label;
    this.createLabel("", 282, marginTop);
    this.createLabel("Output meters").align_(\left);

    outlev = Array.newClear(monitorOutChannels);
    monitorOutChannels.do{ arg i; var compView;
      compView = CompositeView(window, Rect(width:  30, height: 70) );
      outlev[i] = LevelIndicator(compView, Rect(3, 0, width:  35, height: 50) );
      outlev[i].warning = -2.dbamp;
      outlev[i].critical = -1.dbamp;
      outlev[i].drawsPeak = true;
      outlev[i].background = Color.fromHexString("#A0A0A0");
      /* outlev[i].numTicks = 11; */
      /* outlev[i].numMajorTicks = 3; */
      label = StaticText(compView, Rect( 0, 50, width: 30, height: 20)).align_(\center);
      label.font_(Font(font, titleFontSize));
      label.stringColor_(Color.fromHexString("#A0A0A0"));
      label.string = i+1;
    };
    /* create oscfunction */
    oscOutLevels =
    OSCFunc({arg msg; var curMsg = 3;
      {
        monitorOutChannels.do{ arg chan;
          outlev[chan].value = msg[curMsg].ampdb.linlin(-75, 0, 0, 1);
          outlev[chan].peakLevel = msg[curMsg+1].ampdb.linlin(-75, 0, 0, 1);
          curMsg = curMsg + 2;
        }
      }.defer;
    }, '/out_levels', Server.default.addr);
    oscOutLevels.permanent = true;
  }

  /* Server Resources */

  initServerResources {
    this.initGroups;
    this.addSynths;
    this.runSynths;
  }

  initGroups {
    groupA = Group.head(Server.default);
    groupB = Group.after(groupA);
    groupZ = Group.tail(Server.default);
  }

  addSynths {
    SynthDef(\inputLevels, {
      var trig, sig, delayTrig;

      sig = SoundIn.ar( monitorInChannels.collect{arg i; i});
      trig = Impulse.kr(10);
      delayTrig = Delay1.kr(trig);

      SendReply.kr(trig, '/in_levels',
        monitorInChannels.collect{ arg i;
          [Amplitude.kr( sig[i] ), // rms of signal1
          K2A.ar(Peak.ar( sig[i], delayTrig).lag(0, 3))] // peak of signal1
        }.flatten;
      );
    }).add;
    SynthDef(\outputLevels, {
      var trig, sig, delayTrig;

      sig = In.ar( monitorOutChannels.collect{arg i; i});
      trig = Impulse.kr(10);
      delayTrig = Delay1.kr(trig);

      SendReply.kr(trig, '/out_levels',
        monitorOutChannels.collect{ arg i;
          [Amplitude.kr( sig[i] ), // rms of signal1
          K2A.ar(Peak.ar( sig[i], delayTrig).lag(0, 3))] // peak of signal1
        }.flatten;
      );
    }).add;
    SynthDef(\metronome, {arg amp = 0.2, freq = 800, out = 0;
      Out.ar(
        out,
        FSinOsc.ar(freq: freq, mul: amp * EnvGen.kr(Env.perc(attackTime:0.001, releaseTime:0.2),doneAction:2))
      );
    }).add;
  }

  runSynths {
    { inputLevels = Synth(\inputLevels, target: groupA )}.defer(1);
    { outputLevels = Synth(\outputLevels, target: groupZ) }.defer(1);
  }

  /* Handle Events from Dependants */

  update { arg theChanged, message;
    switch (message)
    {\current}
    {this.setCurrent(theChanged.current)}
    {\tempo}
    {bpm.value = theChanged.clock.tempo*60}
  }

  setCurrent { arg val;
    cueNumberDisplay.value = val;
    if (largeDisplay, { largeCueNumberDisplay.string = val });
  }

}
