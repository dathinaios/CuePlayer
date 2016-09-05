
CuePlayer { var <t, <n;

  *new { arg name;
    ^super.newCopyArgs(name).init;
  }

  init {
    this.initAll;
  }

  initAll {
    ~numOfChannels = 2; // Choose number of Speakers: 2,4,6 or 8
    /* ~numOfDelays = 8; // This is the total number of delays which are spread accordingly to ~numOfChannels */
    ~widthOfBigWin = 950; ~visibilityOfBigWin = 0; // Makes visible (0,1) the Big Cue Number Window
    ~instIN1 = 8 ; ~instIN2 = 8; // Define inputs
    /* ~vcLev = 1.2; // VCs gain */
    this.initCuePlayerResources;
    this.initStructures;
    this.initCuePlayer;
  }

  monitor8b{ arg window;
    { ~out_meter = Synth(\out_meter, target: ~groupZ) } .defer(0);
    ~outlev = Array.newClear(10);
    for (0, 7, { arg i; ~outlev[i] = LevelIndicator(window, Rect(width:  30, height: 50) );});

    // an OSC responder receiving information about the amplitude
    ~oscOutLevels = {
      OSCFunc({arg msg; {
        ~outlev[0].value = msg[3].ampdb.linlin(-75, 0, 0, 1);
        ~outlev[0].peakLevel = msg[4].ampdb.linlin(-75, 0, 0, 1);

        ~outlev[1].value = msg[5].ampdb.linlin(-75, 0, 0, 1);
        ~outlev[1].peakLevel = msg[6].ampdb.linlin(-75, 0, 0, 1);

        ~outlev[2].value = msg[7].ampdb.linlin(-75, 0, 0, 1);
        ~outlev[2].peakLevel = msg[8].ampdb.linlin(-75, 0, 0, 1);

        ~outlev[3].value = msg[9].ampdb.linlin(-75, 0, 0, 1);
        ~outlev[3].peakLevel = msg[10].ampdb.linlin(-75, 0, 0, 1);

        ~outlev[4].value = msg[11].ampdb.linlin(-75, 0, 0, 1);
        ~outlev[4].peakLevel = msg[12].ampdb.linlin(-75, 0, 0, 1);

        ~outlev[5].value = msg[13].ampdb.linlin(-75, 0, 0, 1);
        ~outlev[5].peakLevel = msg[14].ampdb.linlin(-75, 0, 0, 1);

        ~outlev[6].value = msg[15].ampdb.linlin(-75, 0, 0, 1);
        ~outlev[6].peakLevel = msg[16].ampdb.linlin(-75, 0, 0, 1);

        ~outlev[7].value = msg[17].ampdb.linlin(-75, 0, 0, 1);
        ~outlev[7].peakLevel = msg[18].ampdb.linlin(-75, 0, 0, 1);
      }.defer; }, '/out_levels', Server.default.addr)
    };

    ~oscOutLevels.value;

    // modify the look of the level indicators

    for (0, 7, { arg i;
      ~outlev[i].warning = -2.dbamp;
      ~outlev[i].critical = -1.dbamp;
      ~outlev[i].drawsPeak = true;
      ~outlev[i].background = Color.grey;
      ~outlev[i].numTicks = 11;
      ~outlev[i].numMajorTicks = 3;

    });

    StaticText(window, Rect(width:30, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("0").align_(\center);
    StaticText(window, Rect(width:30, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("1").align_(\center);
    StaticText(window, Rect(width:30, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("2").align_(\center);
    StaticText(window, Rect(width:30, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("3").align_(\center);
    StaticText(window, Rect(width:30, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("4").align_(\center);
    StaticText(window, Rect(width:30, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("5").align_(\center);
    StaticText(window, Rect(width:30, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("6").align_(\center);
    StaticText(window, Rect(width:30, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("7").align_(\center);


    CmdPeriod.add({SystemClock.sched(0.1, {

      ~oscOutLevels.value;
      ~out_meter = Synth(\out_meter, target: ~groupZ);

    })});

    window.onClose = {~oscOutLevels.free; ~out_meter.free};
  }

  initCuePlayer {
    var window, but_Reaper, but_Metro, slid_Metro, spec_Metro, box_Metro, box1Text, box2Text, metroText, pdefText, metroOutBox, reaperAddr;
    var level1, level2, boxIn1, boxIn2, boxInputText; // variables for level indicators

    t = nil;
    t = TempoClock(50/60).permanent_(true); // Define a Tempo to schedule everything according to this
    ~beatDur = 1/(50/60); // time multiplier, gives the duration of 1 beat
    reaperAddr = "192.168.1.2"; // needed only while composing

    // Building the Cue-player

    window = Window.new("Cue Player - FullMoon", Rect(1400, 650, 290, 450));
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color(0.8, 0.8, 0.8));
    //window.alwaysOnTop = true;
    ~cue = List.newClear(size: 100 ); // set total number of cues

    //a pattern that goes through the elements of the List (ie the Cues)
    ~cuePatternFunction = {arg offset1 = 0; Pseq(list: (1,2..~cue.size), repeats: inf, offset: offset1).asStream;};
    ~cuePattern = ~cuePatternFunction.value;

    // This synth constantly sends information about the signals amplitude to the language
    { ~sig_meter = Synth(\sig_meter, [\in1, ~instIN1, \in2, ~instIN2], target: ~groupA )}.defer(0);
    // for some reason I need to delay a little the evaluation of this in order to set the values of the above synth to the correct inputs

    // Level Indicators & input number boxes
    boxInputText = StaticText(window, Rect(width:350, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("Input meters  / Define Ins using the number boxes");

    // define 1st level-indicators
    level1 = LevelIndicator(window, Rect(width:  220, height: 20) );

    // The box defines the input bus of the 1st instrument
    // Changing the value here changes the input bus which is displayed
    boxIn1 = NumberBox(window, Rect(240, 25, 50, 20)).align_(\center);
    boxIn1.background_(Color(0.9, 0.9, 0.9));
    boxIn1.normalColor_(Color.black);

    boxIn1.value = ~instIN1;

    { boxIn1.action = {arg inval;
      ~sig_meter.set(\in1, inval.value); // choose input to display
    }}.defer(0);

    // define 2nd level-indicators
    level2 = LevelIndicator(window, Rect(width:  220, height: 20) );

    // an OSC responder receiving information about the amplitude of the 2 signals
    ~oscInputLevels = {OSCFunc({arg msg;
      {
        level1.value = msg[3].ampdb.linlin(-75, 0, 0, 1);
        level1.peakLevel = msg[4].ampdb.linlin(-75, 0, 0, 1);

        level2.value = msg[5].ampdb.linlin(-75, 0, 0, 1);
        level2.peakLevel = msg[6].ampdb.linlin(-75, 0, 0, 1);

      }.defer;
    }, '/levels', Server.default.addr).permanent = true;};

    ~oscInputLevels.value;

    // modify the look of the level indicators
    level1.warning = -2.dbamp;
    level1.critical = -1.dbamp;
    level1.drawsPeak = true;
    level1.background = Color.black;
    level1.numTicks = 11;
    level1.numMajorTicks = 3;

    level2.warning = -2.dbamp;
    level2.critical = -1.dbamp;
    level2.drawsPeak = true;
    level2.background = Color.black;
    level2.numTicks = 11;
    level2.numMajorTicks = 3;

    // The box defines the input bus of the 1st instrument
    // Changing the value here changes the input bus which is displayed
    boxIn2 = NumberBox(window, Rect(240, 50, 50, 20)).align_(\center);
    boxIn2.background_(Color(0.9, 0.9, 0.9));
    boxIn2.normalColor_(Color.black);
    boxIn2.value = ~instIN2;
    {boxIn2.action = {arg inval;
      ~sig_meter.set(\in2, inval.value) ; // choose input to display
    }}.defer(0);
    // More GUI elements
    box2Text = StaticText(window, Rect( width: 280, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("Trigger / Display & Reset Cue-number");

    ~trigButton = Button(window, Rect(10, 200, 220, 60)).font_(Font("Arial", 12))
    .states_([["Next Cue / FootSwitch", Color.white, Color(0.2, 0.6, 0.1)]]).action_(
      { 
        var cueNum; cueNum = ~cuePattern.next.postln; ~cue.at(cueNum).value;
        if (cueNum.value == 1) { ~clockFace2.cursecs_(0); ~clockFace2.play };
        {~box2.value=cueNum}.defer;
        ~bigTextCueNum.string = cueNum.value; // used in the big cue number window
      }
    );

    // display cuenumber
    ~box2 = NumberBox(window, Rect(width: 50, height: 60)).align_(\center);
    ~box2.font_(Font("Arial", 26));
    ~box2.action = {
      arg inval; ~cuePattern = ~cuePatternFunction.value(offset1: inval);
      if (inval.value == 0) { ~clockFace2.stop };
      ~bigTextCueNum.string = inval.value; // used in the big cue number window
    }; //resume from particular cue

    // The Timer
    ~clockFace2 = ClockFace2.new(window);
    /* Metronome SynthDef, handy to be used as clicktrack*/
    SynthDef(\metro, {arg amp = 0.2, freq = 800, out = 0;
      Out.ar(out, FSinOsc.ar(freq: freq, mul: amp * EnvGen.kr(Env.perc(attackTime:0.001, releaseTime:0.2),doneAction:2)))
    }).add;

    // Metronome GUI

    ~metroOut = 1; // default output bus for metronome
    ~metro_Vol = 0.1; // default volume

    metroText = StaticText(window, Rect(width: 290, height: 20)).font_(Font("Arial", 11))
    .stringColor_(Color.black).string_("Metronome / Metro Vol / Metro Bus / Start-Stop Reaper");

    but_Metro = Button(window, Rect(width: 80, height: 20) ); // 2 arguments: ( which_Window, bounds )
    but_Metro.states = [ ["Metro", Color.white, Color.grey], ["Metro", Color.white, Color(0.9, 0.5, 0.3)]];
    but_Metro.font_(Font("Arial", 11));


    but_Metro.action = { arg butState;
      if ( butState.value == 1, {
        Pdef(\metro, Pbind(\instrument, \metro, \amp, ~metro_Vol, \dur, 1, \freq, 800, \out, ~metroOut )).play(t, quant:[1]); 
      });
      if ( butState.value == 0, { Pdef(\metro).clear});
    };

    // metronomes volume slider
    slid_Metro = Slider(window, Rect(width: 82, height: 20) );
    spec_Metro = ControlSpec(minval: 0, maxval: 0.5);
    slid_Metro.value = ~metro_Vol;
    slid_Metro.action = { 
      ~metro_Vol = spec_Metro.map(slid_Metro.value);
      // while moving the slider , only evaluate the Pdef when it is already playing
      if (Pdef(\metro).isPlaying == true, { 
        Pdef(\metro, Pbind(\instrument, \metro, \amp, ~metro_Vol, \dur, 1, \freq, 800, \out, ~metroOut )).play(t, quant:[1])
      });
    };

    // defines metronomes output bus
    metroOutBox = NumberBox(window, Rect(width:50, height:20)).align_(\center);
    metroOutBox.background_(Color(0.9, 0.9, 0.9));
    metroOutBox.normalColor_(Color.black);

    metroOutBox.value = ~metroOut;
    metroOutBox.action = {arg inval; ~metroOut = inval.value;
      Pdef(\metro, Pbind(\instrument, \metro, \amp, ~metro_Vol, \dur, 1, \freq, 800, \out, ~metroOut ))
    };

    // This starts and pauses Reapers playback
    n = NetAddr(reaperAddr, 8000); // define IP-address + port number
    // set the same within Reaper  Preferences  Control Surfaces

    but_Reaper = Button(window, Rect(width: 48, height: 20) ); // 2 arguments ( which_Window, bounds )
    but_Reaper.states = [ ["Reaper", Color.white, Color.grey], ["Reaper", Color.white, Color(0.9, 0.5, 0.3)]];
    but_Reaper.font_(Font("Arial", 11));

    // schedules to send the OSC command at the next beat in order to be in sync with Reaper
    but_Reaper.action = { arg butState;
      if ( butState.value == 1, { SystemClock.sched(t.timeToNextBeat , { n.sendMsg("/play", 1); } ) }); // ti works, unknown why
      if ( butState.value == 0, {SystemClock.sched(t.timeToNextBeat , { n.sendMsg("/pause", 1); } ) });
    };

    // receive OSC from Pd, used for FootSwitch
    ~oscTrigBut = {OSCFunc({ arg msg, time, addr, recvPort;
      if ( msg[1] == 101, { { ~trigButton.valueAction = 0 }.defer; })
    }, '/from-Pd').permanent = true;};

    ~oscTrigBut.value;
    // receive MIDI from Reaper through IAC and trigger a Cue in SC
    /* It is used only while composing to auto trigger the cues. It doesnt harm to leave it as such*/

    ~midiFunc = {MIDIFunc.noteOn({ arg vel, noteNum, chan;
      if (vel == 127 && noteNum == 73 && chan == 15, { { ~trigButton.valueAction = 0 }.defer; });
    }).permanent = true;};

    ~midiFunc.value;

    // Run some things on Cmd+Period
    CmdPeriod.add({SystemClock.sched(0.1, {

      // create groups
      ~groupA = Group.head(Server.default);
      ~groupB = Group.after(~groupA);
      ~groupZ = Group.tail(Server.default);

      // ~oscTrigBut.value;
      // ~midiFunc.value;
      // ~oscInputLevels.value; // the osc responder

      ~sig_meter = Synth(\sig_meter, [\in1, ~instIN1, \in2, ~instIN2], target: ~groupA );
      // the synth which sends amplitude data

      ~clockFace2.stop;

      // Pdef.all.clear; // clear all pdefs
    })});

    StaticText(window, Rect(width:350, height: 20)).font_(Font("Arial", 11)).stringColor_(Color.black).string_("Monitor Outputs");
    this.monitor8b(window);
    /* Server.default.makeWindow(window); // embed the server window at the bottom */
    // BIG CUE NUMBER WINDOW, optional
    ~bigWinFunc = { arg widthHeight = ~widthOfBigWin, visible = ~visibilityOfBigWin;
      var bigCueWin;
      bigCueWin = Window.new("Huge Cue Number", Rect(1259, 900, widthHeight, widthHeight)).front;
      bigCueWin.background = Color.black;
      bigCueWin.visible = visible;
      // display cue-number
      ~bigTextCueNum =  StaticText(bigCueWin, Rect(width: widthHeight, height: widthHeight)).align_(\center);
      ~bigTextCueNum.font_(Font("Arial", widthHeight * 0.73)).stringColor_(Color.white);
    }.value;
    // window.onClose = {~oscInputLevels.free; ~sig_meter.free;};
    window.front;
  }

  initStructures { arg numOfChannels;
    // Chooses spatialisation strategy. Code works for any number of even speakers up to 8
    // no need to change these except if you want to do a different routing.

    if ( ~numOfChannels == 2, {
      ~front = [0,1];
      ~side = [0,1];
      ~rear = [0,1];
      ~centre = [0,1]; 
    });

    if ( ~numOfChannels == 4, {
      ~front = [0,1];
      ~side = [2,3];
      ~rear = [2,3];
      ~centre = [0,1]; 
    });

    if ( ~numOfChannels == 6, {
      ~front = [0,1];
      ~side = [2,3];
      ~rear = [4,5];
      ~centre = [0,1]; 
    });

    if ( ~numOfChannels == 8, {
      ~front = [0,1];
      ~side = [2,3];
      ~rear = [4,5];
      ~centre = [6,7]; 
    }); 

    ~groupA = Group.head(Server.default);
    ~groupB = Group.after(~groupA);
    ~groupZ = Group.tail(Server.default);
  }

  initCuePlayerResources {
    // This synth constantly sends information about the signals amplitude to the language
    SynthDef(\sig_meter, {
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

    // Monitor Outs 0-7, links to "monitor8b.scd"

    ~out_meter = SynthDef(\out_meter, {
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
  }

}
