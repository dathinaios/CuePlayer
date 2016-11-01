
OutputMetersCP : AbstractGUIComponentCP { 

  var <outputLevels, oscOutputLevels;

  setDefaultOptions {
    options.monitorOutChannels ?? { options.monitorOutChannels = 8 };
    options.groupOut ?? { options.groupOut = Server.default.defaultGroup};
    options.monitorOutChannels = options.monitorOutChannels.clip(1, 48);
    super.setDefaultOptions;
  }

  createComponent {
    this.createOutputLevels;
  }

  createOutputLevels{ var outlev, label;
    this.createLabel("Output meters").align_(\left);

    outlev = Array.newClear(options.monitorOutChannels);
    options.monitorOutChannels.do{ arg i; var compView;
      compView = CompositeView(window, Rect(width:  30, height: 70) );
      outlev[i] = LevelIndicator(compView, Rect(3, 0, width:  35, height: 50) );
      outlev[i].warning = -2.dbamp;
      outlev[i].critical = -1.dbamp;
      outlev[i].drawsPeak = true;
      outlev[i].background = Color.fromHexString("#A0A0A0");
      /* outlev[i].numTicks = 11; */
      /* outlev[i].numMajorTicks = 3; */
      label = StaticText(compView, Rect( 0, 50, width: 30, height: 20)).align_(\center);
      label.font_(Font(options.font));
      label.stringColor_(Color.fromHexString("#A0A0A0"));
      label.string = i+1;
    };
    /* create oscfunction */
    oscOutputLevels =
    OSCFunc({arg msg; var curMsg = 3;
      {
        options.monitorOutChannels.do{ arg chan;
          outlev[chan].value = msg[curMsg].ampdb.linlin(-75, 0, 0, 1);
          outlev[chan].peakLevel = msg[curMsg+1].ampdb.linlin(-75, 0, 0, 1);
          curMsg = curMsg + 2;
        }
      }.defer;
    }, ('/out_levels'++ options.monitorOutChannels).asSymbol, Server.default.addr);
    oscOutputLevels.permanent = true;
  }

  runResources {
    GroupManagerCP.initialise;
    SynthDef(\outputLevels ++ options.monitorOutChannels, {
      var trig, sig, delayTrig;

      sig = In.ar( options.monitorOutChannels.collect{arg i; i});
      trig = Impulse.kr(10);
      delayTrig = Delay1.kr(trig);

      SendReply.kr(trig, ('/out_levels'++ options.monitorOutChannels).asSymbol,
        options.monitorOutChannels.collect{ arg i;
          [Amplitude.kr( sig[i] ), // rms of signal1
          K2A.ar(Peak.ar( sig[i], delayTrig).lag(0, 3))] // peak of signal1
        }.flatten;
      );
    }).add;
    { outputLevels = Synth(\outputLevels ++ options.monitorOutChannels, target: GroupManagerCP.groupZ) }.defer(1);
  }

  clear {
    oscOutputLevels.free;
    outputLevels.free;
  }

  cmdPeriodAction {
    this.runResources;
  }

  windowName {
    ^"Outputs"
  }

  windowHeight { var levelsHeight;
    levelsHeight = ((options.monitorOutChannels/8).roundUp(1))*(50+25);
     ^(25 + levelsHeight);
  }

}
