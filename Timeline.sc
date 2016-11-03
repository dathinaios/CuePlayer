
Timeline {

  var <>clock, <options, <>path;
  var <>functionList;

  *new { arg clock, options = ();
    ^super.newCopyArgs(clock, options).init;
  }

  *newFromArray { arg array, clock, options = ();
    ^super.newCopyArgs(clock, options).init.functionList_(array);
  }

  *newFromPath { arg path, clock, options = (); var pathReturn;
    pathReturn = path.standardizePath.load;
    ^pathReturn.asTimeline(clock, options).path_(path);
  }

  init {
    if(clock.isNil, {clock = TempoClock.new.permanent_(true)});
    functionList = Array.new;
    this.setDefaultOptions;
  }

  reloadPath { var pathReturn;
    if(path.notNil and:{options.liveReload}, {
      functionList.clear;
      pathReturn = path.standardizePath.load;
      this.functionList_(pathReturn)}
    );
  }

  setDefaultOptions {
    options.mode ?? { options.mode = \beats };
    options.quant ?? { options.quant = 1 };
    options.liveReload ?? { options.liveReload = true };
  }

  add{ arg time, function;
    functionList = functionList.add(time);
    functionList = functionList.add(function);
  }

  play {
    this.reloadPath; 
    functionList.pairsDo{arg time, function;
      this.sched(this.time(time), function);
    }
  }

  quantValue {
    if(options.quant > 0, 
      { ^clock.timeToNextBeat + (options.quant - 1) }, 
      { ^0 }
    );
  }

  time { arg value; var time;
    switch (options.mode)
    { \beats } {time = value - 1}
    { \time  } {time = value*clock.tempo;};
    ^(time + this.quantValue);
  }

  sched { arg time, function;
    Routine {
      time.wait; // in beats
      Server.default.makeBundle(Server.default.latency, function);
    }.play(clock);
  }

  stop {
    clock.clear;
  }

  pairUp { arg array; var newList = List.new;
    array.pairsDo{ arg time, function;
      newList.add([time, function]);
    };
    ^newList;
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
      {45} {
        timeUnitLength = timeUnitLength - 5;
        plotUserView.bounds = plotUserView.bounds - Rect(0,0,timeUnitLength,0);
        plotWindow.refresh;
      } // -
      {61} {
        timeUnitLength = timeUnitLength + 5;
        plotUserView.bounds = plotUserView.bounds + Rect(0,0,timeUnitLength,0);
        plotWindow.refresh;
      } // =
    };

    functionList.do{
      plotUserView.bounds = Rect(width: (plotUserView.bounds.width + timeUnitLength), height: 400 );
    };

    currentForLine = 0@0;
    currentForDots = 0@0;
    orderedList = this.pairUp(functionList).sort{arg a, b; a[0]<b[0]};

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
