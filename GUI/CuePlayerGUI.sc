
CuePlayerGUI {

  var cuePlayer, monitorInChannels, monitorOutChannels, options; 
  var <window;
  var name, clock;
  var timer, trigButton, pauseButton, cueNumberDisplay, <metronome, 
      serverInfoRoutine, lrgCueWin, largeCueNumberDisplay, muteButton;
  var font, titleFontSize, marginTop, <active = false;

  /* Server and Routing */
  var outputLevels, <inputLevels, oscInputLevels, oscOutLevels;
  var <groupA, <groupB,  <groupZ;

  *new { arg cuePlayer, monitorInChannels = 2, monitorOutChannels = 8, options = ();
    ^super.newCopyArgs(cuePlayer, monitorInChannels.clip(2, 8), monitorOutChannels.clip(1, 256), options).init;
  }

  init {
    clock = cuePlayer.clock;
    name = cuePlayer.name ?? "Cue Player";

    this.setDefaultOptions;
    this.initStyleVariables;
    this.createMainWindow;
    this.createInputLevels;
    this.createCueTrigger;
    this.createTimer;
    this.createMetronome;
    this.createOutputLevels;
    this.initServerResources;
    this.createServerControls;
    this.registerShortcuts;
    this.setCmdPeriodActions;

    active = true;
    window.front;
  }

  setDefaultOptions {
    options.monitorInOffset ?? { options.monitorInOffset = 0 };
    options.largeDisplay ?? { options.largeDisplay = false };
    options.left ?? { options.left = 1400 };
    options.top ?? { options.top = 650 };
  }

  setCmdPeriodActions {
    CmdPeriod.add({
      AppClock.sched(0.1, {
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
    window = Window.new(name, Rect(options.left, options.top, 282, 330 + this.calculateLevelSumHeight + (marginTop*4)), resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      metronome.clear;
      timer.stop;
      serverInfoRoutine.stop;
      cuePlayer.removeDependant(this);
      oscOutLevels.free;
      oscInputLevels.free;
      outputLevels.free;
      inputLevels.free;
      if (lrgCueWin.notNil and: {lrgCueWin.isClosed.not}) {lrgCueWin.close};
      active = false;
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

  registerShortcuts {
    window.view.keyDownAction = { 
      arg view, char, modifiers, unicode, keycode; 
      /* [char, modifiers, unicode, keycode].postln; */ 
      switch(unicode)
      {32} { trigButton.doAction(0) } //space
      {109} {} // m
      {77} {} //M
      {112} {} //p
    }; 
  }

  /* -------- */

  createInputLevels { var inLevelArray;
    this.createLabel("Input meters").align_(\left);
    inLevelArray = Array.newClear(monitorInChannels);
    monitorInChannels.do{ arg i;
      inLevelArray[i] = this.createInputLevel;
      this.createLabel("  " ++ (i+1+options.monitorInOffset), 20).align_(\center);
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
    if (options.largeDisplay, { this.createLargeCueNumberDisplay })
  }

  createLabel { arg text = "placeholder text", width = 280, height = 20; var label;
    label = StaticText(window, Rect( width: width, height: height));
    label.font_(Font(font, titleFontSize));
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_(text);
    ^label;
  }

  createTriggerButton {
    trigButton = Button(window, Rect(10, 200, 220, 60));
    trigButton.font_(Font(font, 12));
    trigButton.canFocus = false;
    trigButton.states_([["Next Cue / FootSwitch", Color.white, Color.fromHexString("#1DA34D")]]);
    trigButton.action_(
      {
        var cueNum;
        cueNum = cuePlayer.next;
        if (timer.isPlaying.not) {timer.play; pauseButton.value_(1)};
      }
    );
  }

  createCueNumberDisplay {
    cueNumberDisplay = NumberBox(window, Rect(width: 50, height: 60)).align_(\center);
    cueNumberDisplay.value = cuePlayer.current;
    cueNumberDisplay.font_(Font(font, 26));
    cueNumberDisplay.action = {
      arg box;
      cuePlayer.current = box.value.abs;
      timer.stop;
      if(box.value == 0){timer.stop; timer.cursecs_(0)};
      if (options.largeDisplay, { largeCueNumberDisplay.string = box.value })
    };
  }

  createLargeCueNumberDisplay { arg widthHeight = 950;
    lrgCueWin = Window.new("Huge Cue Number", Rect(1259, 900, widthHeight, widthHeight)).front;
    lrgCueWin.background = Color.black;
    largeCueNumberDisplay =  StaticText(lrgCueWin, Rect(width: widthHeight, height: widthHeight)).align_(\center);
    largeCueNumberDisplay.font_(Font(font, widthHeight * 0.73)).stringColor_(Color.white);
    largeCueNumberDisplay.string = cuePlayer.current;
  }

  /* Timer */

  createTimer { var stopButton;
    this.createLabel("", 282, marginTop);
    this.createLabel("Timer");
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

  createMetronome{
    this.createLabel("", 282, marginTop);
    metronome = MetronomeCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
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

  /* Master */

  createServerControls { var volSlider, spec, peakCPULabel, numSynthsLabel;
    this.createLabel("Master Level").align_(\left);
    muteButton = Button(window, Rect(width: 80, height: 20) );
    muteButton.states = [["Mute", Color.white, Color.grey], ["Unmute", Color.white,  Color(0.9, 0.5, 0.3)]];
    muteButton.canFocus = false;
    muteButton.font_(Font(font, titleFontSize));
    if(Server.default.volume.isMuted){muteButton.value = 1};
    muteButton.action = { arg button;
      if(button.value == 0,
        {Server.default.unmute},
        {Server.default.mute}
      );
    };
    volSlider = Slider(window, Rect(width: 190, height: 20) ).background_(Color.fromHexString("#A0A0A0"));
    spec = ControlSpec(0.ampdb, 2.ampdb, \db, units: " dB");
    volSlider.value = spec.unmap(0);
    volSlider.canFocus = false;
    volSlider.action = { arg slider;
      Server.default.volume = spec.map(slider.value);
    };
    this.createLabel("", 282, marginTop);
    peakCPULabel = this.createLabel("Peak CPU : " ++ Server.local.peakCPU.round(0.1) ++ " %", width: 134, height: 20).align_(\left).stringColor_(Color.white);
    numSynthsLabel = this.createLabel("Synths : " ++ Server.local.numSynths, width: 134, height: 20).align_(\right).stringColor_(Color.white);
    serverInfoRoutine = Routine{ 
      inf.do{
        peakCPULabel.string = "Peak CPU : " ++ Server.local.peakCPU.round(0.1) ++ " %";
        numSynthsLabel.string = "Synths : " ++ Server.local.numSynths;
        0.1.wait;
      }
    }.play(AppClock);
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

      sig = SoundIn.ar( monitorInChannels.collect{arg i; i+options.monitorInOffset});
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
    {metronome.bpm = theChanged.clock.tempo*60}
  }

  setCurrent { arg val;
    cueNumberDisplay.value = val;
    if (options.largeDisplay, { largeCueNumberDisplay.string = val });
  }

}
