window.onload = function() {

    var toggleOn = document.getElementById("toggle-on");
    toggleOn.onclick = handleToggle;
    var toggleOff = document.getElementById("toggle-off");
    toggleOff.onclick = handleToggle;

    toggleTiming(toggleOn.style.display == "none");
};

function handleToggle() {
    var display = false;
    var toggleOn = document.getElementById("toggle-on");
    var toggleOff = document.getElementById("toggle-off");

    if (toggleOn.style.display == "none") {
        toggleOn.style.display = "block";
        toggleOff.style.display = "none";
    } else {
        toggleOn.style.display = "none";
        toggleOff.style.display = "block";
        display = true;
    }

    // update the time figures to reflect the state of the button
    toggleTiming(display);
}

function toggleTiming(show) {

    var timings = document.getElementsByClassName("time-fig");
    var timing;
    for (var i = 0; timing = timings[i]; i++) {
        timing.style.display = show ? "inherit" : "none";
    }

    timings = document.getElementsByClassName("time-run");

    for (var i = 0; timing = timings[i]; i++) {
        timing.style.display = show ? "inline" : "none";
    }

    timings = document.getElementsByClassName("time-fig-table-cell");

        for (var i = 0; timing = timings[i]; i++) {
            timing.style.display = show ? "table-cell" : "none";
        }
}