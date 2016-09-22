
Timeline {

  var <clock, options;
  var <functionList, <>latency;

  *new { arg clock, options = ();
    ^super.newCopyArgs(clock, options).init;
  }

  *newFromArray { arg array, clock, options = ();
    ^super.newCopyArgs(clock, options).init.fillFromArray(array);
  }

  init {
    if(clock.isNil, {clock = TempoClock.new.permanent_(true)});
    functionList = List.new;
    this.setDefaultOptions;
  }

  setDefaultOptions {
    options.mode ?? { options.mode = \beats };
    options.quant ?? { options.quant = true };
  }

  add{ arg time, function;
    functionList.add([time, function]);
  }

  fillFromArray { arg array;
    array.pairsDo{ arg time, function;
      this.add(time, function);
    }
  }

  play{
    functionList.do{arg item;
      this.sched(this.time(item), item[1]);
    }
  }

  quantValue {
    if(options.quant) {^clock.timeToNextBeat} {^0};
  }

  time { arg item;var time;
    switch (options.mode)
    { \beats } {time = item[0]}
    { \time  } {time = item[0]*clock.tempo;};
    ^(time + this.quantValue);
  }

  sched { arg time, function;
    Routine {
      time.wait;
      Server.default.makeBundle(latency, function);
    }.play(clock);
  }

  stop {
    clock.clear;
  }

  asTimeline {
    ^this;
  }

}
