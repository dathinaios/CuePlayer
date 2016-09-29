
InputMetersCP : AbstractGUIComponentCP { 

  var <inputLevels, oscInputLevels;

  setDefaultOptions {
    options.monitorInChannels ?? { options.monitorInChannels = 2 };
    options.groupIn ?? { options.groupIn = Server.default.defaultGroup};
    options.monitorInOffset ?? { options.monitorInOffset = 0};
    options.monitorInChannels = options.monitorInChannels.clip(1,8);
    super.setDefaultOptions;
  }

  createComponent {
    this.createInputLevels;
  }

  createInputLevels { var inLevelArray;
    this.createLabel("Input meters").align_(\left);
    inLevelArray = Array.newClear(options.monitorInChannels);
    options.monitorInChannels.do{ arg i;
      inLevelArray[i] = this.createInputLevel;
      this.createLabel("  " ++ (i+1+options.monitorInOffset), 20).align_(\center);
    };
    oscInputLevels = OSCFunc({arg msg; var curMsg = 3;
      {
        options.monitorInChannels.do{ arg chan;
          inLevelArray[chan].value = msg[curMsg].ampdb.linlin(-75, 0, 0, 1);
          inLevelArray[chan].peakLevel = msg[curMsg+1].ampdb.linlin(-75, 0, 0, 1);
          curMsg = curMsg + 2;
        }
      }.defer;
    }, ( '/in_levels' ++ options.monitorInChannels).asSymbol, Server.default.addr);
    oscInputLevels.permanent = true;
  }

  createInputLevel { var level;
    level = LevelIndicator(window, Rect(width:  220, height: 20));
    level.warning = -2.dbamp;
    level.critical = -1.dbamp;
    level.drawsPeak = true;
    level.background = Color.fromHexString("#A0A0A0");
    ^level;
  }

  runResources {
    GroupManagerCP.initialise;
    SynthDef(\inputLevels ++ options.monitorInChannels, {
      var trig, sig, delayTrig;

      sig = SoundIn.ar( options.monitorInChannels.collect{arg i; i+options.monitorInOffset});
      trig = Impulse.kr(10);
      delayTrig = Delay1.kr(trig);

      SendReply.kr(trig, ( '/in_levels' ++ options.monitorInChannels).asSymbol,
        options.monitorInChannels.collect{ arg i; var chan;
          if(options.monitorInChannels == 1) { chan = sig} {chan = sig[i]};
          [Amplitude.kr(chan), // rms of signal1
          K2A.ar(Peak.ar(chan, delayTrig).lag(0, 3))] // peak of signal1
        }.flatten;
      );
    }).add;
    { inputLevels = Synth(\inputLevels ++ options.monitorInChannels, target: GroupManagerCP.groupA )}.defer(1);
  }

  clear {
    inputLevels.free;
    oscInputLevels.free;
  }

  cmdPeriodAction {
    this.runResources;
  }

  windowName {
    ^"Inputs"
  }

  windowHeight {
    ^(25+(options.monitorInChannels*25))
  }

}
