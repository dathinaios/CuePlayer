
CuePlayerGUI {

  var cuePlayer, monitorInChannels, monitorOutChannels, options;
  var <window, name, clock;
  var <inputLevels,  <cueTrigger, <timer, <metronome, <outputLevels, <serverWindow;
  var <plugins;
  var <windowHeight = 0, font, titleFontSize, marginTop, <active = false;

  *new { arg cuePlayer, monitorInChannels = 2, monitorOutChannels = 8, options = ();
    ^super.newCopyArgs(cuePlayer, monitorInChannels, monitorOutChannels, options).init;
  }

  init {
    this.preparePluginSpecs;
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
      if(options.plugins.notEmpty) { this.createPlugins };
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
    options.infoDisplay ?? { options.infoDisplay = false };
    options.left ?? { options.left = GUI.window.screenBounds.width - 282 };
    options.top ?? { options.top = GUI.window.screenBounds.height - 330 };
    options.timer ?? { options.timer = true };
    options.metronome ?? { options.metronome = true };
    options.serverControls ?? { options.serverControls = true };
    options.shortcuts ?? { options.shortcuts = false };
    options.plugins ?? { options.plugins = List.new };
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
      plugins !? { plugins.do({ arg p; p.clear }) };
      cuePlayer.removeDependant(this);
      active = false;
    };
  }

  registerShortcuts {
    window.view.keyDownAction = {
      arg view, char, modifiers, unicode, keycode;
      /* [char, modifiers, unicode, keycode].postln; */
      switch(unicode)
      {32}  { cueTrigger.trigButton.doAction(0) }             //space - next cue
      {98}  { if(options.metronome){metronome.bpmBox.focus}}  //b     - focus on Bpm field
      {99}  { cueTrigger.cueNumberBox.focus }                 //c     - cue number
      {112} { if(options.timer){timer.togglePlay}}            //p     - play/pause
      {115} { if(options.timer){timer.stop} }                 //s     - stop
      {80}  { cuePlayer.plot }                                //P     - plot current timeline
      {109} {
		if(options.serverControls){                           //m     - mute server
		  serverWindow.toggleMute
		}
	  }
      {77}  { if(options.metronome){metronome.togglePlay} }   //M     - toggle metronome
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
    cueTrigger = CueTriggerCP(window,
      options: (
        largeDisplay: options.largeDisplay,
        largeDisplayBounds: options.largeDisplayBounds,
        infoDisplay: options.infoDisplay
      )
    );
    cueTrigger.trigButton.action = { var cueNum;
      cueNum = cuePlayer.next;
    };
    cueTrigger.cueNumberBox.action = { arg box;
      box.value = box.value.abs.round(1).asInteger;
      cuePlayer.current = box.value.asInteger;
      if (options.largeDisplay, {
        cueTrigger.largeCueNumberDisplay.string = box.value.asInteger;
      });
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

  createPlugins {
    plugins = IdentityDictionary.new;
    options.plugins.do({ arg entry; var mergedOptions, instance;
      mergedOptions = entry.options.copy;
      mergedOptions[\font]       ?? { mergedOptions[\font] = Font(font, titleFontSize) };
      mergedOptions[\cuePlayer]  ?? { mergedOptions[\cuePlayer] = cuePlayer };
      mergedOptions[\tempoClock] ?? { mergedOptions[\tempoClock] = clock };
      this.createLabel("", 282, marginTop);
      instance = PluginCP.new(window, mergedOptions);
      plugins[entry.id] = instance;
      windowHeight = windowHeight + instance.windowHeight;
    });
  }

  pluginAt { arg id; ^plugins !? { plugins[id] } }

  preparePluginSpecs {
    var normalizedEntries;
    options.plugins ?? { options.plugins = List.new };
    if(options.plugins.isKindOf(Event)) { options.plugins = [options.plugins] };
    if(options.plugins.isString.not and:{ options.plugins.isSequenceableCollection } and:{ options.plugins.size == 2 }
      and:{ options.plugins[0].isKindOf(Symbol) or:{ options.plugins[0].isString } }) {
      options.plugins = [options.plugins];
    };
    if(options.plugins.isSequenceableCollection.not) {
      "Ignoring plugins option: expected a collection of plugin entries.".warn;
      options.plugins = List.new;
      ^this;
    };
    normalizedEntries = List.new;
    options.plugins.do({ arg entry; var normalizedEntry;
      normalizedEntry = this.normalizedPluginEntry(entry);
      if(normalizedEntry.notNil) {
        if(normalizedEntries.any({ arg existing; existing.id == normalizedEntry.id })) {
          ("Plugin id % is already present in gui options; replacing.".format(normalizedEntry.id)).warn;
          normalizedEntries = normalizedEntries.reject({ arg existing; existing.id == normalizedEntry.id });
        };
        normalizedEntries.add(normalizedEntry);
      };
    });
    options.plugins = normalizedEntries;
  }

  normalizedPluginEntry { arg entry; var id, optionsOrPath, resolvedOptions;
    if(entry.isKindOf(Event)) {
      id = entry[\id];
      optionsOrPath = entry[\options] ?? { entry[\path] };
    } {
      if(entry.isString.not and:{ entry.isSequenceableCollection } and:{ entry.size == 2 }) {
        id = entry[0];
        optionsOrPath = entry[1];
      } {
        "Ignoring plugin entry: expected an Event or [id, optionsOrPath].".warn;
        ^nil;
      };
    };
    if(id.isNil) {
      "Ignoring plugin entry with no id.".warn;
      ^nil;
    };
    if(optionsOrPath.isNil) {
      "Ignoring plugin %: expected \\options or \\path.".format(id).warn;
      ^nil;
    };
    resolvedOptions = if(optionsOrPath.isString) {
      this.loadPluginOptionsFromPath(optionsOrPath);
    } {
      optionsOrPath
    };
    if(resolvedOptions.isKindOf(Event).not) {
      "Ignoring plugin %: expected an Event of options.".format(id).warn;
      ^nil;
    };
    ^(id: id, options: resolvedOptions);
  }

  loadPluginOptionsFromPath { arg path; var basePath;
    if(path.beginsWith("/").not and:{ path.beginsWith("~").not }) {
      basePath = thisProcess.nowExecutingPath;
      if(basePath.notNil) { path = PathName(basePath).pathOnly +/+ path };
    };
    ^path.standardizePath.load;
  }

  setServerVolume { arg val;
    serverWindow.volume_(val);
  }

  /* Handle Events from Dependants */

  update { arg theChanged, message;
    switch (message)
    {\current}
    {this.setCurrent(theChanged)}
    {\next}
    {this.startTimer(theChanged)}
    {\tempo}
    {
      if(options.metronome){
        metronome.bpm = theChanged.clock.tempo*60
      };
    };
    plugins !? { plugins.do({ arg p; p.update(theChanged, message) }) };
  }

  setCurrent { arg cuePlayer; var currentCue;
    currentCue = cuePlayer.current;
    cueTrigger.setCurrent(currentCue, cuePlayer.getCueObject(currentCue));
    if(options.timer and:{currentCue == 0}){
      timer.stop; timer.cursecs_(0); timer.pauseButton.value_(0);
    };
  }

  startTimer { arg cuePlayer; var currentCue;
    currentCue = cuePlayer.current;
    if(options.timer and:{currentCue != 0}){
      if (timer.isPlaying.not) {timer.play; timer.pauseButton.value_(1)};
    };
    if(options.timer and:{currentCue == 0}){
      timer.stop; timer.cursecs_(0); timer.pauseButton.value_(0);
    };
  }

}
