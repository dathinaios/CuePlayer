
ServerWindowCP : AbstractGUIComponentCP { 

  var serverInfoRoutine;
  var <muteButton, volSlider, peakCPULabel, numSynthsLabel;

  createComponent {
    this.createServerControls;
  }

  createServerControls { var spec;
    this.createLabel("Server").align_(\left);
    muteButton = Button(window, Rect(width: 80, height: 20) );
    muteButton.states = [["Mute", Color.white, Color.grey], ["Unmute", Color.white,  Color(0.9, 0.5, 0.3)]];
    muteButton.canFocus = false;
    muteButton.font_(options.font);
    if(Server.default.volume.isMuted){muteButton.value = 1};
    muteButton.action = { arg button;
      if(button.value == 0,
        {Server.default.unmute},
        {Server.default.mute}
      );
    };
    volSlider = Slider(window, Rect(width: 190, height: 20) ).background_(Color.fromHexString("#A0A0A0"));
    spec = ControlSpec(0.ampdb, 2.ampdb, \db, units: " dB");
    volSlider.value = spec.unmap(0);
    volSlider.canFocus = false;
    volSlider.action = { arg slider;
      Server.default.volume = spec.map(slider.value);
    };
    this.createLabel("", 282, 3);
    peakCPULabel = this.createLabel("Peak CPU : " ++ Server.default.peakCPU.round(0.1) ++ " %", width: 134, height: 20).align_(\left).stringColor_(Color.white);
    numSynthsLabel = this.createLabel("Synths : " ++ Server.default.numSynths, width: 134, height: 20).align_(\right).stringColor_(Color.white);
  }

  toggleMute {
    if(muteButton.value == 0,
      {muteButton.valueAction_(1)},
      {muteButton.valueAction_(0)}
    );
  }

  runResources {
    serverInfoRoutine = Routine{ 
      inf.do{
        peakCPULabel.string = "Peak CPU : " ++ Server.default.peakCPU.round(0.1) ++ " %";
        numSynthsLabel.string = "Synths : " ++ Server.default.numSynths;
        0.1.wait;
      }
    }.play(AppClock);
  }

  clear {
    serverInfoRoutine.stop;
  }

  cmdPeriodAction {
    this.runResources;
  }

  windowName {
    ^"Server"
  }

  windowHeight {
    ^80
  }

}
