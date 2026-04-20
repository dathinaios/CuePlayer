// Duration-line strip plugin spec.
// Returns an options Event for use in gui(options: (plugins: [\durationLine, "./Plugins/duration_line.scd"])).
(
  name: "Duration Line",
  height: 20,
  showLabel: false,
  build: { arg plug;
    var view, drawList, drawRoutine;
    var frameRate = 24;

    view = UserView(plug.contentView, Rect(0, 0, 270, 20));
    drawList = List.new;
    view.background_(Color.fromHexString("#1E1E1E"));
    view.drawFunc = { drawList.do({ arg f; f.value }) };

    drawRoutine = Routine({
      inf.do {
        view.refresh;
        (1 / frameRate).wait;
      };
    });

    plug.store(\view, view);
    plug.store(\drawList, drawList);
    plug.store(\addToDrawFunc, { arg func;
      if(drawList.size == 0) { drawRoutine.reset.play(AppClock) };
      drawList.add(func);
    });
    plug.store(\removeFromDrawFunc, { arg func;
      drawList.remove(func);
      if(drawList.size == 0) {
        drawRoutine.stop;
        view.refresh;
      };
    });
    plug.store(\viewBounds, { view.bounds });
    plug.store(\clear, {
      drawRoutine.stop;
      drawList.clear;
      view.refresh;
    });
    plug.store(\cmdPeriod, {
      drawList.clear;
      drawRoutine.reset.play(AppClock);
    });
  },
  onClear: { arg plug;
    plug.at(\clear).value;
  },
  onCmdPeriod: { arg plug;
    plug.at(\cmdPeriod).value;
  }
)
