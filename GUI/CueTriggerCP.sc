
CueTriggerCP : AbstractGUIComponentCP { 

  var <trigButton, <cueNumberBox, lrgCueWin, <largeCueNumberDisplay;

  setDefaultOptions {
    super.setDefaultOptions;
    options.largeDisplay ?? { options.largeDisplay = false };
    options.cueButtonFont ?? { options.cueButtonFont = Font("Lucida Grande", 12) };
    options.cueNumberBoxFont ?? { options.cueNumberBoxFont = Font("Lucida Grande", 22) };
  }

  createComponent {
    this.createCueTrigger;
  }

  createCueTrigger {
    this.createLabel("Trigger / Display & Reset Cue-number").align_(\left);
    this.createTriggerButton;
    this.createCueNumberBox;
    if (options.largeDisplay, { this.createLargeCueNumberDisplay })
  }

  createTriggerButton {
    trigButton = Button(window, Rect(10, 200, 220, 60));
    trigButton.font_(options.cueButtonFont);
    trigButton.canFocus = false;
    trigButton.states_([["Next Cue / FootSwitch", Color.white, Color.fromHexString("#1DA34D")]]);
    trigButton.action_(
      { "\nUse: \n \n cueTriggerCPInstance.trigButton.action = {}; \n \nto set this action to something useful!".postln; }
    );
  }

  createCueNumberBox {
    cueNumberBox = NumberBox(window, Rect(width: 50, height: 60)).align_(\center);
    cueNumberBox.font_(options.cueNumberBoxFont);
    cueNumberBox.action = { "\nUse: \n \n cueTriggerCPInstance.cueNumberBox.action = {}; \n \nto set this action to something useful!".postln; };
  }

  createLargeCueNumberDisplay { arg widthHeight = 950;
    lrgCueWin = Window.new("Huge Cue Number", Rect(window.bounds.left - widthHeight - 20, 900, widthHeight, widthHeight)).front;
    lrgCueWin.background = Color.black;
    largeCueNumberDisplay =  StaticText(lrgCueWin, Rect(width: widthHeight, height: widthHeight)).align_(\center);
    largeCueNumberDisplay.font_(Font(options.font.name, widthHeight * 0.73)).stringColor_(Color.white);
    lrgCueWin.front;
  }

  runResources {
  }

  clear {
    if (lrgCueWin.notNil and: {lrgCueWin.isClosed.not}) {lrgCueWin.close};
  }

  cmdPeriodAction {
    this.runResources;
  }

  windowName {
    ^"Cue Trigger"
  }

  windowHeight {
    ^95
  }

  setCurrent { arg val;
    cueNumberBox.value = val;
    if (options.largeDisplay, { largeCueNumberDisplay.string = val });
  }

}
