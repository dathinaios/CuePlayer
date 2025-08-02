// Cue Trigger Grid - A grid of buttons for triggering cues

(
var cuePlayer, gridWindow;
var updateGrid;
var buttonSize, margin, windowWidth, windowHeight;

// Function to create/update the grid
updateGrid = { |cp|
    var cols = 10;

    cuePlayer = cp;
    buttonSize = 100;
    margin = 10;
    windowWidth = (cols * buttonSize) + ((cols + 1) * margin);
    windowHeight = (5 * buttonSize) + (6 * margin);

    // Close existing window if it exists
    if(gridWindow.notNil and: { gridWindow.isClosed.not }) {
        gridWindow.close;
    };

    // Create new window
    gridWindow = Window("Cue Grid",
        Rect(
            GUI.window.screenBounds.width - windowWidth - 50,
            50,
            windowWidth,
            windowHeight
        ),
        resizable: false
    );
    gridWindow.background_(Color.fromHexString("#282828"));


    // Create grid of buttons
    50.do { |i|
        var button, cueNumber, xPos, yPos;

        cueNumber = i + 1;
        xPos = (i % cols) * (buttonSize + margin) + margin;
        yPos = (i / cols).floor * (buttonSize + margin) + margin;

        button = Button(gridWindow, Rect(xPos, yPos, buttonSize, buttonSize));

        // Set button appearance based on whether cue exists
        if(cuePlayer.cueList[i].notNil) {
            button.states_([[cueNumber.asString, Color.white, Color.fromHexString("#404040")]]);
            button.action = {
                cuePlayer.trigger(cueNumber);
            };
        } {
            button.states_([[cueNumber.asString, Color.fromHexString("#808080"), Color.fromHexString("#202020")]]);
            button.enabled_(false);
        };

    };

    gridWindow.front;
};

// Create the grid function that can be called
~createCueGrid = updateGrid;

)

// Example usage:
(
  a = CuePlayer.new;

  50.do { |i|
      a.put(i+1, { ("Cue " ++ (i+1)).postln });
  };

  ~createCueGrid.(a);
)
