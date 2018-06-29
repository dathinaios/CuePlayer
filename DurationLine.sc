
DurationLine {
  var <cueTrigger;

  *new { arg cueTrigger;
    ^super.newCopyArgs(cueTrigger).init;
  }

  init {
  }

  create {
    arg duration = 5, color = Color.white;
    var offset = 0;
    var penFunction;

    AppClock.sched(duration, {
      cueTrigger.removeFromDrawFunc(penFunction);
    });

    penFunction = {
      Pen.use {
        Pen.width = 10;
        Pen.color_(color);
        Pen.beginPath;
        Pen.moveTo(Point(0+offset,0));
        Pen.lineTo(Point(0+offset,600));
        Pen.stroke;
        offset = offset + (cueTrigger.lrgWinBounds.width / (duration*cueTrigger.frameRate));
      };
    };
    
    cueTrigger.addToDrawFunc(penFunction);

  }

}