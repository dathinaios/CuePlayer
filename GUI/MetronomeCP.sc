
MetronomeCP : AbstractGUIComponentCP { 

  var <metroButon, <>metroOutBox, <>metronomeVolume, <>bpmBox, <bpm = 120;

  setDefaultOptions {
    super.setDefaultOptions;
    options.tempoClock ?? { options.tempoClock = TempoClock.default };
  }

  createComponent {
    this.createMetronome;
    this.createBpmField;
  }

  createMetronome { var spec_Metro, metroOut, metro_Vol;
    metroOut = 1;
    metro_Vol = 0.1;
    this.createLabel("Metronome       Metro Vol.         Out          Bpm").align_(\left);
    metroButon = Button(window, Rect(width: 80, height: 20) );
    metroButon.states = [ ["Metro", Color.white, Color.grey], ["Metro", Color.white, Color(0.9, 0.5, 0.3)]];
    metroButon.canFocus = false;
    metroButon.font_();
    metroButon.action = { arg butState;
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

  clear {
    Pdef(\metronome).clear;
  }

  windowName {
    ^"Metronome"
  }

  windowHeight {
    ^60
  }


}