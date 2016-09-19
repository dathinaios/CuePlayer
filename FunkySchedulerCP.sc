
FunkySchedulerCP {

  var <clock;
  var functionList;

  *new { arg clock;
    ^super.newCopyArgs(clock).init;
  }

  init {
    if(clock.isNil, {clock = TempoClock.new.permanent_(true)});
    functionList = List.new;
  }

  add{ arg time, function;
    functionList.add([time, function]);
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
      clock.sched(clock.timeToNextBeat+beat, function);
    }
  }

  scheduleToTime {
    functionList.do{arg item; var time, function;
      time = this.secs2beats(item[0]);
      function = item[1];
      clock.sched(clock.timeToNextBeat+time, function)
    }
  }

  stop {
    clock.clear;
  }

}
