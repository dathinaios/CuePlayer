
CuePlayerGUI {

  var cuePlayer, monitorInChannels, monitorOutChannels, options; 
  var <window, name, clock;
  var <inputLevels,  <cueTrigger, <timer, <metronome, <outputLevels, <serverWindow;
  var <windowHeight = 0, font, titleFontSize, marginTop, <active = false;

  *new { arg cuePlayer, monitorInChannels = 2, monitorOutChannels = 8, options = ();
    ^super.newCopyArgs(cuePlayer, monitorInChannels, monitorOutChannels, options).init;
  }

  init {
    Server.default.waitForBoot{
      clock = cuePlayer.clock;
      name = "Cue Player";

      this.setDefaultOptions;
      this.initStyleVariables;
      this.createMainWindow;
      if(monitorInChannels > 0) {this.createInputLevels};
      this.createCueTrigger;
      if(options.timer) {this.createTimer };
      if(options.metronome) { this.createMetronome };
      if(monitorOutChannels > 0) { this.createOutputLevels };
      if(options.serverControls) { this.createServerControls };
      if(options.shortcuts) { this.registerShortcuts };

      active = true;
      window.bounds.height = windowHeight;
      window.bounds = window.bounds.height_(windowHeight + 10);
      window.front;
    }
  }

  setDefaultOptions {
    options.monitorInOffset ?? { options.monitorInOffset = 0 };
    options.largeDisplay ?? { options.largeDisplay = false };
    options.largeDisplayBounds ?? { options.largeDisplayBounds = nil };
    options.left ?? { options.left = GUI.window.screenBounds.width - 282 };
    options.top ?? { options.top = GUI.window.screenBounds.height - 330 };
    options.timer ?? { options.timer = true };
    options.metronome ?? { options.metronome = true };
    options.serverControls ?? { options.serverControls = true };
    options.shortcuts ?? { options.shortcuts = false };
  }

  initStyleVariables {
    font = "Lucida Grande";
    titleFontSize = 11;
    marginTop = 3;
  }

  createMainWindow {
    window = Window.new(name, Rect(options.left, options.top, 282, 330), resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      inputLevels.clear;
      cueTrigger.clear;
      timer.clear;
      metronome.clear;
      outputLevels.clear;
      serverWindow.clear;
      cuePlayer.removeDependant(this);
      active = false;
    };
  }

  registerShortcuts {
    window.view.keyDownAction = { 
      arg view, char, modifiers, unicode, keycode; 
      /* [char, modifiers, unicode, keycode].postln; */ 
      switch(unicode)
      {32}  { cueTrigger.trigButton.doAction(0) }   //space - next cue
      {98}  { metronome.bpmBox.focus }              //b     - focus on Bpm field
      {99}  { cueTrigger.cueNumberBox.focus }       //c     - cue number
      {112} { timer.togglePlay }                    //p     - play/pause
      {115} { timer.stop }                          //s     - stop
      {80}  { cuePlayer.plot }                      //P     - plot current timeline
      {109} { serverWindow.toggleMute }             //m     - mute server
      {77}  { metronome.togglePlay }                //M     - toggle metronome
    }; 
  }

  createLabel { arg text = "placeholder text", width = 280, height = 20; var label;
    label = StaticText(window, Rect( width: width, height: height));
    label.font_(Font(font, titleFontSize));
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_(text);
    ^label;
  }

  /* -------- */

  createInputLevels {
    inputLevels = InputMetersCP(
      window, 
      options: (
        monitorInChannels: monitorInChannels, 
        monitorInOffset: options.monitorInOffset, 
        font: Font(font, titleFontSize)
      )
    );
    windowHeight = windowHeight + inputLevels.windowHeight;
  }

  createCueTrigger {
    this.createLabel("", 282, marginTop);
    cueTrigger = CueTriggerCP(window, options: (largeDisplay: options.largeDisplay, largeDisplayBounds: options.largeDisplayBounds));
    cueTrigger.trigButton.action = { var cueNum;
        cueNum = cuePlayer.next;
        if (timer.isPlaying.not) {timer.play; timer.pauseButton.value_(1)};
    };
    cueTrigger.cueNumberBox.action = { arg box;
      box.value = box.value.abs.round(1);
      cuePlayer.current = box.value;
      if (box.value == 0,       {timer.stop; timer.cursecs_(0)});
      if (options.largeDisplay, {cueTrigger.largeCueNumberDisplay.string = box.value});
    };
    cueTrigger.setCurrent(cuePlayer.current);
    windowHeight = windowHeight + cueTrigger.windowHeight;
  }

  createTimer {
    this.createLabel("", 282, marginTop);
    timer = TimerCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
    windowHeight = windowHeight + timer.windowHeight;
  }

  createMetronome{
    this.createLabel("", 282, marginTop);
    metronome = MetronomeCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
    windowHeight = windowHeight + metronome.windowHeight;
  }

  setMetronomeVolume{ arg val;
    metronome.volume_(val);
  }

  setMetronomeBus{ arg val;
    metronome.outbus_(val);
  }

  createOutputLevels{
    this.createLabel("", 282, marginTop);
    outputLevels = OutputMetersCP(
      window, 
      options: (
        monitorOutChannels: monitorOutChannels, 
        font: Font(font, titleFontSize)
      )
    );
    windowHeight = windowHeight + outputLevels.windowHeight;
  }

  createServerControls {
    serverWindow = ServerWindowCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
    windowHeight = windowHeight + serverWindow.windowHeight;
  }

  /* Handle Events from Dependants */

  update { arg theChanged, message;
    switch (message)
    {\current}
    {this.setCurrent(theChanged.current)}
    {\tempo}
    {metronome.bpm = theChanged.clock.tempo*60}
  }

  setCurrent { arg val;
    cueTrigger.setCurrent(val);
  }

}
