
CuePlayerGUI { 

  var cuePlayer, monitorInChannels, monitorOutChannels;
  var cues, name, clock;
  var timer, timerState = \stopped, cueNumberDisplay, bigTextCueNum;
  var <window, pdefText, reaperAddr;
  var input1 = 8,  input2 = 8;

  /* Server and Routing */
  var outputLevels, oscOutLevels, inputLevels;
  var <groupA, <groupB,  <groupZ;

  *new { arg cuePlayer, monitorInChannels = 2, monitorOutChannels = 8;
    ^super.newCopyArgs(cuePlayer, monitorInChannels, monitorOutChannels).init;
  }

  init {
    cues = cuePlayer.cues;
    clock = cuePlayer.clock;
    name = cuePlayer.name ?? "Cue Player";
    /* ----------- */
    this.createMainWindow;
    this.createInputLevels;
    this.createCueTrigger;
    this.createTimer;
    this.createMetronome;
    /* this.createExternalOSC; */
    this.createOutputLevels(monitorOutChannels);
    /* ----------- */
    this.initServerResources;
    window.front;
  }

  createMainWindow {
    window = Window.new(name, Rect(1400, 650, 290, 450));
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color(0.8, 0.8, 0.8));
    window.onClose = { 
      Pdef(\metronome).clear;
      cues.removeDependant(this);
      oscOutLevels.free; 
      outputLevels.free;
      inputLevels.free;
    };
  }

  /* -------- */

  createInputLevels { var level1, level2;
    this.createLabel("Input meters  / Define Ins using the number boxes");
    level1 = this.createInputLevel;
    this.createInputLevelBox(\in1);
    level2 = this.createInputLevel;
    this.createInputLevelBox(\in2);
    this.createOSCFuncForLevels(level1, level2);
  }

  createInputLevel { var level;
    level = LevelIndicator(window, Rect(width:  220, height: 20));
    level.warning = -2.dbamp;
    level.critical = -1.dbamp;
    level.drawsPeak = true;
    level.background = Color.black;
    level.numTicks = 11;
    level.numMajorTicks = 3;
    ^level;
  }

  createInputLevelBox { arg in; var box;
    box = NumberBox(window, Rect(240, 25, 50, 20)).align_(\center);
    box.background_(Color(0.9, 0.9, 0.9));
    box.normalColor_(Color.black);
    box.value = input1;
    { box.action = {arg inval;
        inputLevels.set(in, inval.value);
      }
    }.defer(0);
    ^box;
  }

  createOSCFuncForLevels { arg level1, level2; var oscInputLevels;
    oscInputLevels = OSCFunc({arg msg;
      {
        level1.value = msg[3].ampdb.linlin(-75, 0, 0, 1);
        level1.peakLevel = msg[4].ampdb.linlin(-75, 0, 0, 1);
        level2.value = msg[5].ampdb.linlin(-75, 0, 0, 1);
        level2.peakLevel = msg[6].ampdb.linlin(-75, 0, 0, 1);
      }.defer;
    }, '/levels', Server.default.addr);
    oscInputLevels.permanent = true;
  }

  /* Cue Trigger */

  createCueTrigger {     
    this.createLabel("Trigger / Display & Reset Cue-number");
    this.createTriggerButton;
    this.createCueNumberDisplay;
  }

  createLabel { arg text = "placeholder text", width = 280; var label;
    label = StaticText(window, Rect( width: width, height: 20));
    label.font_(Font("Arial", 11));
    label.stringColor_(Color.black);
    label.string_(text);
    ^label;
  }

  createTriggerButton { var trigButton;
    trigButton = Button(window, Rect(10, 200, 220, 60));
    trigButton.font_(Font("Arial", 12));
    trigButton.states_([["Next Cue / FootSwitch", Color.white, Color(0.2, 0.6, 0.1)]]);
    trigButton.action_(
      { 
        var cueNum; 
        cueNum = cues.next;
        switch (timerState)
        {\paused} {timer.play; timerState = \playing}
        {\stopped} {timer.play; timerState = \playing};
      }
    );
  }

  createCueNumberDisplay {
    cueNumberDisplay = NumberBox(window, Rect(width: 50, height: 60)).align_(\center);
    cueNumberDisplay.value = cues.current;
    cueNumberDisplay.font_(Font("Arial", 26));
    cueNumberDisplay.action = {
      arg box; 
      cues.current = box.value;
      timer.stop; 
      timerState = \paused;
    };
  }

  createLargeCueNumberDisplay { arg widthHeight = 950;
      var bigCueWin;
      bigCueWin = Window.new("Huge Cue Number", Rect(1259, 900, widthHeight, widthHeight)).front;
      bigCueWin.background = Color.black;
      // display cue-number
      bigTextCueNum =  StaticText(bigCueWin, Rect(width: widthHeight, height: widthHeight)).align_(\center);
      bigTextCueNum.font_(Font("Arial", widthHeight * 0.73)).stringColor_(Color.white);
  }

  /* Timer */

  createTimer { var pauseButton, stopButton;
    timer = ClockFace2.new(window);
    pauseButton = Button(window, Rect(width: 30, height: 20) );
    pauseButton.states = [ ["||", Color.white, Color.grey]];
    pauseButton.font_(Font("Arial", 11));
    pauseButton.action = { arg butState; timer.stop; timerState = \paused};
    stopButton = Button(window, Rect(width: 30, height: 20) );
    stopButton.states = [ ["[]", Color.white, Color.grey]];
    stopButton.font_(Font("Arial", 11));
    stopButton.action = { arg butState; timer.stop; timer.cursecs_(0); timerState = \stopped};
  }

  /* Metronome */

  createMetronome { var but_Metro, spec_Metro, metroOutBox, metroOut, metro_Vol, slid_Metro;
    metroOut = 1; // default output bus for metronome
    metro_Vol = 0.1; // default volume
    this.createLabel("Metronome / Metro Vol / Metro Bus");
    but_Metro = Button(window, Rect(width: 80, height: 20) ); // 2 arguments: ( which_Window, bounds )
    but_Metro.states = [ ["Metro", Color.white, Color.grey], ["Metro", Color.white, Color(0.9, 0.5, 0.3)]];
    but_Metro.font_(Font("Arial", 11));
    but_Metro.action = { arg butState;
      if ( butState.value == 1, {
        Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut )).play(clock, quant:[1]); 
      });
      if ( butState.value == 0, { Pdef(\metronome).clear});
    };
    // metronomes volume slider
    slid_Metro = Slider(window, Rect(width: 82, height: 20) );
    spec_Metro = ControlSpec(minval: 0, maxval: 0.5);
    slid_Metro.value = metro_Vol;
    slid_Metro.action = { 
      metro_Vol = spec_Metro.map(slid_Metro.value);
      // while moving the slider , only evaluate the Pdef when it is already playing
      if (Pdef(\metronome).isPlaying == true, { 
        Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut )).play(clock, quant:[1])
      });
    };

    // defines metronomes output bus
    metroOutBox = NumberBox(window, Rect(width:50, height:20)).align_(\center);
    metroOutBox.background_(Color(0.9, 0.9, 0.9));
    metroOutBox.normalColor_(Color.black);

    metroOutBox.value = metroOut;
    metroOutBox.action = {arg inval; metroOut = inval.value;
      Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut ))
    };
  }

  /* Output Levels */

  createOutputLevels{ arg numOfOutChannels = 8; var outlev, cycle = 0;
    this.createLabel("Monitor Outputs");
  
    outlev = Array.newClear(numOfOutChannels);
    numOfOutChannels.do{ arg i;
      outlev[i] = LevelIndicator(window, Rect(width:  30, height: 50) );
      outlev[i].warning = -2.dbamp;
      outlev[i].critical = -1.dbamp;
      outlev[i].drawsPeak = true;
      outlev[i].background = Color.grey;
      outlev[i].numTicks = 11;
      outlev[i].numMajorTicks = 3;
      if ((i+1)%8 == 0, { var chanNumOffset = 0;
        chanNumOffset = cycle * 8;
        8.do{ arg index;
          this.createLabel(index+1+chanNumOffset, 30).align_(\center);
        };
        cycle = cycle + 1;
      });
    };

    oscOutLevels =
      OSCFunc({arg msg; {
        outlev[0].value = msg[3].ampdb.linlin(-75, 0, 0, 1);
        outlev[0].peakLevel = msg[4].ampdb.linlin(-75, 0, 0, 1);

        outlev[1].value = msg[5].ampdb.linlin(-75, 0, 0, 1);
        outlev[1].peakLevel = msg[6].ampdb.linlin(-75, 0, 0, 1);

        outlev[2].value = msg[7].ampdb.linlin(-75, 0, 0, 1);
        outlev[2].peakLevel = msg[8].ampdb.linlin(-75, 0, 0, 1);

        outlev[3].value = msg[9].ampdb.linlin(-75, 0, 0, 1);
        outlev[3].peakLevel = msg[10].ampdb.linlin(-75, 0, 0, 1);

        outlev[4].value = msg[11].ampdb.linlin(-75, 0, 0, 1);
        outlev[4].peakLevel = msg[12].ampdb.linlin(-75, 0, 0, 1);

        outlev[5].value = msg[13].ampdb.linlin(-75, 0, 0, 1);
        outlev[5].peakLevel = msg[14].ampdb.linlin(-75, 0, 0, 1);

        outlev[6].value = msg[15].ampdb.linlin(-75, 0, 0, 1);
        outlev[6].peakLevel = msg[16].ampdb.linlin(-75, 0, 0, 1);

        outlev[7].value = msg[17].ampdb.linlin(-75, 0, 0, 1);
        outlev[7].peakLevel = msg[18].ampdb.linlin(-75, 0, 0, 1);
      }.defer; }, '/out_levels', Server.default.addr);

    /* CmdPeriod.add({SystemClock.sched(0.1, { */
    /*   oscOutLevels.value; */
    /*   outputLevels = Synth(\outputLevels, target: groupZ); */
    /* })}); */
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
      arg in1 = 0, in2 = 1;
      var trig, sig, delayTrig;

      sig = SoundIn.ar( [in1, in2] );
      trig = Impulse.kr(10);
      delayTrig = Delay1.kr(trig);

      SendReply.kr(trig, '/levels', [
        Amplitude.kr( sig[0] ), // rms of signal1
        K2A.ar(Peak.ar( sig[0], delayTrig).lag(0, 3)), // peak of signal1
        Amplitude.kr( sig[1] ), // rms of signal2
        K2A.ar(Peak.ar( sig[1] , delayTrig).lag(0, 3)), // peak of signal2
      ]);
    }).add;
    SynthDef(\outputLevels, {
      var trig, sig, delayTrig;

      sig = In.ar( [0,1,2,3,4,5,6,7] );
      trig = Impulse.kr(10);
      delayTrig = Delay1.kr(trig);

      SendReply.kr(trig, '/out_levels', [

        Amplitude.kr( sig[0] ), // rms of signal1
        K2A.ar(Peak.ar( sig[0], delayTrig).lag(0, 3)), // peak of signal1

        Amplitude.kr( sig[1] ), // rms of signal2
        K2A.ar(Peak.ar( sig[1] , delayTrig).lag(0, 3)), // peak of signal2

        Amplitude.kr( sig[2] ),
        K2A.ar(Peak.ar( sig[2] , delayTrig).lag(0, 3)),

        Amplitude.kr( sig[3] ),
        K2A.ar(Peak.ar( sig[3] , delayTrig).lag(0, 3)),

        Amplitude.kr( sig[4] ),
        K2A.ar(Peak.ar( sig[4] , delayTrig).lag(0, 3)),

        Amplitude.kr( sig[5] ),
        K2A.ar(Peak.ar( sig[5] , delayTrig).lag(0, 3)),

        Amplitude.kr( sig[6] ),
        K2A.ar(Peak.ar( sig[6] , delayTrig).lag(0, 3)),

        Amplitude.kr( sig[7] ),
        K2A.ar(Peak.ar( sig[7] , delayTrig).lag(0, 3)),

      ]);
    }).add;
    SynthDef(\metronome, {arg amp = 0.2, freq = 800, out = 0;
      Out.ar(
        out, 
        FSinOsc.ar(freq: freq, mul: amp * EnvGen.kr(Env.perc(attackTime:0.001, releaseTime:0.2),doneAction:2))
      );
    }).add;
  }

  runSynths {
    { inputLevels = Synth(\inputLevels, [\in1, input1, \in2, input2], target: groupA )}.defer(1);
    { outputLevels = Synth(\outputLevels, target: groupZ) }.defer(1);
  }

  /* OSC */

  createExternalOSC { var but_Reaper; var n;
    /* reaperAddr = "192.168.1.2"; // needed only while composing */
    // This starts and pauses Reapers playback
    n = NetAddr(reaperAddr, 8000); // define IP-address + port number
    // set the same within Reaper  Preferences  Control Surfaces

    but_Reaper = Button(window, Rect(width: 48, height: 20) ); // 2 arguments ( which_Window, bounds )
    but_Reaper.states = [ ["Reaper", Color.white, Color.grey], ["Reaper", Color.white, Color(0.9, 0.5, 0.3)]];
    but_Reaper.font_(Font("Arial", 11));

    // schedules to send the OSC command at the next beat in order to be in sync with Reaper
    but_Reaper.action = { arg butState;
      if ( butState.value == 1, { SystemClock.sched(clock.timeToNextBeat , { n.sendMsg("/play", 1); } ) }); // ti works, unknown why
      if ( butState.value == 0, {SystemClock.sched(clock.timeToNextBeat , { n.sendMsg("/pause", 1); } ) });
    };
  }

  /* Handle Events from Dependants */

  update { arg theChanged, message;
    switch (message)
    {\current}
    {this.setCurrent(theChanged.current)};
  }

  setCurrent { arg val;
    cueNumberDisplay.value = val;
  }

}
