

TimerCP : AbstractGUIComponentCP { 

  var timer, <pauseButton, stopButton;

  setDefaultOptions {
    super.setDefaultOptions;
    options.font ?? { options.font = Font("Lucida Grande", 11) };
  }

  createComponent {
    this.createTimer;
  }

  createTimer {
    this.createLabel("Timer");
    this.createLabel("", 8);
    timer = ClockFaceCP.new(window);

    pauseButton = Button(window, Rect(width: 22, height: 20) );
    pauseButton.states = [[">", Color.white, Color.grey], ["||", Color.white, Color.grey]];
    pauseButton.font_(options.font);
    pauseButton.canFocus = false;
    pauseButton.action = { arg button; 
      if(button.value == 0, 
        { timer.stop  },
        { timer.play  }
      );
    };
    stopButton = Button(window, Rect(width: 22, height: 20) );
    stopButton.states = [ ["[ ]", Color.white, Color.grey]];
    stopButton.font_(options.font);
    stopButton.canFocus = false;
    stopButton.action = { arg butState; timer.stop; pauseButton.value = 0; timer.cursecs_(0); };
  }

  play {
    timer.play;
    pauseButton.value = 1;
  }

  stop {
    stopButton.valueAction_(0);
  }

  togglePlay {
    if(pauseButton.value == 0,
      {pauseButton.valueAction_(1)},
      {pauseButton.valueAction_(0)}
    );
  }

  runResources {
  }

  clear {
    timer.stop;
  }

  cmdPeriodAction {
    timer.stop;
    pauseButton.value = 0;
    timer.isPlaying = false;
  }

  windowName {
    ^"Timer"
  }

  windowHeight {
    ^55
  }

  cursecs_{ arg val;
    ^timer.cursecs_(val);
  }

  isPlaying {
    ^timer.isPlaying;
  }

}
