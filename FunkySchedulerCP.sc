
FunkySchedulerCP {

  var <clock;
  var <functionList, <>latency;

  *new { arg clock;
    ^super.newCopyArgs(clock).init;
  }

  *newFromArray { arg clock, array;
    ^super.newCopyArgs(clock, array).init.fillFromArray(array);
  }

  init {
    if(clock.isNil, {clock = TempoClock(queueSize: 2048 * 128).permanent_(true)});
    functionList = List.new;
  }

  add{ arg time, function;
    functionList.add([time, function]);
  }

  fillFromArray { arg array;
    array.pairsDo{ arg time, function;
      this.add(time, function);
    }
  }

  play{ arg mode = \beats;
    switch (mode)
    { \beats } {this.scheduleToBeats}
    { \time  } {this.scheduleToTime}
  }

  scheduleToBeats {
    functionList.do{arg item; var beat, function;
      beat = item[0];
      function = item[1];
      this.sched(clock.timeToNextBeat+beat, function);
    }
  }

  scheduleToTime {
    functionList.do{arg item; var time, function;
      time = item[0]*clock.tempo;
      function = item[1];
      this.sched(clock.timeToNextBeat+time, function)
    }
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

  value { arg mode = \beats;
    this.play(mode);
  }

}
