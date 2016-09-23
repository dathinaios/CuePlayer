
Timeline {

  var <clock, options, path;
  var <functionList, <>latency;

  *new { arg clock, options = ();
    ^super.newCopyArgs(clock, options).init;
  }

  *newFromArray { arg array, clock, options = ();
    ^super.newCopyArgs(clock, options).init.fillFromArray(array);
  }

  *newFromPath { arg path, clock, options = (); var array;
    array = path.standardizePath.load;
    ^super.newCopyArgs(clock, options, path).init.fillFromArray(array);
  }

  init {
    if(clock.isNil, {clock = TempoClock.new.permanent_(true)});
    functionList = List.new;
    this.setDefaultOptions;
  }

  reloadPath { var array;
    functionList.clear;
    array = path.standardizePath.load;
    this.fillFromArray(array);
  }

  setDefaultOptions {
    options.mode ?? { options.mode = \beats };
    options.quant ?? { options.quant = true };
    options.liveReload ?? { options.liveReload = true };
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
    if(path.notNil and:{options.liveReload}, {
      this.reloadPath }
    );
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

  plot { arg timeUnitLength = 70;
    var plotWindow, plotUserView, currentForLine, currentForDots, orderedList;
    var makeLine, makeDots, dotColors;

    plotWindow = Window("Timeline", bounds: Rect(700, 500, width: 800 , height: 120), scroll: true).front;
    plotUserView = UserView(plotWindow , plotWindow.view.bounds);

    plotWindow.view.keyDownAction = {
      arg view, char, modifiers, unicode, keycode;
      /* [char, modifiers, unicode, keycode].postln; */
      switch(unicode)
      {45} {timeUnitLength = timeUnitLength - 5; plotWindow.refresh;} // -
      {61} {timeUnitLength = timeUnitLength + 5; plotWindow.refresh;} // =
    };

    functionList.do{ arg i, index; var newPosition;
      plotUserView.bounds = Rect(width: (plotUserView.bounds.width + timeUnitLength), height: 400 );
    };

    currentForLine = 0@0;
    currentForDots = 0@0;
    orderedList = functionList.sort{arg a, b; a[0]<b[0]};

    dotColors = List.new;
    orderedList.do{ arg i, index;
      dotColors = dotColors.add(Color.red(rrand(0.0, 1), rrand(0.4, 0.8)))
    };

    makeLine = { arg index;
      Pen.color = Color.black;
      Pen.fillOval(Rect(currentForLine.x - 1, currentForLine.y + 3, width: 2, height: 8 ));
      Pen.stringAtPoint( index.asString, currentForLine + [-3, 15]);
      Pen.color = Color.black;
      Pen.lineTo(currentForLine + [timeUnitLength, 0]);
      Pen.stroke;
    };

    makeDots = { arg itemTime, index;
      Pen.color = dotColors[index];
      Pen.fillOval(Rect((timeUnitLength*itemTime) - 4, currentForDots.y - 12, width: 8, height: 8 ));
      Pen.stroke;
    };

    plotWindow.view.background_(Color.white);
    plotWindow.drawFunc = {
      Pen.width = 0.05;
      Pen.color = Color.black;
      Pen.translate(50, 50);
      orderedList.do{ arg i, index;
        currentForDots = currentForDots + [timeUnitLength, 0];
        makeDots.(i[0], index);
      };
      (orderedList.last[0].abs+1).do{ arg i, index;
        makeLine.(index);
        currentForLine = currentForLine + [timeUnitLength, 0];
      };
      currentForLine = 0@0;
      currentForDots = 0@0;
    };
    plotWindow.refresh;
  }

}
