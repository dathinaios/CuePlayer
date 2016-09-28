
MetronomeCP : AbstractGUIComponentCP { 

  var <metroButton, <>metroOutBox, <>metronomeVolume, <>bpmBox, <bpm = 120;

  setDefaultOptions {
    super.setDefaultOptions;
    options.tempoClock ?? { options.tempoClock = TempoClock.default };
    options.font ?? { options.font = Font("Lucida Grande", 11) };
  }

  createComponent {
    this.createMetronome;
    this.createBpmField;
  }

  createMetronome { var spec_Metro, metroOut, metro_Vol;
    metroOut = 1;
    metro_Vol = 0.1;
    this.createLabel("Metronome       Metro Vol.         Out          Bpm").align_(\left);
    metroButton = Button(window, Rect(width: 80, height: 20) );
    metroButton.states = [ ["Metro", Color.white, Color.grey], ["Metro", Color.white, Color(0.9, 0.5, 0.3)]];
    metroButton.canFocus = false;
    metroButton.font_(options.font);
    metroButton.action = { arg butState;
      if ( butState.value == 1, {
        Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut - 1 )).play(options.tempoClock, quant:[1]);
      });
      if ( butState.value == 0, { Pdef(\metronome).clear});
    };
    metronomeVolume = Slider(window, Rect(width: 82, height: 20) ).background_(Color.fromHexString("#A0A0A0"));
    spec_Metro = ControlSpec(minval: 0, maxval: 0.5);
    metronomeVolume.value = spec_Metro.unmap(metro_Vol);
    metronomeVolume.canFocus = false;
    metronomeVolume.action = {
      metro_Vol = spec_Metro.map(metronomeVolume.value);
      if (Pdef(\metronome).isPlaying == true, {
        Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut - 1 )).play(options.tempoClock, quant:[1])
      });
    };

    metroOutBox = NumberBox(window, Rect(width:50, height:20)).align_(\center);
    metroOutBox.background_(Color(0.9, 0.9, 0.9));
    metroOutBox.normalColor_(Color.black);

    metroOutBox.value = metroOut;
    metroOutBox.action = {arg box; metroOut = box.value;
      Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metro_Vol, \dur, 1, \freq, 800, \out, metroOut - 1 ))
    };
  }

  createBpmField {
    bpmBox = NumberBox(window, Rect(width:50, height:20)).align_(\center);
    bpmBox.background_(Color(0.9, 0.9, 0.9));
    bpmBox.normalColor_(Color.black);
    bpmBox.value = options.tempoClock.tempo*60;
    bpmBox.action = {arg box;
      options.tempoClock.tempo = box.value/60;
    };
  }

  bpm_ { arg value;
    bpmBox.valueAction = value;
  }

  runResources {
    SynthDef(\metronome, {arg amp = 0.2, freq = 800, out = 0;
      Out.ar(
        out,
        FSinOsc.ar(freq: freq, mul: amp * EnvGen.kr(Env.perc(attackTime:0.001, releaseTime:0.2),doneAction:2))
      );
    }).add;
  }

  togglePlay {
    if(metroButton.value == 0,
      {metroButton.valueAction_(1)},
      {metroButton.valueAction_(0)}
    );
  }

  clear {
    Pdef(\metronome).clear;
  }

  cmdPeriodAction {
    metroButton.value = 0;
    /* if ( metroButton.value == 1, { */
    /*   Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metronomeVolume.value.linlin(0,1,0,0.5), \dur, 1, \freq, 800, \out, metroOutBox.value - 1 )).play(options.tempoClock, quant:[1]); */
    /* }); */
  }

  windowName {
    ^"Metronome"
  }

  windowHeight {
    ^55
  }

  volume_{ arg value = 0.1;
    this.metronomeVolume.valueAction_(value)
  }

  outbus_{ arg value = 0;
    this.metroOutBox.valueAction_(value)
  }

}
