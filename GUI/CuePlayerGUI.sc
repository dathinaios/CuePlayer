
CuePlayerGUI {

  var cuePlayer, monitorInChannels, monitorOutChannels, options; 
  var <window;
  var name, clock;
  var timer, trigButton, pauseButton, cueNumberDisplay, <metronome, serverWindowCP,
      lrgCueWin, largeCueNumberDisplay;
  var font, titleFontSize, marginTop, <active = false;

  /* Server and Routing */
  var outputLevels, inputLevels, oscInputLevels, oscOutLevels;
  var <groupA, <groupB,  <groupZ;

  *new { arg cuePlayer, monitorInChannels = 2, monitorOutChannels = 8, options = ();
    ^super.newCopyArgs(cuePlayer, monitorInChannels.clip(2, 8), monitorOutChannels.clip(1, 256), options).init;
  }

  init {
    clock = cuePlayer.clock;
    name = cuePlayer.name ?? "Cue Player";

    this.setDefaultOptions;
    this.initStyleVariables;
    this.createMainWindow;
    this.createInputLevels;
    this.createCueTrigger;
    this.createTimer;
    this.createMetronome;
    this.createOutputLevels;
    this.initGroups;
    this.createServerControls;
    this.registerShortcuts;
    this.setCmdPeriodActions;

    active = true;
    window.front;
  }

  setDefaultOptions {
    options.monitorInOffset ?? { options.monitorInOffset = 0 };
    options.largeDisplay ?? { options.largeDisplay = false };
    options.left ?? { options.left = 1400 };
    options.top ?? { options.top = 650 };
  }

  setCmdPeriodActions {
    CmdPeriod.add({
      AppClock.sched(0.1, {
        this.initServerResources;
      });
    });
  }

  initStyleVariables {
    font = "Lucida Grande";
    titleFontSize = 11;
    marginTop = 3;
  }

  createMainWindow {
    window = Window.new(name, Rect(options.left, options.top, 282, 330 + this.calculateLevelSumHeight + (marginTop*4)), resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      metronome.clear;
      serverWindowCP.clear;
      inputLevels.clear;
      outputLevels.clear;
      timer.stop;
      cuePlayer.removeDependant(this);
      if (lrgCueWin.notNil and: {lrgCueWin.isClosed.not}) {lrgCueWin.close};
      active = false;
    };
  }

  calculateLevelSumHeight { 
    var outHeightSum, inHeightSum, labelHeight = 20, outMeterHeight = 50, inMeterHeight = 20;
    labelHeight = labelHeight + 5;
    outMeterHeight = outMeterHeight;
    inMeterHeight = inMeterHeight + 5;
    outHeightSum = ((monitorOutChannels/8).roundUp(1))*(outMeterHeight+labelHeight);
    inHeightSum = monitorInChannels*inMeterHeight;
    ^(outHeightSum + inHeightSum);
  }

  registerShortcuts {
    window.view.keyDownAction = { 
      arg view, char, modifiers, unicode, keycode; 
      /* [char, modifiers, unicode, keycode].postln; */ 
      switch(unicode)
      {32} { trigButton.doAction(0) } //space
      {109} {} // m
      {77} {} //M
      {112} {} //p
    }; 
  }

  /* -------- */

  createInputLevels {
    inputLevels = InputMetersCP(
      window, 
      options: (
        monitorInChannels: monitorInChannels, 
        monitorInOffset: options.monitorInOffset, 
        font: Font(font, titleFontSize),
        groupIn: groupA
      )
    );
  }
  /* Cue Trigger */

  createCueTrigger {
    this.createLabel("", 282, marginTop);
    this.createLabel("Trigger / Display & Reset Cue-number").align_(\left);
    this.createTriggerButton;
    this.createCueNumberDisplay;
    if (options.largeDisplay, { this.createLargeCueNumberDisplay })
  }

  createLabel { arg text = "placeholder text", width = 280, height = 20; var label;
    label = StaticText(window, Rect( width: width, height: height));
    label.font_(Font(font, titleFontSize));
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_(text);
    ^label;
  }

  createTriggerButton {
    trigButton = Button(window, Rect(10, 200, 220, 60));
    trigButton.font_(Font(font, 12));
    trigButton.canFocus = false;
    trigButton.states_([["Next Cue / FootSwitch", Color.white, Color.fromHexString("#1DA34D")]]);
    trigButton.action_(
      {
        var cueNum;
        cueNum = cuePlayer.next;
        if (timer.isPlaying.not) {timer.play; pauseButton.value_(1)};
      }
    );
  }

  createCueNumberDisplay {
    cueNumberDisplay = NumberBox(window, Rect(width: 50, height: 60)).align_(\center);
    cueNumberDisplay.value = cuePlayer.current;
    cueNumberDisplay.font_(Font(font, 26));
    cueNumberDisplay.action = {
      arg box;
      cuePlayer.current = box.value.abs;
      timer.stop;
      if(box.value == 0){timer.stop; timer.cursecs_(0)};
      if (options.largeDisplay, { largeCueNumberDisplay.string = box.value })
    };
  }

  createLargeCueNumberDisplay { arg widthHeight = 950;
    lrgCueWin = Window.new("Huge Cue Number", Rect(1259, 900, widthHeight, widthHeight)).front;
    lrgCueWin.background = Color.black;
    largeCueNumberDisplay =  StaticText(lrgCueWin, Rect(width: widthHeight, height: widthHeight)).align_(\center);
    largeCueNumberDisplay.font_(Font(font, widthHeight * 0.73)).stringColor_(Color.white);
    largeCueNumberDisplay.string = cuePlayer.current;
  }

  /* Timer */

  createTimer { var stopButton;
    this.createLabel("", 282, marginTop);
    this.createLabel("Timer");
    this.createLabel("", 8);
    timer = ClockFaceCP.new(window);

    pauseButton = Button(window, Rect(width: 22, height: 20) );
    pauseButton.states = [[">", Color.white, Color.grey], ["||", Color.white, Color.grey]];
    pauseButton.font_(Font(font, titleFontSize));
    pauseButton.canFocus = false;
    pauseButton.action = { arg button; 
      if(button.value == 0, 
        { timer.stop  },
        { timer.play  }
      );
    };
    stopButton = Button(window, Rect(width: 22, height: 20) );
    stopButton.states = [ ["[ ]", Color.white, Color.grey]];
    stopButton.font_(Font(font, titleFontSize));
    stopButton.canFocus = false;
    stopButton.action = { arg butState; timer.stop; timer.cursecs_(0); };
  }

  createMetronome{
    this.createLabel("", 282, marginTop);
    metronome = MetronomeCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
  }

  /* Output Levels */

  createOutputLevels{
    this.createLabel("", 282, marginTop);
    outputLevels = OutputMetersCP(
      window, 
      options: (
        monitorOutChannels: monitorOutChannels, 
        font: Font(font, titleFontSize),
        groupOut: groupZ
      )
    );
  }

  /* Master */

  createServerControls {
    serverWindowCP = ServerWindowCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
  }

  /* Server Resources */

  initGroups {
    groupA = Group.head(Server.default);
    groupB = Group.after(groupA);
    groupZ = Group.tail(Server.default);
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
    cueNumberDisplay.value = val;
    if (options.largeDisplay, { largeCueNumberDisplay.string = val });
  }

}
