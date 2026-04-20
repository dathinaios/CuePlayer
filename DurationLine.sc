DurationLine {
  classvar <active = true;
  var <cuePlayer, largeDisplay, durationLinePlugin;

  *new { arg cuePlayer;
    ^super.newCopyArgs(cuePlayer);
  }

  *on {
    active = true;
  }

  *off {
    active = false;
  }

  *toggle {
    if(active,
      { active = false },
      { active = true });
  }

  refreshGuiRefs {
    var gui = cuePlayer.guiInstance;
    if(gui.notNil and:{ gui.active }) {
      largeDisplay       = gui.largeDisplay;
      durationLinePlugin = gui.pluginAt(\durationLine);
    } {
      largeDisplay = nil;
      durationLinePlugin = nil;
    };
  }

  create {
    arg duration = 5, color = Color.white, width = 2;

    if(active.not) { ^this };

    this.refreshGuiRefs;

    if(largeDisplay.notNil) {
      this.animateLargeWindow(duration, color, width);
    };

    if(durationLinePlugin.notNil) {
      this.animateStrip(duration, color, width);
    };
  }

  animateLargeWindow {
    arg duration = 5, color = Color.white, width = 2;

    var penFunction;
    var startTime = Main.elapsedTime;

    AppClock.sched(duration + 2, {
      largeDisplay.removeFromDrawFunc(penFunction);
    });

    penFunction = {
      var offset = (Main.elapsedTime - startTime) / duration * largeDisplay.lrgWinBounds.width;
      Pen.use {
        Pen.width = width;
        Pen.color_(color);
        Pen.beginPath;
        Pen.moveTo(Point(offset, 0));
        Pen.lineTo(Point(offset, largeDisplay.lrgWinBounds.height));
        Pen.stroke;
      };
    };

    largeDisplay.addToDrawFunc(penFunction);
  }

  animateStrip {
    arg duration = 5, color = Color.white, width = 2;

    var addToDrawFunc    = durationLinePlugin.at(\addToDrawFunc);
    var removeFromDrawFunc = durationLinePlugin.at(\removeFromDrawFunc);
    var viewBounds       = durationLinePlugin.at(\viewBounds);
    var bounds           = viewBounds.value;
    var startTime        = Main.elapsedTime;
    var penFunction;

    penFunction = {
      var offset = (Main.elapsedTime - startTime) / duration * bounds.width;
      Pen.use {
        Pen.width = width;
        Pen.color_(color);
        Pen.beginPath;
        Pen.moveTo(Point(offset, 0));
        Pen.lineTo(Point(offset, bounds.height));
        Pen.stroke;
      };
    };

    AppClock.sched(duration + 2, {
      removeFromDrawFunc.value(penFunction);
      nil
    });

    addToDrawFunc.value(penFunction);
  }

}
