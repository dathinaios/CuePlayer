
DurationLine {
  classvar <active = true;
  var <cuePlayer, cueTrigger;

  *new { arg cuePlayer;
    ^super.newCopyArgs(cuePlayer).getCueTriggerFromCuePlayer;
  }

  *activate {
    active = true;
  }

  *deactivate {
    active = false;
  }

  *toggle {
    if(active,
      { active = false },
      { active = true });
  }

  getCueTriggerFromCuePlayer {
    Server.default.waitForBoot{
      cueTrigger = cuePlayer.guiInstance.cueTrigger;
    }
  }

  create {
    arg duration = 5, color = Color.white, width = 2;

    if(active and:{cueTrigger.lrgCueWin.notNil}, {
      this.animation(duration, color, width);
    });

  }

  animation { 
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
        Pen.lineTo(Point(0+offset, cueTrigger.lrgWinBounds.height));
        Pen.stroke;
        offset = offset + (cueTrigger.lrgWinBounds.width+20 / (duration*cueTrigger.frameRate));
      };
    };

    cueTrigger.addToDrawFunc(penFunction);
    
  }

}