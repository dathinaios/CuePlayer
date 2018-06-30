
DurationLine {
  var <cuePlayer, cueTrigger;

  *new { arg cuePlayer;
    ^super.newCopyArgs(cuePlayer).init;
  }

  init {
    cueTrigger = cuePlayer.guiInstance.cueTrigger;
  }

  create {
    arg duration = 5, color = Color.white, width = 2;
    var offset = 0;
    var penFunction;

    AppClock.sched(duration+2, {
      cueTrigger.removeFromDrawFunc(penFunction);
    });

    penFunction = {
      Pen.use {
        Pen.width = width;
        Pen.color_(color);
        Pen.beginPath;
        Pen.moveTo(Point(0+offset,0));
        Pen.lineTo(Point(0+offset,600));
        Pen.stroke;
        offset = offset + (cueTrigger.lrgWinBounds.width+20 / (duration*cueTrigger.frameRate));
      };
    };

    cueTrigger.addToDrawFunc(penFunction);

  }

}