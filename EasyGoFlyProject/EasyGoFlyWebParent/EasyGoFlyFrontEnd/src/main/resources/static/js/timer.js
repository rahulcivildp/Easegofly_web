 // Set the date and time to count down to (in this example, it's set to 10 minutes from the current time)
var countDownDate = new Date().getTime() + timeInSec;

// Update the countdown every second
var x = setInterval(function() {
  var now = new Date().getTime();
  var distance = countDownDate - now;

  // Calculate remaining time
  var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
  var seconds = Math.floor((distance % (1000 * 60)) / 1000);

  // Display the countdown timer in the "timer" div
  document.getElementById("timer").innerHTML = minutes + "m " + seconds + "s ";
  $("#timeRemaining").attr("value", distance);
  $(".timeRemain").attr("value", distance);
  // When the countdown timer reaches zero, display a message
  if (distance < 2) {
    clearInterval(x);
    document.location.href = moduleURL;
  }
}, 0);