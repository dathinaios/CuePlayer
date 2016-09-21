
MetronomeCP { 

  var window, <options; 
  var <metroButon, <>metroOutBox, <>metronomeVolume, <>bpmBox, <bpm = 120;

  *new { arg window, options = ();
    ^super.newCopyArgs( window, options).init;
  }

  init {
    this.setDefaultOptions;
    this.addSynths;
    this.createMetronome;
    this.createBpmField;
    this.setCmdPeriodActions;
    window.front;
  }

  setDefaultOptions {
    window ?? {this.createMainWindow};
    options.tempoClock ?? { options.tempoClock = TempoClock.default };
    options.font ?? {options.font = Font("Lucida Grande", 11)};
  }

  createMainWindow {
    window = Window("Metronome", Rect(1000, 1000, width: 282, height: 60), resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      this.clear;
    };
  }

  setCmdPeriodActions {
    CmdPeriod.add({
      AppClock.sched(0.1, {
        if ( metroButon.value == 1, {
          Pdef(\metronome, Pbind(\instrument, \metronome, \amp, metronomeVolume.value.linlin(0,1,0,0.5), \dur, 1, \freq, 800, \out, metroOutBox.value - 1 )).play(options.tempoClock, quant:[1]);
        });
      });
    });
  }

  createLabel { arg text = "placeholder text", width = 280, height = 20; var label;
    label = StaticText(window, Rect( width: width, height: height));
    label.font_(options.font);
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_(text);
    ^label;
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

  addSynths {
    SynthDef(\metronome, {arg amp = 0.2, freq = 800, out = 0;
      Out.ar(
        out,
        FSinOsc.ar(freq: freq, mul: amp * EnvGen.kr(Env.perc(attackTime:0.001, releaseTime:0.2),doneAction:2))
      );
    }).add;
  }

  height {
    ^window.view.bounds.height;
  }

  clear {
    Pdef(\metronome).clear;
  }

}
